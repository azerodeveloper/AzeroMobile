package com.soundai.azero.azeromobile.common.bean.login

import com.soundai.azero.lib_surrogate.exception.RetrofitException

data class LoginState(val exception: RetrofitException?)
data class ResetPasswordState(val exception: RetrofitException?)
data class VerificationCodeState(val exception: RetrofitException? = null)