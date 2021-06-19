package com.soundai.azero.azeromobile.ui.activity.launcher.head

import android.view.ViewGroup

interface IHeadView {
    fun inflateHeadView(container: ViewGroup)
    fun release()
}