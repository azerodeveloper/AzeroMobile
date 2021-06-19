package com.soundai.azero.azeromobile.ui.widget.wheelview

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.Scroller
import com.soundai.azero.azeromobile.ui.widget.wheelview.WheelView.OnWheelChangedListener

class WheelScroller(context: Context?, val mWheelView: WheelView) :
    Scroller(context) {
    private var mScrollOffset = 0
    private var lastTouchY = 0f
    private var isScrolling = false
    private var mVelocityTracker: VelocityTracker? = null
    var onWheelChangedListener: OnWheelChangedListener? = null
    fun computeScroll() {
        if (isScrolling) {
            isScrolling = computeScrollOffset()
            doScroll(currY - mScrollOffset)
            if (isScrolling) {
                mWheelView.postInvalidate()
            } else { // 滚动结束后，重新调整位置
                justify()
            }
        }
    }

    var currentIndex = -1

    private fun doScroll(distance: Int) {
        mScrollOffset += distance
        if (!mWheelView.isCyclic) { // 限制滚动边界
            val maxOffset = (mWheelView.itemSize - 1) * mWheelView.mItemHeight
            if (mScrollOffset < 0) {
                mScrollOffset = 0
            } else if (mScrollOffset > maxOffset) {
                mScrollOffset = maxOffset
            }
        }
        notifyWheelChangedListener()
    }

    fun notifyWheelChangedListener() {
        val oldValue = currentIndex
        val newValue = getNewIndex()
        if (oldValue != newValue) {
            currentIndex = newValue
            if (onWheelChangedListener != null) {
                onWheelChangedListener!!.onChanged(
                    mWheelView,
                    oldValue,
                    newValue,
                    mWheelView.mEntries[newValue].toString()
                )
            }
        }
    }

    fun getNewIndex(): Int {
        val itemHeight = mWheelView.mItemHeight
        val itemSize = mWheelView.itemSize
        if (itemSize == 0) return -1
        val itemIndex: Int
        itemIndex = if (mScrollOffset < 0) {
            (mScrollOffset - itemHeight / 2) / itemHeight
        } else {
            (mScrollOffset + itemHeight / 2) / itemHeight
        }
        var currentIndex = itemIndex % itemSize
        if (currentIndex < 0) {
            currentIndex += itemSize
        }
        return currentIndex
    }

    fun setCurrentIndex(index: Int, animated: Boolean) {
        val position = index * mWheelView.mItemHeight
        val distance = position - mScrollOffset
        if (distance == 0) return
        if (animated) {
            isScrolling = true
            startScroll(0, mScrollOffset, 0, distance, JUSTIFY_DURATION)
            mWheelView.invalidate()
        } else {
            doScroll(distance)
            mWheelView.invalidate()
        }
    }

    val itemIndex: Int
        get() = if (mWheelView.mItemHeight == 0) 0 else mScrollOffset / mWheelView.mItemHeight

    val itemOffset: Int
        get() = if (mWheelView.mItemHeight == 0) 0 else mScrollOffset % mWheelView.mItemHeight

    fun reset() {
        isScrolling = false
        mScrollOffset = 0
        notifyWheelChangedListener()
        forceFinished(true)
    }

    /**
     * 当滚轮结束滑行后，调整滚轮的位置，需要调用该方法
     */
    fun justify() {
        val itemHeight = mWheelView.mItemHeight
        val offset = mScrollOffset % itemHeight
        if (offset > 0 && offset < itemHeight / 2) {
            isScrolling = true
            startScroll(0, mScrollOffset, 0, -offset, JUSTIFY_DURATION)
            mWheelView.invalidate()
        } else if (offset >= itemHeight / 2) {
            isScrolling = true
            startScroll(
                0,
                mScrollOffset,
                0,
                itemHeight - offset,
                JUSTIFY_DURATION
            )
            mWheelView.invalidate()
        } else if (offset < 0 && offset > -itemHeight / 2) {
            isScrolling = true
            startScroll(0, mScrollOffset, 0, -offset, JUSTIFY_DURATION)
            mWheelView.invalidate()
        } else if (offset <= -itemHeight / 2) {
            isScrolling = true
            startScroll(
                0,
                mScrollOffset,
                0,
                -itemHeight - offset,
                JUSTIFY_DURATION
            )
            mWheelView.invalidate()
        }
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                forceFinished(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val touchY = event.y
                val deltaY = (touchY - lastTouchY).toInt()
                if (deltaY != 0) {
                    doScroll(-deltaY)
                    mWheelView.invalidate()
                }
                lastTouchY = touchY
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker!!.computeCurrentVelocity(1000)
                val velocityY = mVelocityTracker!!.yVelocity
                if (Math.abs(velocityY) > 0) {
                    isScrolling = true
                    fling(
                        0,
                        mScrollOffset,
                        0,
                        (-velocityY).toInt(),
                        0,
                        0,
                        Int.MIN_VALUE,
                        Int.MAX_VALUE
                    )
                    mWheelView.invalidate()
                } else {
                    justify()
                }
                // 当触发抬起、取消事件后，回收VelocityTracker
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                }
            }
            MotionEvent.ACTION_CANCEL -> if (mVelocityTracker != null) {
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    companion object {
        const val JUSTIFY_DURATION = 400
    }

}