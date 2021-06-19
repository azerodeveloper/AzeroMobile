package com.soundai.azero.azeromobile.ui.activity.template.weather.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.Air
import com.soundai.azero.azeromobile.common.bean.weather.Daily
import com.soundai.azero.azeromobile.ui.widget.weather.AirQualityTextView
import com.soundai.azero.azeromobile.utils.DateUtils
import com.soundai.azero.azeromobile.utils.WeatherUtils

class WeatherPortraitAdapter : RecyclerView.Adapter<WeatherPortraitAdapter.ViewHolder>() {
    var dailyList: List<Daily> = listOf()
    var airList: List<Air> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_portrait, parent, false)
        return ViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val daily = dailyList[position]
        if (daily.date != null) {
            holder.week.text = DateUtils.getWeek(daily.date)
            holder.date.text = DateUtils.convertDateFormat(daily.date, "MM月dd日")
        } else {
            holder.week.text = null
            holder.date.text = null
        }
        holder.temperatureRange.text = WeatherUtils.getTemperatureRange(daily.low, daily.high)
        holder.weather.text = WeatherUtils.getWeather(daily.textDay, daily.textNight)
        holder.windDirection.text = "${daily.windDirection}风"
        holder.windScale.text = "${daily.windScale}级"
        val dayIcon = WeatherUtils.getWeatherIconId(daily.textDay)
        if (dayIcon != null) {
            holder.iconDay.setImageDrawable(TaApp.application.getDrawable(dayIcon))
        } else {
            holder.iconDay.setImageDrawable(null)
        }
        if (!daily.textDay.equals(daily.textNight)) {
            val nightIcon = WeatherUtils.getWeatherIconId(daily.textNight)
            if (nightIcon != null) {
                holder.iconNight.visibility = View.VISIBLE
                holder.iconNight.setImageDrawable(TaApp.application.getDrawable(nightIcon))
            } else {
                holder.iconNight.visibility = View.GONE
            }
        } else {
            holder.iconNight.visibility = View.GONE
        }

        var quality: String? = null
        for (air in airList) {
            if (daily.date == air.date) {
                quality = air.quality
                break
            }
        }
        holder.airQuality.setQualityLevel(WeatherUtils.getAirQuality(quality))
    }

    override fun getItemCount(): Int {
        return dailyList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val week: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_item_week) }
        val date: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_item_date) }
        val temperatureRange: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_item_temperature_range) }
        val weather: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_item_weather) }
        val windDirection: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_item_wind_direction) }
        val windScale: TextView by lazy { itemView.findViewById<TextView>(R.id.tv_weather_item_wind_scale) }
        val airQuality: AirQualityTextView by lazy { itemView.findViewById<AirQualityTextView>(R.id.tv_weather_item_air) }
        val iconDay: ImageView by lazy { itemView.findViewById<ImageView>(R.id.iv_weather_item_day_icon) }
        val iconNight: ImageView by lazy { itemView.findViewById<ImageView>(R.id.iv_weather_item_night_icon) }
    }

}