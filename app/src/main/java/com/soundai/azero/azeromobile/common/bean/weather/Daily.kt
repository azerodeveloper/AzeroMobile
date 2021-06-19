package com.soundai.azero.azeromobile.common.bean.weather

data class Daily(
    val date: String? = null,
    val textDay: String? = null,
    val codeDay: String? = null,
    val textNight: String? = null,
    val codeNight: String? = null,
    val high: String? = null,
    val low: String? = null,
    val precip: String? = null,
    val windDirection: String? = null,
    val windDirectionDegree: String? = null,
    val windSpeed: String? = null,
    val windScale: String? = null,
    val iconUrl: String? = null,
    val backgroundUrl: String? = null,
    val week: String? = null,
    val monthDay: String? = null
)