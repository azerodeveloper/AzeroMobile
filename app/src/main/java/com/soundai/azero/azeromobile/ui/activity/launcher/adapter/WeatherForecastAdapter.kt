package com.soundai.azero.azeromobile.ui.activity.launcher.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.WeatherForecastItem
import com.soundai.azero.azeromobile.utils.Utils
import java.lang.UnsupportedOperationException

class WeatherForecastAdapter :
    RecyclerView.Adapter<WeatherForecastAdapter.WeatherViewHolder>() {
    var dataList: List<WeatherForecastItem?> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.recycleview_weather_big -> WeatherBigWeatherViewHolder(view)
            R.layout.recycleview_weather_small -> WeatherSmallWeatherViewHolder(view)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        if (position < dataList.size) {
            val data = dataList[position]
            holder.bind(data)
        } else {
            holder.bind(null)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            R.layout.recycleview_weather_big
        } else {
            R.layout.recycleview_weather_small
        }
    }

    abstract class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val temperature: TextView = itemView.findViewById(R.id.tv_temperature)
        protected val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(weather: WeatherForecastItem?) {
            onBind(weather)
        }

        protected abstract fun onBind(
            weather: WeatherForecastItem?
        )
    }
}

class WeatherBigWeatherViewHolder(itemView: View) :
    WeatherForecastAdapter.WeatherViewHolder(itemView) {
    private val titleTemperature: TextView = itemView.findViewById(R.id.tv_title_temperature)

    @SuppressLint("SetTextI18n")
    override fun onBind(weather: WeatherForecastItem?) {
        weather?.let {
            val highTemperature = weather.highTemperature?.toInt() ?: 0
            val lowTemperature = weather.lowTemperature?.toInt() ?: 0

            temperature.text = "$lowTemperature ~ $highTemperature℃"
            tvDate.text = Utils.getDate(weather.date?:"")
            titleTemperature.text = ((highTemperature + lowTemperature) / 2).toString()+"°"
        }
    }
}

class WeatherSmallWeatherViewHolder(itemView: View) :
    WeatherForecastAdapter.WeatherViewHolder(itemView) {
    private val week: TextView = itemView.findViewById(R.id.tv_week)

    @SuppressLint("SetTextI18n")
    override fun onBind(weather: WeatherForecastItem?) {
        weather?.let {
            week.text = Utils.getWeek(weather.date ?: "")
            tvDate.text = Utils.getDate(weather.date?:"")
            temperature.text = "${weather.lowTemperature
                ?: "0"} ~ ${weather.highTemperature ?: "0"}℃"
        }
    }
}