package com.soundai.azero.azeromobile.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import android.view.animation.GridLayoutAnimationController
import android.view.ViewGroup
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View


/**
 * Create by xingw on 2019/11/2
 */
class StaggeredGridRecyclerView(context: Context, attrs: AttributeSet?) :
    RecyclerView(context, attrs){
    /**
     * 支持GridLayoutManager以及StaggeredGridLayoutManager
     *
     * @param child
     * @param params
     * @param index
     * @param count
     */
    override fun attachLayoutAnimationParameters(
        child: View, params: ViewGroup.LayoutParams,
        index: Int, count: Int
    ) {
        val layoutManager = this.layoutManager
        if (adapter != null && (layoutManager is GridLayoutManager || layoutManager is StaggeredGridLayoutManager)) {

            var animationParams =
                params.layoutAnimationParameters as? GridLayoutAnimationController.AnimationParameters

            if (animationParams == null) {
                animationParams = GridLayoutAnimationController.AnimationParameters()
                params.layoutAnimationParameters = animationParams
            }

            var columns = 0
            if (layoutManager is GridLayoutManager) {
                columns = layoutManager.spanCount
            } else {
                columns = (layoutManager as StaggeredGridLayoutManager).spanCount
            }

            animationParams.count = count
            animationParams.index = index
            animationParams.columnsCount = columns
            animationParams.rowsCount = count / columns

            val invertedIndex = count - 1 - index
            animationParams.column = columns - 1 - invertedIndex % columns
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns

        } else {
            super.attachLayoutAnimationParameters(child, params, index, count)
        }
    }
}