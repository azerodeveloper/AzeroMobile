package com.soundai.azero.azeromobile.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.activity.*
import com.soundai.azero.azeromobile.ui.activity.launcher.item.*
import java.lang.UnsupportedOperationException

/**
 * Create by xingw on 2019/10/26
 */
class RecycleAdapter(val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList: MutableList<IGridItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.grid_music_item -> MusicViewHolder(view)
            R.layout.grid_english_item -> EnglishViewHolder(view)
            R.layout.grid_news_single_img_item -> NewsViewHolder1(view)
            R.layout.grid_news_double_img_item -> NewsViewHolder2(view)
            R.layout.grid_video_small_item -> VideoViewHolder(view)
            R.layout.grid_video_big_item -> VideoViewHolder(view)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        when (holder) {
            is MusicViewHolder -> holder.bind(data as MusicGridItem, activity)
            is EnglishViewHolder -> holder.bind(data as EnglishGridItem, activity)
            is NewsViewHolder1 -> holder.bind(data as NewsGridItem, activity)
            is NewsViewHolder2 -> holder.bind(data as NewsGridItem, activity)
            is VideoViewHolder -> holder.bind(data as VideoGridItem, activity)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].layoutResId
    }

    abstract class ViewHolder<T : IGridItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(iGridItem: T, activity: Activity) {
            onBind(iGridItem, activity)
        }

        protected abstract fun onBind(
            iGridItem: T,
            activity: Activity
        )
    }

    class DiffCallback(
        val oldList: MutableList<IGridItem>,
        val newList: MutableList<IGridItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return checkDiff(oldItem, newItem)
        }

        private fun checkDiff(
            oldItem: IGridItem,
            newItem: IGridItem
        ): Boolean {
            if (newItem.focus != oldItem.focus) return false
            if (oldItem is NewsGridItem && newItem is NewsGridItem) {
                return oldItem.title == newItem.title && oldItem.serial == newItem.serial
            } else if (oldItem is MusicGridItem && newItem is MusicGridItem) {
                return oldItem.title == newItem.title && oldItem.serial == newItem.serial
            } else if (oldItem is VideoGridItem && newItem is VideoGridItem) {
                return oldItem.title == newItem.title && oldItem.serial == newItem.serial
            } else if (oldItem is EnglishGridItem && newItem is EnglishGridItem) {
                return oldItem.title == newItem.title && oldItem.serial == newItem.serial
            }
            return false
        }
    }
}