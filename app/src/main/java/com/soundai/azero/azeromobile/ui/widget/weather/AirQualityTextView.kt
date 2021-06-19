package com.soundai.azero.azeromobile.ui.widget.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.utils.Utils

class AirQualityTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val paint by lazy {
        val field = Paint()
        field.isAntiAlias = true
        field
    }

    private var startColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getColor(R.color.weather_air_excellent_start)
    } else {
        context.resources.getColor(R.color.weather_air_excellent_start)
    }
    private var endColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getColor(R.color.weather_air_excellent_end)
    } else {
        context.resources.getColor(R.color.weather_air_excellent_end)
    }

    fun setQualityLevel(qualityLevel: QualityLevel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startColor = context.getColor(qualityLevel.startColorId)
            endColor = context.getColor(qualityLevel.endColorId)
        } else {
            startColor = context.resources.getColor(qualityLevel.startColorId)
            endColor = context.resources.getColor(qualityLevel.endColorId)
        }
        text = qualityLevel.value
    }

    init {
        minWidth = Utils.dp2px(40f).toInt()
        textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val width = measuredWidth.toFloat()
            val height = measuredHeight.toFloat()
            paint.shader =
                LinearGradient(0f, 0f, width, 0f, startColor, endColor, Shader.TileMode.CLAMP)
            it.drawRoundRect(0f, 0f, width, height, height / 2, height / 2, paint)
        }
        super.onDraw(canvas)
    }

    sealed class QualityLevel(val value: String, val startColorId: Int, val endColorId: Int) {
        object EXCELLENT :
            QualityLevel(
                TaApp.application.getString(R.string.weather_air_excellent),
                R.color.weather_air_excellent_start,
                R.color.weather_air_excellent_end
            )

        object GOOD : QualityLevel(
            TaApp.application.getString(R.string.weather_air_good),
            R.color.weather_air_good_start,
            R.color.weather_air_good_end
        )

        object LIGHT : QualityLevel(
            TaApp.application.getString(R.string.weather_air_light),
            R.color.weather_air_light_start,
            R.color.weather_air_light_end
        )

        object MEDIUM :
            QualityLevel(
                TaApp.application.getString(R.string.weather_air_medium),
                R.color.weather_air_medium_start,
                R.color.weather_air_medium_end
            )

        object SERVER :
            QualityLevel(
                TaApp.application.getString(R.string.weather_air_server),
                R.color.weather_air_server_start,
                R.color.weather_air_server_end
            )

        object EMPTY : QualityLevel(
            TaApp.application.getString(R.string.weather_air_empty),
            R.color.weather_air_excellent_start,
            R.color.weather_air_excellent_end
        )
    }
}