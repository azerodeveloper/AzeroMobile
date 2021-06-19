package com.soundai.azero.azeromobile.ui.widget.guide

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class GuideSphereDotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint by lazy {
        val p = Paint()
        p.isAntiAlias = true
        p.style = Paint.Style.FILL
        p
    }

    var color: Int = Color.WHITE

    override fun onDraw(canvas: Canvas?) {
        paint.color = color
        canvas?.run {
            translate(measuredWidth / 2f, measuredHeight / 2f)
            val radius = min(measuredWidth, measuredHeight) / 4f
            drawCircle(0f, 0f, radius, paint)
            drawOval(
                -measuredWidth / 18f,
                -measuredHeight / 2f,
                measuredWidth / 18f,
                measuredHeight / 2f,
                paint
            )
            drawOval(
                -measuredWidth / 2f,
                -measuredHeight / 18f,
                measuredWidth / 2f,
                measuredHeight / 18f,
                paint
            )
            rotate(45f)
            drawOval(
                -measuredWidth / 15f,
                -measuredHeight / 2.5f,
                measuredWidth / 15f,
                measuredHeight / 2.5f,
                paint
            )
            drawOval(
                -measuredWidth / 2.5f,
                -measuredHeight / 15f,
                measuredWidth / 2.5f,
                measuredHeight / 15f,
                paint
            )
        }
    }
}