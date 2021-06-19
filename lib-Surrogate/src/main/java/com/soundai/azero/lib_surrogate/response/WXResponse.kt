package com.soundai.azero.lib_surrogate.response

import com.google.gson.annotations.SerializedName

data class WXAccessResponse(
    @field:SerializedName("errcode") val errCode: Int = 0,
    @field:SerializedName("errmsg") val errMsg: String,
    @field:SerializedName("access_token") val accessToken: String,
    @field:SerializedName("expires_in") val expiresIn: Int,
    @field:SerializedName("refresh_token") val refreshToken: String,
    @field:SerializedName("openid") val openId: String,
    @field:SerializedName("scope") val scope: String,
    @field:SerializedName("unionid") val unionId: String
)