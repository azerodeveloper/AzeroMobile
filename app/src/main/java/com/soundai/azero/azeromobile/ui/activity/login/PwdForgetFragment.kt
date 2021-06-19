package com.soundai.azero.azeromobile.ui.activity.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.login.LoginViewModel
import com.soundai.azero.lib_surrogate.VerificationType

class PwdForgetFragment : Fragment() {
    private lateinit var etPhoneNum: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pwd_forget, container, false)
        etPhoneNum = view.findViewById(R.id.et_phone_num)
        view.findViewById<Button>(R.id.btn_next).setOnClickListener {
            val loginViewModel = ViewModelProviders.of(activity!!).get(LoginViewModel::class.java)
            loginViewModel.model = VerificationType.RESET_PWD
            Navigation.findNavController(it)
                .navigate(R.id.action_pwdForgetFragment_to_verificationCodeFragment)
        }

        return view
    }
}
