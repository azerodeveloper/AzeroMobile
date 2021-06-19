package com.soundai.azero.azeromobile.ui

import android.animation.ValueAnimator.INFINITE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ProgressBar
import com.soundai.azero.azeromobile.calculateTotalSpan
import com.soundai.azero.azeromobile.ui.activity.launcher.item.IGridItem

/**
 * Create by xingw on 2019/10/29
 */
class LoadMoreWrapper(
    val adapter: RecycleAdapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 脚布局
    private val TYPE_FOOTER = 2
    //空布局
    private val TYPE_EMPTY = 3
    // 当前加载状态，默认为加载完成
    private var loadState = 2
    // 正在加载
    val LOADING = 1
    // 加载完成
    val LOADING_COMPLETE = 2
    // 加载到底
    val LOADING_END = 3

    override fun getItemViewType(position: Int): Int {
        //最后一个item设置为FooterView
        return if (adapter.itemCount == 0) {
            TYPE_EMPTY
        } else if (position + 1 == itemCount) {
            TYPE_FOOTER
        } else {
            adapter.getItemViewType(position + 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_FOOTER) {
            return FootViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_refresh_footer,
                    parent,
                    false
                )
            )
        } else if (viewType == TYPE_EMPTY) {
            return EmptyViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.view_empty_remind,
                    parent,
                    false
                )
            )
        } else {
            return adapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FootViewHolder) {
            when (loadState) {
                //开始加载
                LOADING -> {
                    holder.pbLoading.visibility = View.VISIBLE
                    holder.tvLoading.visibility = View.VISIBLE
//                    holder.llEnd.visibility = View.GONE
                }
                //加载完成
                LOADING_COMPLETE -> {
                    holder.pbLoading.visibility = View.INVISIBLE
                    holder.tvLoading.visibility = View.INVISIBLE
//                    holder.llEnd.visibility = View.GONE
                }
                //加载到底
                LOADING_END -> {
                    holder.pbLoading.visibility = View.GONE
                    holder.tvLoading.visibility = View.GONE
//                    holder.llEnd.visibility = View.VISIBLE
                }
            }
        } else if (holder is EmptyViewHolder) {
            //Empty
        } else {
            adapter.onBindViewHolder(holder, position + 1)
        }
    }

    override fun getItemCount(): Int {
        return if (adapter.itemCount == 0) {
            1
        } else {
            adapter.itemCount
        }
    }

    private inner class FootViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        internal var pbLoading: ProgressBar = itemView.findViewById(R.id.pb_loading)
        internal var tvLoading: TextView = itemView.findViewById(R.id.tv_loading)
        internal var llEnd: LinearLayout = itemView.findViewById(R.id.ll_end)
    }

    private inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val ivLoading = itemView.findViewById<ImageView>(R.id.iv_loading)

        init {
            val rotationAnimate = RotateAnimation(0f, 360f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            rotationAnimate.repeatCount =  INFINITE
            rotationAnimate.duration = 1000
            rotationAnimate.interpolator = LinearInterpolator()
//            ivLoading.startAnimation(rotationAnimate)
        }
    }

    /**
     * 设置上拉加载状态
     *
     * @param loadState 0.正在加载 1.加载完成 2.加载到底
     */
    fun setLoadState(loadState: Int) {
        this.loadState = loadState
        notifyDataSetChanged()
    }

    var datas: MutableList<IGridItem>
        set(value) {
            adapter.dataList = value
        }
        get() = adapter.dataList
}