package com.soundai.azero.azeromobile.utils

import android.text.TextUtils
import android.util.Log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.Alarm
import com.soundai.azero.azeromobile.common.bean.weather.Now
import com.soundai.azero.azeromobile.ui.widget.weather.AirQualityTextView


object WeatherUtils {
    private const val TAG = "WeatherUtils"

    fun getTemperatureRange(
        low: String?,
        high: String?,
        split: String = "~",
        lowFirst: Boolean = true
    ): String? {
        if (TextUtils.isEmpty(low) || TextUtils.isEmpty(high)) {
            return null
        }
        return if (lowFirst) "${low}${split}${high}°" else "${high}${split}${low}°"

    }

    fun getCurrentWeather(now: Now, alarms: List<Alarm>?): String? {
        var currentWeather: String? = null
        if (!alarms.isNullOrEmpty()) {
            for (alarm in alarms) {
                currentWeather =
                    convertWeatherName(alarm.type)
            }
        }
        if (TextUtils.isEmpty(currentWeather)) {
            currentWeather =
                convertWeatherName(now.text)
        }
        return currentWeather
    }

    fun getWeather(day: String?, night: String?): String? {
        return if (!TextUtils.isEmpty(day) && !TextUtils.isEmpty(night)) {
            if (day.equals(night)) {
                day
            } else {
                "${day}转${night}"
            }
        } else if (!TextUtils.isEmpty(day)) {
            day
        } else if (!TextUtils.isEmpty(night)) {
            night
        } else {
            null
        }
    }

    fun getBackgroundId(type: String?): Int? {
        if (type == null) {
            Log.e(TAG, "get bg id failed, weather type is null")
            return null
        }
        return when {
            type.contains("晴") -> {
                R.drawable.tq_bg_sunny
            }
            type.contains("云") -> {
                R.drawable.tq_bg_cloudy
            }
            type.contains("阴") -> {
                R.drawable.tq_bg_overcast
            }
            type.contains("雨") -> {
                R.drawable.tq_bg_rain
            }
            type.contains("雪") -> {
                R.drawable.tq_bg_snow
            }
            type.contains("冰") -> {
                R.drawable.tq_bg_hail
            }
            type.contains("沙") || type.contains("尘") -> {
                R.drawable.tq_bg_sand
            }
            type.contains("雾") -> {
                R.drawable.tq_bg_foggy
            }
            type.contains("霾") -> {
                R.drawable.tq_bg_smog
            }
            type.contains("雪") -> {
                R.drawable.tq_bg_cloudy
            }
            else -> {
                Log.e(TAG, "get bg id failed, unknown type: $type")
                R.drawable.tq_bg_sunny
            }
        }
    }

    fun getAirQuality(quality: String?): AirQualityTextView.QualityLevel {
        return when (quality) {
            "优" -> AirQualityTextView.QualityLevel.EXCELLENT
            "良" -> AirQualityTextView.QualityLevel.GOOD
            "轻度污染" -> AirQualityTextView.QualityLevel.LIGHT
            "中度污染" -> AirQualityTextView.QualityLevel.MEDIUM
            "重度污染", "严重污染" -> AirQualityTextView.QualityLevel.SERVER
            else -> AirQualityTextView.QualityLevel.EMPTY
        }
    }

    fun convertWeatherName(type: String?): String? {
        if (type == null) {
            return null
        }
        val context = TaApp.application
        return when {
            type.contains("晴") -> {
                return context.getString(R.string.weather_type_sunny)
            }
            type.contains("云") -> {
                return context.getString(R.string.weather_type_cloudy)
            }
            type.contains("阴") -> {
                return context.getString(R.string.weather_type_overcast)
            }
            type.contains("雨") -> {
                return context.getString(R.string.weather_type_rain)
            }
            type.contains("雪") -> {
                return context.getString(R.string.weather_type_snow)
            }
            type.contains("冰") -> {
                return context.getString(R.string.weather_type_hail)
            }
            type.contains("沙") || type.contains("尘") -> {
                return context.getString(R.string.weather_type_sand)
            }
            type.contains("雾") -> {
                return context.getString(R.string.weather_type_foggy)
            }
            type.contains("霾") -> {
                return context.getString(R.string.weather_type_smog)
            }
            type.contains("雪") -> {
                return  context.getString(R.string.weather_type_cloudy)
            }
            else -> {
                null
            }
        }
    }

    fun isMultiDate(date: String): Boolean {
        return date.contains("/")
    }

    fun getWeatherIconId(type: String?): Int? {
        if (type == null) {
            Log.e(TAG, "get icon id failed, weather type is null")
            return null
        }
        return when {
            type.contains("晴") -> {
                R.drawable.tq_icon_sunny
            }
            type.contains("云") -> {
                R.drawable.tq_icon_cloudy
            }
            type.contains("阴") -> {
                R.drawable.tq_icon_overcast
            }
            type.contains("雨") -> {
                R.drawable.tq_icon_rain
            }
            type.contains("雪") -> {
                R.drawable.tq_icon_snow
            }
            type.contains("冰") -> {
                R.drawable.tq_icon_hail
            }
            type.contains("沙") || type.contains("尘") -> {
                R.drawable.tq_icon_sand
            }
            type.contains("雾") -> {
                R.drawable.tq_icon_foggy
            }
            type.contains("霾") -> {
                R.drawable.tq_icon_smog
            }
            type.contains("雪") -> {
                R.drawable.tq_icon_cloudy
            }
            else -> {
                Log.e(TAG, "get icon id failed, unknown type: $type")
                R.drawable.tq_bg_sunny
            }
        }
    }

    fun getMultiDate(date: String): Array<String>? {
        return if (date.contains("/")) {
            date.split("/").toTypedArray()
        } else {
            null
        }
    }
}