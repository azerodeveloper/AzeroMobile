package com.soundai.azero.azeromobile.common.bean.weather

data class Weather(
    val date: String? = null,
    val week: String? = null,
    val city: String? = null,
    val now: Now? = null,
    val suggestion: LifeSuggestion? = null,
    val uv: UvSuggestion? = null,
    val air: Air? = null,
    val airs: List<Air>? = null,
    val daily: DailyBean? = null,
    val weather: Daily? = null,
    val alarms: List<Alarm>? = null,
    val notSupport: NotSupport? = null,
    val answer: String? = null
)