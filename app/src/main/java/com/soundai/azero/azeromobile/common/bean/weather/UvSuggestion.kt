package com.soundai.azero.azeromobile.common.bean.weather

data class UvSuggestion(
    val brief: String? = null,
    val details: String? = null,
    val level: Int = 0,
    val maxLevel: Int = 0,
    val iconUrl: String? = null,
    val suggest: String? = null
)