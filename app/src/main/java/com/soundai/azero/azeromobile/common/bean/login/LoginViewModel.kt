package com.soundai.azero.azeromobile.common.bean.login

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.network.Surrogate
import com.soundai.azero.azeromobile.toast
import com.soundai.azero.azeromobile.ui.activity.base.viewmodel.BaseRequestViewModel
import com.soundai.azero.azeromobile.ui.activity.guide.GuidePageActivity
import com.soundai.azero.azeromobile.utils.SPUtils
import com.soundai.azero.azeromobile.utils.Utils
import com.soundai.azero.lib_surrogate.Sender
import com.soundai.azero.lib_surrogate.VerificationType

class LoginViewModel(
    application: Application
) : BaseRequestViewModel(application) {
    val loginState: LiveData<LoginState>
        get() = _loginState
    val resetPasswordState: LiveData<ResetPasswordState>
        get() = _resetPasswordState
    val verificationCodeState: LiveData<VerificationCodeState>
        get() = _verificationCodeState

    private val _loginState = MutableLiveData<LoginState>()
    private val _resetPasswordState = MutableLiveData<ResetPasswordState>()
    private val _verificationCodeState = MutableLiveData<VerificationCodeState>()

    var phoneNumber: String = ""
    var countryCode: String = "+86"
    var verificationId: String = ""
    var model: VerificationType = VerificationType.VERIFICATION

    fun sendVerificationCode() {
        surrogateRequest({
            Surrogate.sendSMSVerificationCode(
                phoneNumber,
                countryCode,
                VerificationType.VERIFICATION,
                Sender.SOUNDAI
            )
        }, {
            verificationId = it.verificationId
            _verificationCodeState.value = VerificationCodeState()
        }, {
            _verificationCodeState.value = VerificationCodeState(it)
        })
    }

    fun loginWithCode(verificationCode: CharSequence) {
        surrogateRequest({
            Surrogate.loginWithCode(
                countryCode,
                phoneNumber,
                verificationId,
                verificationCode.toString()
            )
        }, {
            SPUtils.saveAccountInfo(
                it.token,
                it.userId,
                phoneNumber
            )
            bindDevice(it.userId)
        }, {
            _loginState.value = LoginState(it)
            TaApp.application.toast("登录失败")
        })
    }

    fun loginWithPassword(account: String, password: String) {
        surrogateRequest({
            Surrogate.loginWithPassword(account, password, countryCode)
        }, {
            SPUtils.saveAccountInfo(
                it.token,
                it.userId,
                phoneNumber
            )
            bindDevice(it.userId)
        }, {
            _loginState.value = LoginState(it)
            TaApp.application.toast("登录失败")
        })
    }

    fun resetPassword(verificationCode: CharSequence) {
        surrogateRequest({
            Surrogate.resetPassword(
                "",
                verificationId,
                verificationCode.toString()
            )
        }, {
            _resetPasswordState.value = ResetPasswordState(null)
        }, {
            _resetPasswordState.value = ResetPasswordState(it)
        })
    }

    private fun bindDevice(userId: String) {
        surrogateRequest({
            Surrogate.bindDevice(
                TaApp.productId,
                Utils.getimei(TaApp.application) ?: "",
                userId
            )
        }, {
            SPUtils.saveBondState(true)
            TaApp.application.startAzero()
            _loginState.value = LoginState(null)
        }, {
            _loginState.value = LoginState(it)
            TaApp.application.toast("登录失败")
        })
    }

    fun logout(userId: String, activity: Activity) {
        surrogateRequest({
            Surrogate.logout(userId)
        }, {
            SPUtils.getAccountPref().edit()?.clear()?.apply()
            val intent = Intent(activity, GuidePageActivity::class.java)
            activity.startActivity(intent)
        }, {
            activity.toast("${it.message}")
        })
    }
}
