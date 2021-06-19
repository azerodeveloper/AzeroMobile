package com.soundai.azero.azeromobile.manager

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R

class ScaleGridLayoutManager(
    context: Context?,
    spanCount: Int,
    orientation: Int,
    reverseLayout: Boolean
) : GridLayoutManager(context, spanCount, orientation, reverseLayout) {
    private val currentFraction = 0.5f

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        repeat(childCount) {
            getChildAt(it)?.apply {
                scaleView(this)
            }
        }
    }

    private fun scaleView(itemView: View) {
        itemView.findViewById<ConstraintLayout>(R.id.cl_bg).scaleX = currentFraction
        itemView.findViewById<ConstraintLayout>(R.id.cl_bg).scaleY = currentFraction
    }
}