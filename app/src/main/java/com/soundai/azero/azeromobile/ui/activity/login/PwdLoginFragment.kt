package com.soundai.azero.azeromobile.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.login.LoginState
import com.soundai.azero.azeromobile.common.bean.login.LoginViewModel
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity

class PwdLoginFragment : Fragment() {
    private val loginViewModel by lazy {
        ViewModelProviders.of(activity!!).get(LoginViewModel::class.java)
    }
    private val loginStateObserver = Observer<LoginState> {
        if (it.exception != null) {
            tvError.visibility = View.VISIBLE
        } else {
            startActivity(Intent(activity, LauncherActivity::class.java))
            activity?.finish()
        }
    }

    private lateinit var etAccount: EditText
    private lateinit var etPwd: EditText
    private lateinit var tvError: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pwd_login, container, false)
        // Inflate the layout for this fragment
        etAccount = view.findViewById(R.id.et_account)
        etPwd = view.findViewById(R.id.et_phone_num)
        tvError = view.findViewById(R.id.tv_error_hint)
        view.findViewById<TextView>(R.id.tv_forget_pwd).setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_pwdLoginFragment_to_pwdForgetFragment)
        }
        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            val account = etAccount.text.toString()
            val password = etAccount.text.toString()
            loginViewModel.loginWithPassword(account, password)
        }
        initViewModel()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    private fun initViewModel() {
        loginViewModel.loginState.observe(activity!!, loginStateObserver)
    }

    private fun removeObservers() {
        loginViewModel.loginState.removeObserver(loginStateObserver)
    }
}
