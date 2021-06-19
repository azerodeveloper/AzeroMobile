package com.soundai.azero.azeromobile.ui.activity.template.weather.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.Air
import com.soundai.azero.azeromobile.common.bean.weather.Alarm
import com.soundai.azero.azeromobile.common.bean.weather.Daily
import com.soundai.azero.azeromobile.common.bean.weather.Now
import com.soundai.azero.azeromobile.ui.widget.weather.AirQualityTextView
import com.soundai.azero.azeromobile.utils.DateUtils
import com.soundai.azero.azeromobile.utils.WeatherUtils

class WeatherLandAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dailyList: List<Daily> = listOf()
    var airList: List<Air> = listOf()
    var alarmsList: List<Alarm>? = null
    var now: Now? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_weather_land_main -> MainViewHolder(view)
            else -> NormalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MainViewHolder -> holder.bind()
            is NormalViewHolder -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        return dailyList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) R.layout.item_weather_land_main else R.layout.item_weather_land_normal
    }

    private inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val realTimeTag: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_real_time) }
        val lunarDate: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_lunar_date) }
        val date: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_date) }
        val temperature: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_temperature) }
        val weather: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_weather) }
        val weatherBottom: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_weather_bottom) }
        val temperatureRange: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_temperature_range) }
        val humidity: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_humidity) }
        val wind: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_wind) }
        val airQuality: AirQualityTextView by lazy { itemView.findViewById<AirQualityTextView>(R.id.tv_weather_land_item_air) }
        val humidityGroup: Group by lazy { itemView.findViewById<Group>(R.id.gp_weather_land_item_humidity) }

        @SuppressLint("SetTextI18n")
        fun bind() {
            val daily = dailyList[0]
            if (daily.date != null) {
                lunarDate.text = DateUtils.convertDateFormat(daily.date, "MM月dd日")
                date.text = DateUtils.getWeek(daily.date)
            } else {
                lunarDate.text = null
                date.text = null
            }
            temperatureRange.text = WeatherUtils.getTemperatureRange(daily.low, daily.high)
            var quality: String? = null
            for (air in airList) {
                if (daily.date == air.date) {
                    quality = air.quality
                    break
                }
            }
            airQuality.setQualityLevel(WeatherUtils.getAirQuality(quality))
            val nowInfo = now
            if (nowInfo != null) {
                realTimeTag.visibility = View.VISIBLE
                humidityGroup.visibility = View.VISIBLE
                temperature.text = "${nowInfo.temperature}°"
                temperature.textSize = 74f
                weather.visibility = View.VISIBLE
                weatherBottom.visibility = View.GONE
                weather.text = WeatherUtils.getCurrentWeather(nowInfo, alarmsList)
                humidity.text = nowInfo.humidity
                wind.text = "${nowInfo.windDirection}；${nowInfo.windScale}级"
            } else {
                realTimeTag.visibility = View.GONE
                humidityGroup.visibility = View.GONE
                temperature.text =
                    WeatherUtils.getTemperatureRange(daily.low, daily.high, "/", false)
                temperature.textSize = 50f
                val weatherStr = WeatherUtils.getWeather(daily.textDay, daily.textNight)
                if (weatherStr != null) {
                    if (weatherStr.length > 2) {
                        weather.visibility = View.GONE
                        weatherBottom.visibility = View.VISIBLE
                        weatherBottom.text = weatherStr
                    } else {
                        weather.visibility = View.VISIBLE
                        weatherBottom.visibility = View.GONE
                        weather.text = weatherStr
                    }
                } else {
                    weather.visibility = View.GONE
                    weatherBottom.visibility = View.GONE
                }
                weather.textSize = 15f
                wind.text = "${daily.windDirection}风；${daily.windScale}级"
            }
        }
    }


    private inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val week: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_week) }
        val date: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_date) }
        val weather: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_weather) }
        val temperatureRange: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_temperature_range) }
        val humidity: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_humidity) }
        val windDirection: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_wind_direction) }
        val windScale: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_land_item_wind_scale) }
        val airQuality: AirQualityTextView by lazy { itemView.findViewById<AirQualityTextView>(R.id.tv_weather_land_item_air) }
        val iconDay: ImageView by lazy { itemView.findViewById<ImageView>(R.id.iv_weather_land_item_day_icon) }
        val iconNight: ImageView by lazy { itemView.findViewById<ImageView>(R.id.iv_weather_land_item_night_icon) }
        val humidityGroup: Group by lazy { itemView.findViewById<Group>(R.id.gp_weather_land_item_humidity) }

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            if (now == null) {
                humidityGroup.visibility = View.GONE
            } else {
                humidityGroup.visibility = View.VISIBLE
                humidity.text = "-"
            }
            val daily = dailyList[position]
            if (daily.date != null) {
                week.text = DateUtils.getWeek(daily.date)
                date.text = DateUtils.convertDateFormat(daily.date, "MM月dd日")
            } else {
                week.text = null
                date.text = null
            }
            weather.text = WeatherUtils.getWeather(daily.textDay, daily.textNight)
            temperatureRange.text = WeatherUtils.getTemperatureRange(daily.low, daily.high)
            windDirection.text = "${daily.windDirection}风"
            windScale.text = "${daily.windScale}级"
            val dayIcon = WeatherUtils.getWeatherIconId(daily.textDay)
            if (dayIcon != null) {
                iconDay.setImageDrawable(TaApp.application.getDrawable(dayIcon))
            } else {
                iconDay.setImageDrawable(null)
            }
            if (!daily.textDay.equals(daily.textNight)) {
                val nightIcon = WeatherUtils.getWeatherIconId(daily.textNight)
                if (nightIcon != null) {
                    iconNight.visibility = View.VISIBLE
                    iconNight.setImageDrawable(TaApp.application.getDrawable(nightIcon))
                } else {
                    iconNight.visibility = View.GONE
                }
            } else {
                iconNight.visibility = View.GONE
            }
            var quality: String? = null
            for (air in airList) {
                if (daily.date == air.date) {
                    quality = air.quality
                    break
                }
            }
            airQuality.setQualityLevel(WeatherUtils.getAirQuality(quality))
        }
    }

}