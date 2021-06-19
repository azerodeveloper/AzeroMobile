package com.soundai.azero.azeromobile.ui.activity.base.activity

import android.animation.ObjectAnimator
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.widget.ASRDialog
import com.soundai.azero.azeromobile.utils.screenWidth
import kotlinx.coroutines.launch
import kotlin.math.abs

abstract class BaseSwipeActivity : BaseActivity() {
    private val touchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }
    private val lastPoint by lazy { PointF() }
    private val windowWidth by lazy { screenWidth }
    private var isFirst = false
    private var swiping = false
    private var offsetX = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.anim_right_in, 0)
    }

    override fun finish() {
        launch {
            super.finish()
            overridePendingTransition(0, R.anim.anim_left_out)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.x > windowWidth / 2 && !swiping) {
            isFirst = false
            return super.dispatchTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isFirst = true
                swiping = false
                lastPoint.set(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                val changeX = event.x - lastPoint.x
                val changeY = event.y - lastPoint.y
                if (isFirst && abs(changeX) > touchSlop && abs(changeY) > abs(changeX) * 1.5) {
                    isFirst = false
                }
                if (isFirst && abs(changeX) > touchSlop && abs(changeX) > abs(changeY) * 1.5) {
                    swiping = true
                }
                if (swiping) {
                    offsetX += changeX
                    if (offsetX < 0) offsetX = 0f
                    window.decorView.translationX = offsetX
                    lastPoint.set(event.x, event.y)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (swiping) {
                    if (offsetX >= windowWidth / 3) {
                        startAnim(true)
                    } else {
                        startAnim(false)
                    }
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun startAnim(exit: Boolean) {
        ObjectAnimator().run {
            if (exit) {
                interpolator = AccelerateInterpolator()
                setFloatValues(offsetX, windowWidth.toFloat())
                doOnEnd { finish() }
            } else {
                interpolator = DecelerateInterpolator()
                setFloatValues(offsetX, 0f)
                doOnEnd { offsetX = 0f }
            }
            duration = (200 * (offsetX / windowWidth)).toLong()
            addUpdateListener { animation ->
                offsetX = animation.animatedValue as Float
                window.decorView.translationX = offsetX
            }
            start()
        }
    }

    override fun onStart() {
        super.onStart()
        ASRDialog.show()
    }
}