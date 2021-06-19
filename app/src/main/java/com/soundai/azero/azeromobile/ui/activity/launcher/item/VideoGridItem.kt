package com.soundai.azero.azeromobile.ui.activity.launcher.item

import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.Setting

/**
 * Create by xingw on 2019/10/26
 */
class VideoGridItem(
    val title:String,
    val size: SIZE,
    val img: String
) : IGridItem() {
    override val layoutResId: Int = when (size) {
        SIZE.SMALL -> R.layout.grid_video_small_item
        SIZE.BIG -> R.layout.grid_video_big_item
    }

    override fun getSpanSize(): Int = when (size) {
        SIZE.SMALL -> Setting.LAUNCHER_SPANSIZE / 3
        SIZE.BIG -> Setting.LAUNCHER_SPANSIZE
    }

    enum class SIZE {
        SMALL,
        BIG
    }
}