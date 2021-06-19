package com.soundai.azero.azeromobile.ui.activity.template.weather

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.Daily
import com.soundai.azero.azeromobile.common.bean.weather.Weather
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity
import com.soundai.azero.azeromobile.ui.activity.template.weather.adapter.WeatherLandAdapter
import com.soundai.azero.azeromobile.ui.activity.template.weather.adapter.WeatherPortraitAdapter
import com.soundai.azero.azeromobile.ui.widget.weather.AirQualityTextView
import com.soundai.azero.azeromobile.utils.DateUtils
import com.soundai.azero.azeromobile.utils.Utils
import com.soundai.azero.azeromobile.utils.WeatherUtils


open class WeatherActivity : BaseDisplayCardActivity() {
    companion object {
        private const val TAG = "WeatherActivity"
    }

    // 通用
    private val container by lazy { findViewById<ViewGroup>(R.id.cl_weather_container) }
    private val location by lazy { findViewById<TextView>(R.id.tv_location) }
    private val city by lazy { findViewById<TextView>(R.id.tv_city) }
    private val share by lazy { findViewById<ImageView>(R.id.ic_share) }

    // 竖屏
    private val normalContainer by lazy { findViewById<ViewGroup>(R.id.cl_weather_normal_container) }
    private val feedback by lazy { findViewById<TextView>(R.id.tv_feedback) }
    private val temperature by lazy { findViewById<TextView>(R.id.tv_temperature) }
    private val weather by lazy { findViewById<TextView>(R.id.tv_weather) }
    private val weatherBottom by lazy { findViewById<TextView>(R.id.tv_weather_bottom) }
    private val lunarDate by lazy { findViewById<TextView>(R.id.tv_lunar_date) }
    private val date by lazy { findViewById<TextView>(R.id.tv_date) }
    private val temperatureRange by lazy { findViewById<TextView>(R.id.tv_temperature_range) }
    private val humidity by lazy { findViewById<TextView>(R.id.tv_humidity) }
    private val wind by lazy { findViewById<TextView>(R.id.tv_wind) }
    private val solarTerms by lazy { findViewById<TextView>(R.id.tv_solar_terms) }
    private val solarTermsGroup by lazy { findViewById<Group>(R.id.gp_solar_terms) }
    private val todayWeather by lazy { findViewById<TextView>(R.id.tv_today_weather) }
    private val todayTemperature by lazy { findViewById<TextView>(R.id.tv_today_temperature) }
    private val todayAir by lazy { findViewById<AirQualityTextView>(R.id.tv_today_air) }
    private val tomorrowWeather by lazy { findViewById<TextView>(R.id.tv_tomorrow_weather) }
    private val tomorrowTemperature by lazy { findViewById<TextView>(R.id.tv_tomorrow_temperature) }
    private val tomorrowAir by lazy { findViewById<AirQualityTextView>(R.id.tv_tomorrow_air) }
    private val humidityGroup by lazy { findViewById<Group>(R.id.gp_humidity) }
    private val portraitRecyclerView by lazy {
        val rv = findViewById<RecyclerView>(R.id.rv_weather_multi_portrait)
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.layoutManager = linearLayoutManager
        rv.adapter = portraitAdapter
        rv.addItemDecoration(WeatherItemDecoration())
        rv
    }
    private val portraitAdapter by lazy { WeatherPortraitAdapter() }

    // 横屏
    private val landRecyclerView by lazy { findViewById<RecyclerView>(R.id.rv_weather_land) }
    private val landAdapter by lazy { WeatherLandAdapter() }

    override val layoutResId: Int
        get() = R.layout.activity_weather_details

    override fun initView() {
        share.setOnClickListener {
            // TODO 分享
        }
        if (Utils.isLandscape(this)) {
            initLandscapeView()
        } else {
            initPortraitView()
        }
    }

