package com.soundai.azero.azeromobile.ui.activity.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.login.LoginState
import com.soundai.azero.azeromobile.common.bean.login.LoginViewModel
import com.soundai.azero.azeromobile.common.bean.login.ResetPasswordState
import com.soundai.azero.azeromobile.common.bean.login.VerificationCodeState
import com.soundai.azero.azeromobile.safeNavigate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.widget.CaptchaInputView
import com.soundai.azero.lib_surrogate.VerificationType

class VerificationCodeFragment : BaseFragment() {
    private lateinit var tvHint: TextView
    private lateinit var tvErrorHint: TextView
    private lateinit var civVerification: CaptchaInputView
    private lateinit var btnResend: Button
    private lateinit var model: VerificationType
    private val loginViewModel by lazy {
        ViewModelProviders.of(activity!!).get(LoginViewModel::class.java)
    }

    private val verificationCodeObserver by lazy {
        Observer<VerificationCodeState> {
            if (it.exception != null) {
                tvErrorHint.visibility = View.VISIBLE
                tvErrorHint.text = it.exception.msg
            } else {
                tvErrorHint.visibility = View.GONE
            }
        }
    }

    private val resendClock = object : CountDownTimer(60 * 1000, 1000) {
        override fun onFinish() {
            btnResend.isEnabled = true
            btnResend.text = "重发验证码"
            btnResend.setTextColor(activity!!.resources.getColor(R.color.textPrimary))
        }

        @SuppressLint("SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {
            btnResend.isEnabled = false
            btnResend.text = "${millisUntilFinished / 1000}秒后重发"
            btnResend.setTextColor(activity!!.resources.getColor(R.color.textHint))
        }
    }

    private val loginStateObserver = Observer<LoginState> {
        if (it.exception != null) {
            if (it.exception.code >= 1000) {
                tvErrorHint.text = it.exception.msg
                tvErrorHint.visibility = View.VISIBLE
            }
        } else {
            activity?.finish()
        }
    }
    private val resetPasswordStateObserver = Observer<ResetPasswordState> {
        if (it.exception != null) {
            if (it.exception.code >= 1000) {
                tvErrorHint.text = it.exception.msg
                tvErrorHint.visibility = View.VISIBLE
            }
        } else {
            Navigation.findNavController(civVerification)
                .navigate(R.id.action_verificationCodeFragment_to_pwdLoginFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = loginViewModel.model
        val root = inflater.inflate(R.layout.fragment_verification_code, container, false)
        initView(root)
        initViewModel()
        btnResend.post { resendClock.start() }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
        resendClock.cancel()
    }

    private fun initViewModel() {
        with(loginViewModel) {
            verificationCodeState.observe(activity!!, verificationCodeObserver)
            loginState.observe(activity!!, loginStateObserver)
            resetPasswordState.observe(activity!!, resetPasswordStateObserver)
        }
    }

    private fun removeObservers() {
        with(loginViewModel) {
            verificationCodeState.removeObserver(verificationCodeObserver)
            loginState.removeObserver(loginStateObserver)
            resetPasswordState.removeObserver(resetPasswordStateObserver)
        }
    }

    private fun sendVerificationCode() {
        loginViewModel.sendVerificationCode()
        resendClock.start()
    }

    private fun initView(root: View) {
        btnResend = root.findViewById(R.id.btn_resend)
        tvHint = root.findViewById(R.id.tv_verification_send_hint)
        tvErrorHint = root.findViewById(R.id.tv_error_hint)
        tvHint.text = "验证码已发送至${loginViewModel.countryCode} ${loginViewModel.phoneNumber}"
        civVerification = root.findViewById(R.id.civ_verification)
        civVerification.setTextChangeListener(object : CaptchaInputView.TextChangeListener {
            override fun onTextChanged(
                text: CharSequence?,
                start: Int,
                lengthBefore: Int,
                lengthAfter: Int
            ) {
                if (!text.isNullOrEmpty() && text.length == civVerification.getPasswordLength()) {
                    when (model) {
                        VerificationType.REGISTER -> {
                        }
                        VerificationType.VERIFICATION -> onVerification(text)
                        VerificationType.RESET_PWD -> onResetPassword(text)
                    }
                }
            }
        })
        btnResend.setOnClickListener {
            sendVerificationCode()
        }
    }

    private fun onResetPassword(verificationCode: CharSequence) {
        loginViewModel.resetPassword(verificationCode)
    }

    private fun onVerification(verificationCode: CharSequence) {
        loginViewModel.loginWithCode(verificationCode)
    }
}
