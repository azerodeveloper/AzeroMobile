package com.soundai.azero.lib_surrogate.request

import com.google.gson.annotations.SerializedName

data class VerificationRequest(
	@field:SerializedName("phoneNumber")
	val phoneNumber: String,

	@field:SerializedName("countryCode")
	val countryCode: String,

    @field:SerializedName("type")
	val type: String,

    @field:SerializedName("sender")
    val sender: Int
)