package com.soundai.azero.azeromobile.ui.activity.base.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soundai.azero.azeromobile.common.request.CoroutineRequestExecutor
import com.soundai.azero.lib_surrogate.exception.RetrofitException
import com.soundai.azero.lib_surrogate.response.SurrogateResponse
import kotlinx.coroutines.launch

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}

abstract class BaseRequestViewModel(application: Application) : BaseViewModel(application) {
    private val executor by lazy { CoroutineRequestExecutor() }

    fun <T, U> request(
        block: suspend () -> T,
        success: suspend (U) -> Unit,
        error: suspend (e: RetrofitException) -> Unit = {},
        complete: suspend () -> Unit = {},
        check: suspend (rsp: T, suspend (U) -> Unit) -> Unit
    ) {
        executor.request(block, success, error, complete, check, ::launch)
    }

    fun <T> surrogateRequest(
        block: suspend () -> SurrogateResponse<T>,
        success: suspend (T) -> Unit,
        error: suspend (e: RetrofitException) -> Unit = {},
        complete: suspend () -> Unit = {}
    ) {
        request(block, success, error, complete, ::surrogateChecker)
    }
}