package com.soundai.azero.lib_surrogate.request

import com.google.gson.annotations.SerializedName

data class LoginWithCodeRequest(

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("countryCode")
	val countryCode: String? = null,

	@field:SerializedName("verificationId")
	val verificationId: String? = null,

	@field:SerializedName("verificationCode")
	val verificationCode: String? = null
)