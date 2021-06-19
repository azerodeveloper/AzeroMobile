package com.soundai.azero.lib_surrogate.response

data class SurrogateResponse<T>(val code: Int, val message: String, val data: T) {
    val isSuccess
        get() = code == 200
}

data class LoginWithPasswordData(
    val userId: String,
    val token: String
)

data class LoginWithCodeData(
    val userId: String,
    val token: String
)

data class VerificationData(
    val expiresIn: Int? = null,
    val registered: Boolean? = null,
    val verificationId: String
)


data class BindDeviceData(val deviceId: String)

data class Balance(val balance: Long)

data class Withdrawal(val result: String, val partnerTradeNo: String, val orderNum: String)

data class UserInfo(
    val userId: String,
    val countryCode: String?,
    val phoneNumber: String?,
    val createdTime: String?,
    val name: String?,
    val birthday: String?,
    val sex: String?,
    val pictureUrl: String?,
    val email: String?
)

data class UploadInfo(val id: String, val url: String)