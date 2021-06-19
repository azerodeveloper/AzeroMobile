package com.soundai.azero.lib_surrogate.api

import com.soundai.azero.lib_surrogate.request.*
import com.soundai.azero.lib_surrogate.response.*
import retrofit2.Call
import retrofit2.http.*

interface SurrogateService {
    @POST("v1/surrogate/users/verification")
    suspend fun getVerificationCode(
        @Body verificationRequest: VerificationRequest
    ): SurrogateResponse<VerificationData>

    @POST("v1/surrogate/users/loginWithCode")
    suspend fun loginWithCode(
        @Body loginWithCodeRequest: LoginWithCodeRequest
    ): SurrogateResponse<LoginWithCodeData>

    @POST("v1/surrogate/users/resetPassword")
    suspend fun resetPassword(
        @Body resetPwdRequest: ResetPwdRequest
    ): SurrogateResponse<Nothing>

    @GET("v1/surrogate/users/login?phone={account}&password={password}&countryCode={countryCode}")
    suspend fun loginWithPassword(
        @Path("account") account: String,
        @Path("password") password: String,
        @Path("countryCode") countryCode: String
    ): SurrogateResponse<LoginWithPasswordData>

    @POST("v1/surrogate/users/device/bind")
    suspend fun bindDevice(
        @Header("Authorization") token: String,
        @Body bindDeviceRequest: BindDeviceRequest
    ): SurrogateResponse<BindDeviceData>

    @GET("/v1/surrogate/payment/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): SurrogateResponse<Balance>

    @POST("/v1/surrogate/payment/withdraw/wechat")
    suspend fun wxWithdrawal(
        @Header("Authorization") token: String,
        @Body withdrawalRequest: WithdrawalRequest
    ): SurrogateResponse<Withdrawal>

    @POST("/v1/surrogate/users/queryUserInfo")
    suspend fun queryUserInfo(
        @Header("Authorization") token: String,
        @Body queryUserInfoRequest: QueryUserInfoRequest
    ): SurrogateResponse<UserInfo>

    @POST("/v1/surrogate/users/modify")
    suspend fun updateUserInfo(
        @Header("Authorization") token: String,
        @Body updateUserInfoRequest: UpdateUserInfoRequest
    ): SurrogateResponse<Nothing>

    @GET("/v1/surrogate/users/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): SurrogateResponse<Nothing>

    @POST("/v1/surrogate/users/extendToken")
    suspend fun extendToken(@Body extendToken: ExtendTokenRequest): SurrogateResponse<Nothing>
}