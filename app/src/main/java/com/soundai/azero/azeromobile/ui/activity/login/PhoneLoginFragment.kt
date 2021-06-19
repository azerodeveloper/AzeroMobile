package com.soundai.azero.azeromobile.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.azero.sdk.util.log
import com.rengwuxian.materialedittext.MaterialEditText
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.login.LoginViewModel
import com.soundai.azero.azeromobile.common.bean.login.VerificationCodeState
import com.soundai.azero.azeromobile.safeNavigate
import com.soundai.azero.azeromobile.ui.activity.login.dialog.ProtocolDialogFragment
import com.soundai.azero.azeromobile.utils.SPUtils.getAccountPref
import com.soundai.azero.lib_surrogate.VerificationType
import com.yiang.phoneareacode.AreaCodeModel
import com.yiang.phoneareacode.PhoneAreaCodeActivity
import com.yiang.phoneareacode.SelectPhoneCode

class PhoneLoginFragment : Fragment() {
    private lateinit var tvAreaCode: TextView
    private lateinit var btnGetCode: Button
    private lateinit var etPhoneNum: MaterialEditText
    private lateinit var errorHint: TextView
    private lateinit var tvProtocolPrompt: TextView
    private lateinit var loginViewModel: LoginViewModel

    private val verificationCodeObserver by lazy {
        Observer<VerificationCodeState> {
            if (it.exception != null) {
                errorHint.visibility = View.VISIBLE
                errorHint.text = it.exception.msg
            } else {
                errorHint.visibility = View.GONE
                findNavController().safeNavigate(
                    R.id.action_phoneLoginFragment_to_verificationCodeFragment,
                    R.id.phoneLoginFragment
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_phone_login, container, false)
        initView(root)
        log.e(
            "surrogate phone login:" + getAccountPref().getString(
                Constant.SAVE_USERID,
                ""
            ) + "token:${getAccountPref().getString(Constant.SAVE_TOKEN, "")}"
        )
        return root
    }

    private fun initView(root: View) {
        btnGetCode = root.findViewById(R.id.btn_get_verification_code)
        etPhoneNum = root.findViewById(R.id.et_phone_num)
        tvAreaCode = root.findViewById(R.id.tv_area_code)
        errorHint = root.findViewById(R.id.tv_error_hint)
        tvProtocolPrompt = root.findViewById(R.id.tv_protocols_prompt)
        loginViewModel = ViewModelProviders.of(activity!!).get(LoginViewModel::class.java)
        btnGetCode.setOnClickListener {
            val phoneNum = etPhoneNum.text.toString()
            loginViewModel.phoneNumber = phoneNum
            loginViewModel.countryCode = tvAreaCode.text.toString()
            loginViewModel.model = VerificationType.VERIFICATION
            loginViewModel.sendVerificationCode()
        }
        etPhoneNum.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnGetCode.isEnabled = s.length == 11
                if (btnGetCode.isEnabled) {
                    btnGetCode.setTextColor(activity!!.resources.getColor(R.color.textPrimary))
                } else {
                    btnGetCode.setTextColor(activity!!.resources.getColor(R.color.textHint))
                }
            }

        })

        tvAreaCode.setOnClickListener {
            SelectPhoneCode.with(this)
                .setTitle("选择手机号归属地")
                .setStickHeaderColor("#EBEBEB")//粘性头部背景颜色
                .setTitleBgColor("#FFFFFF")//界面头部标题背景颜色
                .setTitleTextColor("#333333")//标题文字颜色
                .select()
        }

        initProtocolPrompt()

        loginViewModel.verificationCodeState.observe(activity!!, verificationCodeObserver)
    }

    private fun initProtocolPrompt() {
        tvProtocolPrompt.text =
            SpannableString(resources.getString(R.string.protocols_prompt)).apply {
                setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            ProtocolDialogFragment.newInstance(ProtocolDialogFragment.TYPE_PRIVACY_PROTOCOL)
                                .show(activity!!.supportFragmentManager, "privacyProtocolDialog")
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = resources.getColor(R.color.blueText)
                            ds.isUnderlineText = false
                        }
                    }, 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            ProtocolDialogFragment.newInstance(ProtocolDialogFragment.TYPE_USER_PROTOCOL)
                                .show(activity!!.supportFragmentManager, "userProtocolDialog")
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = resources.getColor(R.color.blueText)
                            ds.isUnderlineText = false
                        }
                    }, 12, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        tvProtocolPrompt.highlightColor = resources.getColor(R.color.color_transparent)
        tvProtocolPrompt.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginViewModel.verificationCodeState.removeObserver(verificationCodeObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == PhoneAreaCodeActivity.resultCode) {
            if (data != null) {
                val model =
                    data.getSerializableExtra(PhoneAreaCodeActivity.DATAKEY) as AreaCodeModel
                tvAreaCode.text = "+${model.tel}"

            }
        }
    }
}
