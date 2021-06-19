package com.soundai.azero.azeromobile.network

import com.soundai.azero.lib_surrogate.api.CMSService
import com.soundai.azero.lib_surrogate.response.SurrogateResponse
import com.soundai.azero.lib_surrogate.response.UploadInfo
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object Cms {
    private val cmsService =
        createRetrofit("https://cms-azero.soundai.cn:8443", defaultOkHttpClientCreator).create(CMSService::class.java)

    suspend fun uploadResource(file: File): SurrogateResponse<UploadInfo> {
        val requestBody = RequestBody.create(MediaType.parse("image/jpg"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return cmsService.uploadResource("Bearer $surrogateToken", body)
    }
}