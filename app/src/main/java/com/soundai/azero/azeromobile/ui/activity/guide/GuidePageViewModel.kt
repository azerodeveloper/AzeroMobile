package com.soundai.azero.azeromobile.ui.activity.guide

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.soundai.azero.azeromobile.network.Surrogate
import com.soundai.azero.azeromobile.network.surrogateToken
import com.soundai.azero.azeromobile.ui.activity.base.viewmodel.BaseRequestViewModel
import com.soundai.azero.lib_surrogate.request.ExtendTokenRequest

class GuidePageViewModel(application: Application) : BaseRequestViewModel(application) {
    companion object {
        const val TOKEN_VALID = 1
        const val TOKEN_INVALID = 2
    }
    val loginChecker: LiveData<Int>
        get() = loginCheckerLiveData
    private val loginCheckerLiveData = MutableLiveData<Int>()

    fun extendToken() {
        surrogateRequest({
            Surrogate.extendToken(ExtendTokenRequest(surrogateToken))
        }, {
            Log.i("extendToken", "login state is valid")
            loginCheckerLiveData.value = TOKEN_VALID
        }, {
            Log.i("extendToken", "login state is invalid, need login again!")
            loginCheckerLiveData.value = TOKEN_INVALID
        })
    }
}