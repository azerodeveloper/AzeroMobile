package com.soundai.azero.lib_surrogate.request

import com.google.gson.annotations.SerializedName

data class ResetPwdRequest(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("verificationId")
	val verificationId: String? = null,

	@field:SerializedName("verificationCode")
	val verificationCode: String? = null
)