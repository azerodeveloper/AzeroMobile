package com.soundai.azero.azeromobile.common.bean.weather

data class Now(
    val text: String? = null,
    val code: String? = null,
    val temperature: String? = null,
    val feelsLike: String? = null,
    val pressure: String? = null,
    val humidity: String? = null,
    var visibility: String? = null,
    val windDirection: String? = null,
    val windDirectionDegree: String? = null,
    val windSpeed: String? = null,
    val windScale: String? = null,
    val iconUrl: String? = null,
    val backgroundUrl: String? = null,
    val windDirectionIcon: String? = null
)