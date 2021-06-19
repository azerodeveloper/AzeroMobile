package com.soundai.azero.azeromobile.common.bean.weather

data class WeatherResponse(
	val condition: Condition? = null,
	val currentWeatherIcon: CurrentWeatherIcon? = null,
	val skillIcon: SkillIcon? = null,
	val weatherForecast: List<WeatherForecastItem?>? = null,
	val highTemperature: HighTemperature? = null,
	val ttsContent: String? = null,
	val lowTemperature: LowTemperature? = null,
	val type: String? = null,
	val title: Title? = null,
	val token: String? = null
)
