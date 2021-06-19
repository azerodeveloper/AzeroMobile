package com.soundai.azero.azeromobile.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView

/**
 * 完全去除默认边距的TextView
 */
class NoPaddingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    private val textPaint by lazy {
        val paint = TextPaint()
        paint.isAntiAlias = true
        paint
    }
    private val rect by lazy { Rect() }
    private var layoutWidth = -1

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        layout?.let {
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val content = if (TextUtils.isEmpty(text)) "" else text.toString()
            textPaint.getTextBounds(content, 0, content.length, rect)
            textPaint.textSize = textSize
            textPaint.color = currentTextColor
            if (layoutWidth < 0) {
                layoutWidth = it.width
            }
            val width = if (widthMode == MeasureSpec.EXACTLY) {
                widthSize
            } else {
                layoutWidth
            }
            val height = if (heightMode == MeasureSpec.EXACTLY) {
                heightSize
            } else {
                val lineHeight = rect.bottom - rect.top
                if (it.lineCount > 1) {
                    lineHeight * it.lineCount + (lineSpacingExtra * lineSpacingMultiplier * (it.lineCount - 1)).toInt()
                } else {
                    lineHeight * it.lineCount
                }
            }
            setMeasuredDimension(width, height)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        layoutWidth = -1
        val lineHeight = rect.bottom - rect.top
        val lineSpace: Float = lineSpacingExtra * lineSpacingMultiplier
        for (i in 0 until layout.lineCount) {
            canvas?.drawText(
                text.subSequence(layout.getLineStart(i), layout.getLineEnd(i)).toString(),
                0f,
                (lineHeight + lineSpace) * i - rect.top,
                textPaint
            )
        }
    }
}