    override fun initData(intent: Intent) {
        super.initData(intent)
        val payload = intent.getStringExtra(Constant.EXTRA_TEMPLATE)
        val weather = Gson().fromJson(payload, Weather::class.java)
        weather?.let {
            renderCommon(it)
            if (Utils.isLandscape(this)) {
                renderLandscape(it)
            } else {
                renderPortrait(it)
            }
        }
    }

    private fun renderCommon(weatherInfo: Weather) {
        location.text = weatherInfo.city
        city.text = weatherInfo.city
    }

    private fun renderPortrait(weatherInfo: Weather) {
        val date = weatherInfo.date ?: DateUtils.getTodayDate()
        if (WeatherUtils.isMultiDate(date)) {
            renderPortraitMulti(weatherInfo)
        } else {
            renderPortraitNormal(weatherInfo)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderPortraitNormal(weatherInfo: Weather) {
        portraitRecyclerView.visibility = View.GONE
        normalContainer.visibility = View.VISIBLE
        date.text = weatherInfo.week
        weatherInfo.date?.let {
            lunarDate.text = DateUtils.convertDateFormat(it, "MM月dd日") // TODO 后续skill支持了改成农历
        }
        weatherInfo.weather?.let {
            temperatureRange.text = WeatherUtils.getTemperatureRange(it.low, it.high)
        }
        if (weatherInfo.now != null && weatherInfo.date != null && DateUtils.isToday(weatherInfo.date)) {
            val now = weatherInfo.now
            temperature.text = "${now.temperature}°"
            temperature.textSize = 74f
            humidityGroup.visibility = View.VISIBLE
            humidity.text = now.humidity
            wind.text = "${now.windDirection}风；${now.windScale}级"
            val currentWeather = WeatherUtils.getCurrentWeather(now, weatherInfo.alarms)
            weather.text = currentWeather
            weather.visibility = View.VISIBLE
            weatherBottom.visibility = View.GONE
            changeBackground(currentWeather)
        } else {
            weatherInfo.weather?.let {
                temperature.text = temperatureRange.text
                temperature.textSize = 60f
                val weatherStr = WeatherUtils.getWeather(it.textDay, it.textNight)
                weatherStr?.let { str ->
                    if (str.length > 3) {
                        weather.visibility = View.GONE
                        weatherBottom.visibility = View.VISIBLE
                        weatherBottom.text = str
                    } else {
                        weather.visibility = View.VISIBLE
                        weatherBottom.visibility = View.GONE
                        weather.text = str
                    }
                }
                changeBackground(it.textNight)
                humidityGroup.visibility = View.GONE
                wind.text = "${it.windDirection}风；${it.windScale}级"
            }
        }
        solarTermsGroup.visibility = View.GONE // TODO 节气目前不支持，隐藏
        weatherInfo.daily?.daily?.let {
            if (it.isNotEmpty()) {
                todayWeather.text = WeatherUtils.getWeather(it[0].textDay, it[0].textNight)
                todayTemperature.text =
                    WeatherUtils.getTemperatureRange(it[0].low, it[0].high, "/", false)
            }
            if (it.size >= 2) {
                tomorrowWeather.text = WeatherUtils.getWeather(it[1].textDay, it[1].textNight)
                tomorrowTemperature.text =
                    WeatherUtils.getTemperatureRange(it[1].low, it[1].high, "/", false)
            }
        }
        if (weatherInfo.airs != null) {
            if (weatherInfo.airs.isNotEmpty()) {
                todayAir.setQualityLevel(WeatherUtils.getAirQuality(weatherInfo.airs[0].quality))
            } else {
                todayAir.setQualityLevel(WeatherUtils.getAirQuality(null))
            }
            if (weatherInfo.airs.size >= 2) {
                tomorrowAir.setQualityLevel(WeatherUtils.getAirQuality(weatherInfo.airs[1].quality))
            } else {
                tomorrowAir.setQualityLevel(WeatherUtils.getAirQuality(null))
            }
        } else {
            todayAir.setQualityLevel(WeatherUtils.getAirQuality(null))
            tomorrowAir.setQualityLevel(WeatherUtils.getAirQuality(null))
        }

    }

    private fun renderPortraitMulti(weatherInfo: Weather) {
        portraitRecyclerView.visibility = View.VISIBLE
        normalContainer.visibility = View.GONE
        val date = weatherInfo.date ?: DateUtils.getTodayDate()
        val dailyList = filterDailyList(date, weatherInfo.daily?.daily)
        if (dailyList.isNotEmpty()) {
            changeBackground(dailyList[0].textNight)
        }
        portraitAdapter.dailyList = dailyList
        portraitAdapter.airList = weatherInfo.airs ?: listOf()
        portraitAdapter.notifyDataSetChanged()
    }

    private fun renderLandscape(weatherInfo: Weather) {
        val date = weatherInfo.date ?: DateUtils.getTodayDate()
        val dailyList = filterDailyList(date, weatherInfo.daily?.daily)
        if (DateUtils.isToday(date)) {
            landAdapter.now = weatherInfo.now
            landAdapter.alarmsList = weatherInfo.alarms
            if (weatherInfo.now != null) {
                changeBackground(weatherInfo.now.text)
            } else {
                if (dailyList.isNotEmpty()) {
                    changeBackground(dailyList[0].textNight)
                }
            }
        } else {
            landAdapter.now = null
            landAdapter.alarmsList = null
            if (dailyList.isNotEmpty()) {
                changeBackground(dailyList[0].textNight)
            }
        }
        landAdapter.dailyList = dailyList
        landAdapter.airList = weatherInfo.airs ?: listOf()
        landAdapter.notifyDataSetChanged()
    }

    private fun filterDailyList(date: String, dailyList: List<Daily>?): List<Daily> {
        var result: List<Daily>? = null
        dailyList?.let {
            if (WeatherUtils.isMultiDate(date)) {
                val multiDate = WeatherUtils.getMultiDate(date)
                if (multiDate != null && multiDate.size == 2) {
                    getDailyList(multiDate[0], multiDate[1], it)?.let { list ->
                        result = list
                    }
                } else {
                    Log.e(TAG, "parse multi date failed, result= $multiDate")
                }
            } else {
                getDailyList(date, null, it)?.let { list ->
                    result = list
                }
            }
        }
        return result ?: listOf()
    }

    private fun initPortraitView() {
        feedback.setOnClickListener {
            // TODO 反馈，目前没这个功能，按钮暂时隐藏
        }
    }

    private fun initLandscapeView() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        landRecyclerView.layoutManager = linearLayoutManager
        landRecyclerView.adapter = landAdapter
        landRecyclerView.addItemDecoration(WeatherItemDecoration())
    }

    private fun changeBackground(type: String?) {
        WeatherUtils.getBackgroundId(type)
            ?.let { container.background = resources.getDrawable(it, null) }
    }

    private fun getDailyList(
        startDate: String,
        endDate: String?,
        dailyList: List<Daily>?
    ): List<Daily>? {
        if (dailyList.isNullOrEmpty()) {
            return null
        }
        var startIndex = 0
        var endIndex = dailyList.size
        for (i in dailyList.indices) {
            if (startDate == dailyList[i].date) {
                startIndex = i
                break
            }
        }
        endDate?.let {
            for (i in startIndex until dailyList.size) {
                if (it == dailyList[i].date) {
                    endIndex = i + 1
                    break
                }
            }
        }

        if (startIndex > endIndex) {
            return null
        }
        return dailyList.subList(startIndex, endIndex)
    }

    private inner class WeatherItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            when (parent.getChildAdapterPosition(view)) {
                0 -> {
                    outRect.left = Utils.dp2px(15f).toInt()
                    outRect.right = Utils.dp2px(5f).toInt()
                }
                parent.adapter!!.itemCount - 1 -> {
                    outRect.left = Utils.dp2px(5f).toInt()
                    outRect.right = Utils.dp2px(15f).toInt()
                }
                else -> {
                    outRect.left = Utils.dp2px(5f).toInt()
                    outRect.right = Utils.dp2px(5f).toInt()
                }
            }
        }
    }
}