package com.soundai.azero.azeromobile.common.bean.weather

data class Air(
    val date: String? = null,
    val aqi: String? = null,
    val pm25: String? = null,
    val pm10: String? = null,
    val so2: String? = null,
    val no2: String? = null,
    val co: String? = null,
    val o3: String? = null,
    val primaryPollutant: String? = null,
    val quality: String? = null,
    val level: Int = 0,
    val maxLevel: Int = 0,
    val iconUrl: String? = null,
    val backgroundUrl: String? = null
)