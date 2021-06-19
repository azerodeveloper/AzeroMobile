package com.soundai.azero.azeromobile.ui.activity.launcher

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.GridLayoutAnimationController
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.animation.getAnimationSetFromLeft
import com.soundai.azero.azeromobile.common.bean.skilltip.SkillListTipsResponse
import com.soundai.azero.azeromobile.common.decoration.GridItemDecoration
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.setCurrentItem
import com.soundai.azero.azeromobile.ui.EndlessRecyclerOnScrollListener
import com.soundai.azero.azeromobile.ui.LoadMoreWrapper
import com.soundai.azero.azeromobile.ui.RecycleAdapter
import com.soundai.azero.azeromobile.ui.Setting
import com.soundai.azero.azeromobile.ui.activity.launcher.adapter.SkillListTipsAdapter
import com.soundai.azero.azeromobile.ui.activity.launcher.head.IHeadView
import com.soundai.azero.azeromobile.ui.activity.launcher.item.IGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.LauncherViewModel
import com.soundai.azero.azeromobile.ui.widget.StaggeredGridRecyclerView
import kotlinx.coroutines.*

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-03-15.
 */

class ListShowFragment : Fragment(), MediaPlayerHandler.AudioPlayerListener {
    private var currentHeadView: IHeadView? = null
    private lateinit var recyclerView: StaggeredGridRecyclerView
    private lateinit var cvHeadContent: CardView
    private lateinit var loadMoreAdapter: LoadMoreWrapper
    private lateinit var vpTitle: ViewPager2
    private lateinit var vpTips: ViewPager2
    private lateinit var tipTitleAdapter: SkillListTipsAdapter
    private lateinit var tipAdapter: SkillListTipsAdapter
    private var currentTitleIndex = 0
    private var currentTipIndex = 0
    private var loopJob: Job? = null

    private val launcherViewModel by lazy {
        ViewModelProviders.of(activity!!).get(LauncherViewModel::class.java)
    }
    private val DISMISS_EVENT_TIME = 8 * 1000

    private val dismissTimer = object : CountDownTimer(DISMISS_EVENT_TIME.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (TaApp.isHelloSkill) {
                TaApp.isHelloSkill = false
                return
            }
            (activity as LauncherActivity).setFragment(1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_launcher, container, false)
        recyclerView = root.findViewById(R.id.rv_content)
        cvHeadContent = root.findViewById(R.id.cv_launcher_head)
        vpTitle = root.findViewById(R.id.vp_title)
        vpTips = root.findViewById(R.id.vp_tips)
        loadMoreAdapter = LoadMoreWrapper(RecycleAdapter(activity!!))
        initView()
        initViewModel(loadMoreAdapter)
        var audioPlayer: MediaPlayerHandler =
            AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER) as MediaPlayerHandler
        audioPlayer.setAudioPlayerListener(this)

