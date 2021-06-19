package com.soundai.azero.azeromobile.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.cos


class WaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private val WAVE_COLOR_LIST = listOf("#3b77ef", "#2a91c6", "#7884dc")
    }

    private var isPlaying = false

    private val paint by lazy {
        val p = Paint()
        p.isAntiAlias = true
        p
    }

    private val waveList by lazy {
        val list = ArrayList<Wave>()
        for (i in 0..10) {
            list.add(Wave())
        }
        list
    }

    private val line by lazy { Path() }

    fun start() {
        isPlaying = true
        waveList.forEach { it.startAnimator() }
        postInvalidate()
    }

    fun stop() {
        isPlaying = false
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            canvas.translate(measuredWidth / 2f, measuredHeight / 2f)
            line.moveTo(-measuredWidth / 2f, 0f)
            line.lineTo(measuredWidth / 2f, 0f)
            paint.color = Color.WHITE
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            drawPath(line, paint)
            var waveStop = true
            with(waveList) {
                forEach {
                    if (!it.animatorStopped) {
                        waveStop = false
                        return@with
                    }
                }
            }
            if (!waveStop) {
                waveList.forEach { it.draw(canvas, paint) }
                postInvalidateDelayed(10)
            }
        }
    }

    private inner class Wave {
        var position = 0
        var waveWidth = 0
            set(value) {
                field = value
                step = value / 6.28f
            }
        var maxHeight = 0
        var waveHeight = 0
        var step = 0f
        var color = Color.WHITE
        var duration = 0L
        var animatorStopped = true

        val pathAbove by lazy { Path() }
        val pathBelow by lazy { Path() }
        val upAnimator: ValueAnimator by lazy {
            val a = ValueAnimator.ofInt(0, maxHeight)
            a.interpolator = AccelerateInterpolator()
            a.addUpdateListener {
                waveHeight = it.animatedValue as Int
            }
            a
        }
        val downAnimator: ValueAnimator by lazy {
            val a = ValueAnimator.ofInt(maxHeight, 0)
            a.interpolator = DecelerateInterpolator()
            a.addUpdateListener {
                waveHeight = it.animatedValue as Int
            }
            a
        }


        fun draw(canvas: Canvas, paint: Paint) {
            pathAbove.reset()
            pathBelow.reset()
            pathAbove.moveTo(position - waveWidth / 2f, 0f)
            pathBelow.moveTo(position - waveWidth / 2f, 0f)
            var x = -3.14f
            while (x <= 3.14f) {
                val y = calculateY(x)
                pathAbove.lineTo(position + x * step, y * waveHeight)
                pathBelow.lineTo(position + x * step, -y * waveHeight)
                x += 0.01f
            }
            paint.color = color
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
            paint.style = Paint.Style.FILL
            canvas.drawPath(pathAbove, paint)
            canvas.drawPath(pathBelow, paint)
        }

        fun calculateY(x: Float): Float {
            return (cos(x) + 1) / 2
        }

        fun regenerate() {
            color = Color.parseColor(WAVE_COLOR_LIST[(WAVE_COLOR_LIST.indices).random()])
            waveWidth = ((measuredWidth / 3)..(measuredWidth / 2)).random()
            val twoThirdWidth = measuredWidth / 3
            position = (-twoThirdWidth..twoThirdWidth).random()
            maxHeight = (10..40).random()
            duration = (300..500).random().toLong()
        }

        fun startAnimator() {
            animatorStopped = false
            regenerate()

            upAnimator.setIntValues(0, maxHeight)
            upAnimator.duration = duration
            upAnimator.startDelay = (200..500).random().toLong()

            downAnimator.setIntValues(maxHeight, 0)
            downAnimator.duration = duration
            downAnimator.startDelay = (100..200).random().toLong()

            upAnimator.doOnEnd {
                downAnimator.start()
            }
            downAnimator.doOnEnd {
                if (isPlaying) {
                    animatorStopped = false
                    regenerate()
                    upAnimator.setIntValues(0, maxHeight)
                    upAnimator.duration = duration
                    upAnimator.startDelay = 0

                    downAnimator.setIntValues(maxHeight, 0)
                    downAnimator.duration = duration
                    downAnimator.startDelay = 0

                    upAnimator.start()
                } else {
                    animatorStopped = true
                }
            }
            upAnimator.start()
        }
    }
}