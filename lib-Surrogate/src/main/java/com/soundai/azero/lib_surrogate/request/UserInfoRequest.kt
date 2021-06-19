package com.soundai.azero.lib_surrogate.request

data class QueryUserInfoRequest(val userId: String)

data class UpdateUserInfoRequest(
    val userId: String?,
    val pictureUrl: String?,
    val sex: String?,
    val birthday: String?,
    val name: String?,
    val email: String?
)

data class ExtendTokenRequest(
    val token: String
)