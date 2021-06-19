package com.soundai.azero.azeromobile.ui.activity.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.login.LoginViewModel
import com.soundai.azero.azeromobile.ui.activity.debug.DebugActivity
import com.soundai.azero.azeromobile.ui.activity.personal.PersonalInfoActivity
import com.soundai.azero.azeromobile.ui.activity.wallet.WalletActivity
import com.soundai.azero.azeromobile.utils.SPUtils
import com.soundai.azero.azeromobile.utils.Utils

class PersonalFragment : Fragment(), View.OnClickListener {

    private lateinit var clPersonalInfo: ConstraintLayout
    private lateinit var clAccountAndSafe: ConstraintLayout
    private lateinit var clVoiceprintSetting: ConstraintLayout
    private lateinit var clMyWallet: ConstraintLayout
    private lateinit var clDebug: ConstraintLayout
    private lateinit var btnLogout: Button
    private lateinit var verTextView: TextView

    companion object {
        fun newInstance() = PersonalFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.personal_fragment, container, false)
        initView(rootView)
        initVersionInfo(rootView)
        return rootView
    }

    private fun initView(rootView: View) {
        clPersonalInfo = rootView.findViewById(R.id.cl_personal_info)
        clAccountAndSafe = rootView.findViewById(R.id.cl_account_and_safe)
        clVoiceprintSetting = rootView.findViewById(R.id.cl_voiceprint_setting)
        clMyWallet = rootView.findViewById(R.id.cl_my_wallet)
        btnLogout = rootView.findViewById(R.id.btn_logout)
        clDebug = rootView.findViewById(R.id.cl_my_debug )

        btnLogout.setOnClickListener(this)
        clPersonalInfo.setOnClickListener(this)
        clAccountAndSafe.setOnClickListener(this)
        clVoiceprintSetting.setOnClickListener(this)
        clMyWallet.setOnClickListener(this)
        clDebug.setOnClickListener(this)
    }

    private fun initVersionInfo(rootView: View){
        var version = Utils.getVersion(this.context)
        verTextView = rootView.findViewById(R.id.version_textview)
        verTextView.setText(version)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cl_personal_info -> {
                startActivity(Intent(activity,PersonalInfoActivity::class.java))
            }
            R.id.cl_account_and_safe -> {
            }
            R.id.cl_voiceprint_setting -> {
            }
            R.id.cl_my_wallet -> {
                startActivity(Intent(activity,WalletActivity::class.java))
            }
            R.id.cl_my_debug -> {
                startActivity(Intent(activity,DebugActivity::class.java))
            }
            R.id.btn_logout ->{
                val loginViewModel = ViewModelProviders.of(activity!!).get(LoginViewModel::class.java)
                val sp = SPUtils.getAccountPref()
                val userId = sp?.getString(Constant.SAVE_USERID, "")
                loginViewModel.logout(userId!!,activity!!)
            }
        }
    }

}
