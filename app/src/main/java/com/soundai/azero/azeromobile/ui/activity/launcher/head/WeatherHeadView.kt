package com.soundai.azero.azeromobile.ui.activity.launcher.head

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.WeatherResponse
import com.soundai.azero.azeromobile.common.decoration.HorizontalItemDecoration
import com.soundai.azero.azeromobile.ui.activity.launcher.adapter.WeatherForecastAdapter
import com.soundai.azero.azeromobile.ui.widget.StaggeredGridRecyclerView

class WeatherHeadView(private val weatherResponse: WeatherResponse?) : IHeadView {
    override fun inflateHeadView(container: ViewGroup) {
        if (weatherResponse == null) return
        if (weatherResponse.weatherForecast == null) return
        val context = TaApp.application
        val rootView =
            LayoutInflater.from(context).inflate(R.layout.cardview_weather, container)
        rootView.findViewById<StaggeredGridRecyclerView>(R.id.rv_weather).run {
            addItemDecoration(
                HorizontalItemDecoration(
                    5f
                )
            )
            layoutManager = GridLayoutManager(
                context,
                1,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = WeatherForecastAdapter().also { adapter ->
                adapter.dataList = weatherResponse.weatherForecast
            }
        }
    }

    override fun release() {
        // do nothing
    }
}