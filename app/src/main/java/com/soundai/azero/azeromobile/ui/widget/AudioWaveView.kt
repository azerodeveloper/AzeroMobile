package com.soundai.azero.azeromobile.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R

class AudioWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val size = 64
    }

    private var mWidth: Float
    private var mHeight: Float
    private var mPaint: Paint
    private var waveData: ByteArray = ByteArray(size)
    private var viewWidth: Float

    init {
        mWidth = width.toFloat()
        mHeight = height.toFloat()
        mPaint = Paint()
        viewWidth = mWidth / (size + 2)
        mPaint.color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.asr_content_wave_view_color, null)
        } else {
            resources.getColor(R.color.asr_content_wave_view_color)
        }

        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.strokeWidth = resources.getDimension(R.dimen.asr_content_wave_view_paint)
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(0f, mHeight / 2)
        for (i in 0 until size) {
            drawLump(canvas, i)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        viewWidth = mWidth / size
    }

    fun setWaveData(data: ByteArray, dataSize: Int) {
        synchronized(waveData) {
            this.waveData = readyData(data, dataSize)
        }
        invalidate()
    }

    private fun readyData(byteArray: ByteArray, dataSize: Int): ByteArray {
        synchronized(this) {

            val dataArray = ByteArray(dataSize / 2)
            for (index in dataArray.indices) {
                dataArray[index] = byteArray[2 * index + 1]
            }
            val resultDateArray = ByteArray(size)
            val int: Int = dataArray.size / size

            for (index in 0 until size) {
                val byte = dataArray[index * int]
                resultDateArray[index] = byte
            }
            return resultDateArray
        }
    }

    private fun drawLump(canvas: Canvas, i: Int) {
        val x = (i + 1) * viewWidth
        var y: Float = 0f;
        if (TaApp.denoise_tye == 0) {
            y = waveData[i] * mHeight / 256f
        } else {
            y = waveData[i] * waveData[i] * mHeight / 256 * 2f
        }
        canvas.drawLine(x, y, x, -y, mPaint)
    }
}