        val gridLayoutManager = GridLayoutManager(
            this.requireContext(),
            Setting.LAUNCHER_SPANSIZE
        )

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val data = loadMoreAdapter.datas
                return if (position < data.size) {
                    data[position].getSpanSize()
                } else {
                    Setting.LAUNCHER_SPANSIZE
                }
            }
        }
        recyclerView.run {
            adapter = loadMoreAdapter
            layoutManager = gridLayoutManager
            addItemDecoration(
                GridItemDecoration(
                    16,
                    0,
                    Color.BLACK,
                    false
                )
            )
            addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
                override fun onScrolled() {
                    dismissTimer.cancel()
                }

                override fun onLoadMore() {
                    loadMoreAdapter.setLoadState(loadMoreAdapter.LOADING)

                    // if (mAdapter.itemCount < 52) {
                    //     // 模拟获取网络数据，延时1s
                    //     Timer().schedule(object : TimerTask() {
                    //         override fun run() {
                    //             runOnUiThread {
                    //                 getMoreData()
                    //                 mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                    //             }
                    //         }
                    //     }, 2000)
                    // } else {
                    // 显示加载到底的提示
                    loadMoreAdapter.setLoadState(loadMoreAdapter.LOADING_END)
                    // }
                }
            })
        }

        return root

    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        tipTitleAdapter = SkillListTipsAdapter(activity!!)
        tipAdapter = SkillListTipsAdapter(activity!!)

        vpTitle.isUserInputEnabled = false
        vpTips.isUserInputEnabled = false
        vpTitle.adapter = tipTitleAdapter
        vpTips.adapter = tipAdapter
    }

    private fun initViewModel(loadMoreAdapter: LoadMoreWrapper) {
        launcherViewModel.gridItems.observeForever {
            onGridItemUpdate(loadMoreAdapter, it)
        }
        launcherViewModel.headView.observeForever {
            onHeadUpdate(it)
        }

        launcherViewModel.skillListTips.observeForever {
            onTipsDataUpdate(it)
        }
    }

    private fun onTipsDataUpdate(it: SkillListTipsResponse) {
        if (it.items.isEmpty()) return
        val tipTitles = arrayListOf<Pair<String,Boolean>>()
        val tips = arrayListOf<Pair<String,Boolean>>()
        it.items.forEach{item->
            tipTitles.add(Pair(item.title,false))
            for (tipsIndex in item.tips.indices) {
                if(tipsIndex == 0){
                    tips.add(Pair(item.tips[tipsIndex],true))
                }else{
                    tips.add(Pair(item.tips[tipsIndex],false))
                }
            }
        }
        currentTipIndex = 0
        currentTitleIndex = 0
        if (vpTitle.isFakeDragging) {
            log.d("viewpager2 isFakeDragging")
            CoroutineScope(Dispatchers.Main).launch {
                delay(800)
                vpTitle.setCurrentItem(currentTitleIndex, false)
            }
        } else {
            vpTitle.setCurrentItem(currentTitleIndex, false)
        }

        if (vpTips.isFakeDragging) {
            log.d("viewpager2 isFakeDragging")
            CoroutineScope(Dispatchers.Main).launch {
                delay(800)
                vpTips.setCurrentItem(currentTipIndex, false)
            }
        } else {
            vpTips.setCurrentItem(currentTipIndex, false)
        }
        tipTitleAdapter.data = tipTitles
        tipAdapter.data = tips
    }

    private fun onGridItemUpdate(
        loadMoreAdapter: LoadMoreWrapper,
        gridItemList: MutableList<IGridItem>?
    ) {
        if (gridItemList.isNullOrEmpty()) return
        val oldDataList = loadMoreAdapter.datas
        var count = 0
        gridItemList.forEach { it.serial = count++ }
        if (oldDataList.size == 0) {
            val animationController =
                GridLayoutAnimationController(getAnimationSetFromLeft())
            animationController.rowDelay = 0.3f
            animationController.columnDelay = 0.2f
            recyclerView.layoutAnimation = animationController
            loadMoreAdapter.datas = gridItemList
            loadMoreAdapter.notifyDataSetChanged()
            recyclerView.scheduleLayoutAnimation()
        } else {
            val diffResult =
                DiffUtil.calculateDiff(
                    RecycleAdapter.DiffCallback(
                        oldDataList,
                        gridItemList
                    )
                )
            loadMoreAdapter.datas = gridItemList
            diffResult.dispatchUpdatesTo(loadMoreAdapter)
        }
        recyclerView.scrollToPosition(0)
    }

    private fun onHeadUpdate(headView: IHeadView) {
        currentHeadView?.release()
        currentHeadView = headView
        cvHeadContent.removeAllViews()
        headView.inflateHeadView(cvHeadContent)
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && tipAdapter.data.isNotEmpty()) {
            loopJob = createLoopJob()
        } else {
            loopJob?.cancel()
        }
    }

    private fun createLoopJob(): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(8000)
                currentTipIndex++
                if (titleNeedScroll(currentTipIndex)) {
                    currentTitleIndex++
                    vpTitle.setCurrentItem(currentTitleIndex, 800)
                }
                vpTips.setCurrentItem(currentTipIndex, 800)
            }
        }
    }

    private fun titleNeedScroll(nexTipIndex: Int): Boolean {
        val nextTip = tipAdapter.data[nexTipIndex % tipAdapter.data.size]
        return nextTip.second
    }

    override fun onAudioPlayerStopped() {
        log.e("ListFragment onAudioPlayerStopped()")
        var topActivity = ActivityLifecycleManager.getInstance().topActivity
        if ((topActivity is LauncherActivity) && !((topActivity as LauncherActivity).getCurrentFragment() is SkillShowFragment)) {
            dismissTimer.start()
        }
    }

    override fun play() {
        log.e("ListFragment  play()")
        dismissTimer.cancel()
    }

    override fun onAudioPlayerStarted() {
    }

    override fun prepare() {
        log.e("ListFragment  prepare()")
    }

    override fun stop() {
        log.e("ListFragment  stop()")
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun seekTo(p0: Long) {
    }


}