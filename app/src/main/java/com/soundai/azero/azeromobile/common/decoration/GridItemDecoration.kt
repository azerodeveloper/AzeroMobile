package com.soundai.azero.azeromobile.common.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.azero.sdk.util.log

/**
 * Create by xingw on 2019/10/29
 * 绘制分割线
 */
class GridItemDecoration(
    val mHorizonSpan: Int,
    val mVerticalSpan: Int,
    val color: Int,
    val showLastList: Boolean
) : RecyclerView.ItemDecoration() {
    private val mDivider = ColorDrawable(color)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        // val childCount = parent.childCount
        // for (i in 0 until childCount) {
        //     val child = parent.getChildAt(i)
        //
        //     if (!showLastList && (i == childCount - 1)) continue
        //     val params = child.layoutParams as RecyclerView.LayoutParams
        //     val left = child.left - params.leftMargin
        //     val right = child.right + params.rightMargin
        //     val top = child.bottom + params.topMargin
        //     val bottom = top + mHorizonSpan
        //
        //     mDivider.setBounds(left, top, right, bottom)
        //     mDivider.draw(c)
        // }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spanCount = getSpanCount(parent)
        val childCount = parent.childCount
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition

        if (itemPosition < 0) return
        val column = itemPosition % spanCount
        var bottom = mHorizonSpan
        val left = column * mVerticalSpan / spanCount
        val right = mVerticalSpan - (column + 1) * mVerticalSpan / spanCount

        outRect.set(left, 0, right, bottom)
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        // val childCount = parent.childCount
        // for (i in 0 until childCount) {
        //     val child = parent.getChildAt(i)
        //     if ((parent.getChildViewHolder(child).adapterPosition + 1) % getSpanCount(parent) == 0) {
        //         continue
        //     }
        //     val params = child.layoutParams as RecyclerView.LayoutParams
        //     val top = child.top - params.topMargin
        //     val bottom = child.bottom + params.bottomMargin + mHorizonSpan
        //     val left = child.right + params.rightMargin
        //     var right = left + mVerticalSpan
        //     //            //满足条件( 最后一行 && 不绘制 ) 将vertical多出的一部分去掉;
        //     if (i == childCount - 1) {
        //         right -= mVerticalSpan
        //     }
        //     mDivider.setBounds(left, top, right, bottom)
        //     mDivider.draw(c)
        // }
    }

    /**
     * 获取列数
     */
    private fun getSpanCount(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            return layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            return layoutManager.spanCount
        } else if (layoutManager is SpannedGridLayoutManager) {
            return layoutManager.spans
        }
        return -1
    }
}