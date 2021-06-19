package com.soundai.azero.lib_surrogate.api

import com.soundai.azero.lib_surrogate.response.SurrogateResponse
import com.soundai.azero.lib_surrogate.response.UploadInfo
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CMSService {
    @Multipart
    @POST("/v1/cmsservice/resource/upload")
    suspend fun uploadResource(
        @Header("Authorization") token: String,
        @Part() body: MultipartBody.Part
    ): SurrogateResponse<UploadInfo>
}