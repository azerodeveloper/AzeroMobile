package com.soundai.azero.azeromobile.ui.activity.base.viewmodel

import com.soundai.azero.lib_surrogate.exception.RetrofitException
import com.soundai.azero.lib_surrogate.response.SurrogateResponse
import com.soundai.azero.lib_surrogate.response.WXAccessResponse

suspend fun <T> surrogateChecker(
    response: SurrogateResponse<T>,
    success: suspend (T) -> Unit
) {
    if (response.isSuccess)
        success(response.data)
    else
        throw RetrofitException(response.code, response.message)
}

suspend fun wxChecker(response: WXAccessResponse,success: suspend (WXAccessResponse) -> Unit){
    if (response.errCode == 0)
        success(response)
    else
        throw RetrofitException(response.errCode,response.errMsg)
}