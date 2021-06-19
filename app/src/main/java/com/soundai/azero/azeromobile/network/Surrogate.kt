package com.soundai.azero.azeromobile.network

import com.soundai.azero.lib_surrogate.Sender
import com.soundai.azero.lib_surrogate.VerificationType
import com.soundai.azero.lib_surrogate.api.SurrogateService
import com.soundai.azero.lib_surrogate.request.*

object Surrogate {
    // "https://api-azero.soundai.cn"
    // "https://api-dev.soundai.cn"
    // "https://api-fat-azero.soundai.cn"

    private val surrogateService =
        createRetrofit("https://api-azero.soundai.cn", surrogateOkHttpClientCreator).create(
            SurrogateService::class.java
        )

    suspend fun sendSMSVerificationCode(
        phoneNumber: String,
        countryCode: String,
        type: VerificationType,
        sender: Sender
    ) = surrogateService.getVerificationCode(
        VerificationRequest(
            phoneNumber,
            countryCode,
            type.toString(),
            sender.value
        )
    )


    suspend fun loginWithCode(
        countryCode: String,
        phone: String,
        verificationId: String,
        verificationCode: String
    ) = surrogateService.loginWithCode(
        LoginWithCodeRequest(
            phone,
            countryCode,
            verificationId,
            verificationCode
        )
    )

    suspend fun resetPassword(
        passwordNew: String,
        verificationId: String,
        verificationCode: String
    ) = surrogateService.resetPassword(
        ResetPwdRequest(
            passwordNew,
            verificationId,
            verificationCode
        )
    )

    suspend fun loginWithPassword(
        account: String,
        password: String,
        countryCode: String
    ) = surrogateService.loginWithPassword(
        account,
        password,
        countryCode
    )

    suspend fun logout(
        userId: String
    ) = surrogateService.logout("Bearer $surrogateToken", userId)

    suspend fun bindDevice(
        productId: String,
        deviceSn: String,
        userId: String
    ) = surrogateService.bindDevice(
        "Bearer $surrogateToken",
        BindDeviceRequest(
            productId,
            deviceSn,
            userId
        )
    )

    suspend fun getBalance(userId: String) =
        surrogateService.getBalance("Bearer $surrogateToken", userId)

    suspend fun wxWithdrawal(
        amount: Long,
        applyNo: String,
        appId: String,
        desc: String,
        sign: String,
        code: String
    ) = surrogateService.wxWithdrawal(
        "Bearer $surrogateToken",
        WithdrawalRequest(
            amount,
            applyNo,
            appId,
            desc,
            sign,
            code
        )
    )

    suspend fun queryUserInfo(
        userId: String
    ) = surrogateService.queryUserInfo(
        "Bearer $surrogateToken",
        QueryUserInfoRequest(userId)
    )

    suspend fun updateUserInfo(
        userId: String,
        pictureUrl: String?,
        sex: String?,
        birthday: String?,
        name: String?,
        email: String?
    ) = surrogateService.updateUserInfo(
        "Bearer $surrogateToken",
        UpdateUserInfoRequest(
            userId,
            pictureUrl,
            sex,
            birthday,
            name,
            email
        )
    )

    suspend fun extendToken(extendTokenRequest: ExtendTokenRequest) =
        surrogateService.extendToken(extendTokenRequest)
}