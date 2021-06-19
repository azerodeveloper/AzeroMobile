package com.soundai.azero.lib_surrogate.request

import com.google.gson.annotations.SerializedName

data class BindDeviceRequest(

	@field:SerializedName("productId")
	val productId: String? = null,

	@field:SerializedName("deviceSN")
	val deviceSN: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("token")
	val token: String? = null,
	@field:SerializedName("deviceKey")
	val deviceKey: String? = null,

	@field:SerializedName("name")
	val name: String? = null

)