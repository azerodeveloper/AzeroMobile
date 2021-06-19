package com.soundai.azero.azeromobile.common.request

import com.azero.sdk.util.log
import com.soundai.azero.lib_surrogate.exception.RetrofitException
import com.soundai.azero.lib_surrogate.exception.convertException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.lang.RuntimeException

class CoroutineRequestExecutor {
    fun <T, U> request(
        request: suspend () -> T,
        success: suspend (U) -> Unit,
        error: suspend (e: RetrofitException) -> Unit = {},
        complete: suspend () -> Unit = {},
        check: suspend (rsp: T, suspend (U) -> Unit) -> Unit,
        launch: (block: suspend () -> Unit) -> Job
    ) {
        launch {
            try {
                val response = withContext(Dispatchers.IO) { request() }
                check(response, success)
            } catch (e: Throwable) {
                val ex = convertException(e)
                log.e("request error, code= ${ex.code}, message= ${ex.msg}")
                error(ex)
            } finally {
                complete()
            }
        }
    }
}