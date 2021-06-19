package com.soundai.azero.azeromobile.ui.activity.launcher.head

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.helloweather.HelloWeather
import com.soundai.azero.azeromobile.ui.widget.weather.AirQualityTextView
import com.soundai.azero.azeromobile.utils.Utils
import com.soundai.azero.azeromobile.utils.WeatherUtils

class HelloWeatherHeadView(private val helloWeather: HelloWeather) : IHeadView {
    @SuppressLint("SetTextI18n")
    override fun inflateHeadView(container: ViewGroup) {
        val activity = Utils.findActivity(container.context) ?: return
        val root = LayoutInflater.from(activity).inflate(R.layout.cardview_hello_weather, container)
        val week = root.findViewById<TextView>(R.id.tv_week)
        val date = root.findViewById<TextView>(R.id.tv_date)
        val temperature = root.findViewById<TextView>(R.id.tv_temperature)
        val weather = root.findViewById<TextView>(R.id.tv_weather)
        val airQuality = root.findViewById<AirQualityTextView>(R.id.tv_air_quality)
        val weatherIc = root.findViewById<ImageView>(R.id.ic_weather_icon)
        week.text = helloWeather.week
        date.text = helloWeather.date
        temperature.text = "${helloWeather.condition.temperature}°"
        weather.text = WeatherUtils.convertWeatherName(helloWeather.condition.text)
        airQuality.setQualityLevel(WeatherUtils.getAirQuality(helloWeather.air.quality))
        WeatherUtils.getWeatherIconId(helloWeather.condition.text)?.let {
            weatherIc.setImageDrawable(TaApp.application.getDrawable(it))
        }
    }

    override fun release() {
        // do nothing
    }
}