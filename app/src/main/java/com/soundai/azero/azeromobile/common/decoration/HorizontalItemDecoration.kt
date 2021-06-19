package com.soundai.azero.azeromobile.common.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.utils.Utils

class HorizontalItemDecoration(
    val mHorizonDp: Float
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        parent.adapter?.run {
            val itemPosition = parent.getChildAdapterPosition(view)
            if (itemPosition < 0) return
            if (itemPosition >=  itemCount - 1) return
            outRect.set(0, 0, Utils.dp2px(mHorizonDp).toInt(), 0)
        }
    }
}