package com.soundai.azero.azeromobile.ui.activity.launcher.item

import kotlinx.android.parcel.Parcelize

/**
 * Create by xingw on 2019/10/26
 */
abstract class IGridItem {
    var serial: Int = 0
    var focus = false
    abstract val layoutResId: Int
    abstract fun getSpanSize(): Int
}