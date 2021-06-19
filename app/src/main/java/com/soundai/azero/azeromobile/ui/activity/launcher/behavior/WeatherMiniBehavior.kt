package com.soundai.azero.azeromobile.ui.activity.launcher.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView

class WeatherMiniBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    private var deltaY: Float = 0f

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is RecyclerView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (deltaY == 0f) deltaY = getDistanceHeight(dependency, child)
        val dy = if (getDistanceHeight(dependency, child) < 0) {
            0f
        } else {
            getDistanceHeight(dependency, child)
        }
        val y = -(dy / deltaY) * child.height
        child.translationY = y
        val alpha = 1 - dy / deltaY
        child.alpha = alpha
        return true
    }

    private fun getDistanceHeight(dependency: View, child: View) =
        dependency.y - child.height
}