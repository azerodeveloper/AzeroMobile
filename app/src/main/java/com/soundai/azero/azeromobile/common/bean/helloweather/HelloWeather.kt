package com.soundai.azero.azeromobile.common.bean.helloweather

data class HelloWeather(val date: String, val week: String, val condition: Condition, val air: Air)

data class Air(val aqi: String, val quality: String)

data class Condition(
    val code: String,
    val text: String,
    val temperature: String,
    val windDirection: String,
    val windScale: String
)