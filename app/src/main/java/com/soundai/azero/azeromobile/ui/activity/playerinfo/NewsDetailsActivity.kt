package com.soundai.azero.azeromobile.ui.activity.playerinfo

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.soundai.azero.azeromobile.Constant.KEY_NEWS_PARCELABLE
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.ui.activity.launcher.item.NewsGridItem
import com.soundai.azero.azeromobile.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by xingw on 2019/11/2
 */
class NewsDetailsActivity : BasePlayerInfoActivity() {
    private val title by lazy { findViewById<TextView>(R.id.tv_news_title) }
    private val details by lazy { findViewById<TextView>(R.id.tv_news_details) }
    private val author by lazy { findViewById<TextView>(R.id.tv_news_author) }
    private val time by lazy { findViewById<TextView>(R.id.tv_news_time) }
    private val img by lazy { findViewById<ImageView>(R.id.iv_news_img) }

    companion object {
        fun start(
            activity: Activity,
            newsGridItem: NewsGridItem,
            img: View? = null,
            title: View? = null
        ) {
            CoroutineScope(Dispatchers.Main).launch(coroutineExceptionHandler) {
                if (img == null || title == null) {
                    val intent = Intent(
                        activity,
                        NewsDetailsActivity::class.java
                    )
                    intent.putExtra(KEY_NEWS_PARCELABLE, newsGridItem)
                    activity.startActivity(intent)
                } else {
                    val intent = Intent(activity.baseContext, NewsDetailsActivity::class.java)
                    intent.putExtra(KEY_NEWS_PARCELABLE, newsGridItem)
                    val imgPair: androidx.core.util.Pair<View, String> =
                        androidx.core.util.Pair(img, ViewCompat.getTransitionName(img)!!)
                    val titlePair: androidx.core.util.Pair<View, String> =
                        androidx.core.util.Pair(title, ViewCompat.getTransitionName(title)!!)
                    val activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            imgPair,
                            titlePair
                        )
                    ActivityCompat.startActivity(activity, intent, activityOptionsCompat.toBundle())
                }
            }
        }
    }

    override val layoutResId: Int
        get() = R.layout.activity_details_news

    override fun initView() {
        val newsGridItem = intent.getParcelableExtra<NewsGridItem>(KEY_NEWS_PARCELABLE)
        details.text = newsGridItem.title
        title.text = newsGridItem.title
        author.text = newsGridItem.author
        time.text = newsGridItem.time

        if (Utils.isLandscape(this)) {
            img.layoutParams.width = Utils.dp2px(345f).toInt()
        } else {
            img.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        Glide.with(img).load(newsGridItem.img[0])
            .apply(RequestOptions().centerCrop())
            .into(img)

        ViewCompat.setTransitionName(img, "TitleImg:" + newsGridItem.title)
        ViewCompat.setTransitionName(title, "Title:" + newsGridItem.title)

        window.enterTransition = Fade()
        window.exitTransition = Fade()

        val transitionSet = TransitionSet()
        transitionSet.addTransition(ChangeBounds())
        transitionSet.addTransition(ChangeTransform())
        transitionSet.addTarget(img)
        transitionSet.addTarget(title)
        window.sharedElementEnterTransition = transitionSet
        window.sharedElementExitTransition = transitionSet
    }

    override fun initData(intent: Intent) {
        intent.let {
            val newsGridItem = intent.getParcelableExtra<NewsGridItem>(KEY_NEWS_PARCELABLE)
            details.text = newsGridItem.title
            title.text = newsGridItem.title
            author.text = newsGridItem.author
            time.text = newsGridItem.time
            Glide.with(img).load(newsGridItem.img[0])
                .apply(RequestOptions().centerCrop())
                .into(img)
        }
    }

    fun updateIntent(newsGridItem: NewsGridItem) {
        intent.putExtra(KEY_NEWS_PARCELABLE, newsGridItem)
        launch { initData(intent) }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            img.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            img.layoutParams.width = Utils.dp2px(345f).toInt()
        }
    }
}