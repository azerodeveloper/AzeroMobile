package com.soundai.azero.azeromobile.ui.activity.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.toast
import com.soundai.azero.azeromobile.ui.activity.base.activity.BaseSwipeActivity
import com.soundai.azero.azeromobile.ui.widget.MoneyInputFilter
import com.soundai.azero.azeromobile.utils.SPUtils.getAccountPref
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WalletActivity : BaseSwipeActivity(), View.OnClickListener {

    private val TAG = WalletActivity::class.java.simpleName

    private val walletViewModel: WalletViewModel by lazy {
        ViewModelProviders.of(this).get(WalletViewModel::class.java)
    }
    private var wxApi: IWXAPI? = null
    private val tvMoney: TextView by lazy { findViewById<TextView>(R.id.tv_money) }
    private val btnWithdrawal: Button by lazy { findViewById<Button>(R.id.btn_withdrawal) }
    private val etMoney: EditText by lazy { findViewById<EditText>(R.id.et_money) }
    private val tvAll: TextView by lazy { findViewById<TextView>(R.id.tv_all) }
    private val rlWexinPay: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_wexin_pay) }
    private val rlAliPay: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_ali_pay) }
    private val cbWexinPay: CheckBox by lazy { findViewById<CheckBox>(R.id.cb_wexin_pay) }
    private val cbAliPay: CheckBox by lazy { findViewById<CheckBox>(R.id.cb_ali_pay) }
    private val tvOverflow: TextView by lazy { findViewById<TextView>(R.id.tv_overflow_tip) }
    private val clInput: ConstraintLayout by lazy{findViewById<ConstraintLayout>(R.id.cl_input)}

    private val moneyTextChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (!s.toString().isNullOrEmpty()){
                walletViewModel.inputBalance(s.toString().toDouble())
            } else{
                btnWithdrawal.isEnabled = false
                tvOverflow.visibility = View.GONE
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        wxApi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID, false)
        wxApi?.registerApp(Constant.WX_APP_ID)

        clInput.setOnClickListener(this)
        rlWexinPay.setOnClickListener(this)
        rlAliPay.setOnClickListener(this)
        tvAll.setOnClickListener(this)
        btnWithdrawal.setOnClickListener(this)
        etMoney.addTextChangedListener(moneyTextChangeListener)
        etMoney.filters = arrayOf(MoneyInputFilter())
        btnWithdrawal.isEnabled = false

        initViewModel()
    }

    private fun initViewModel() {
        walletViewModel.stateLiveData.observe(this, this::onNewState)
        walletViewModel.balanceLiveData.observe(this, Observer {
            tvMoney.text = (it.toDouble() / 100).toString()
        })
        walletViewModel.enableWithdrawalLiveData.observe(this, Observer {
            if (it) {
                tvOverflow.visibility = View.VISIBLE
                btnWithdrawal.isEnabled = false
            } else {
                btnWithdrawal.isEnabled = true
                tvOverflow.visibility = View.GONE
            }
        })
        walletViewModel.withdrawalSuccess.observe(this, Observer {
            if(it){
                etMoney.setText("")
                Toast.makeText(this, "提现成功", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val money = etMoney.text.toString().trim()
        if(money.isNullOrEmpty()){
            toast("提现金额不能为空")
            return
        }
        val wxCode = intent?.getStringExtra("wxCode")
        val sp = getSharedPreferences(
            Constant.SHARDPREF_ACCOUNT,
            Context.MODE_PRIVATE
        )
        val token = sp.getString(Constant.SAVE_TOKEN, "")
        val userId = sp.getString(Constant.SAVE_USERID, "")
        wxCode?.let {
            walletViewModel.wxWithdrawal(token,userId,money.toDouble(),"TA来了提现",Constant.APP_ID,it)
        }
    }

    override fun onStart() {
        super.onStart()
        getAccountPref().apply {
            val token = getString(Constant.SAVE_TOKEN, "")
            val userId = getString(Constant.SAVE_USERID, "")
            walletViewModel.getBalance(token, userId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wxApi?.unregisterApp()
        etMoney.removeTextChangedListener(moneyTextChangeListener)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rl_wexin_pay -> {
                if (cbAliPay.isChecked) {
                    cbAliPay.isChecked = false
                }
                cbWexinPay.isChecked = !cbWexinPay.isChecked
            }
            R.id.rl_ali_pay -> {
                if (cbWexinPay.isChecked) {
                    cbWexinPay.isChecked = false
                }
                cbAliPay.isChecked = !cbAliPay.isChecked
            }
            R.id.tv_all -> {
                walletViewModel.balanceLiveData.value.let {
                    etMoney.setText((it?.toDouble()?.div(100)).toString())
                }
            }
            R.id.btn_withdrawal -> {
                if (cbWexinPay.isChecked) {
                    walletViewModel.wxAuth(this)
                } else if (cbAliPay.isChecked) {
                    Toast.makeText(this, "暂不支持", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "请选择提现方式", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.cl_input -> {
                showSoftInputFromWindow(etMoney)
            }
        }
    }

    private fun onNewState(state: WalletViewState) {
        when(state.isLoading){
            true -> {
                Log.i(TAG,"onNewState loading")
            }
            false -> {
                Log.i(TAG,"onNewState loading finish")
            }
        }
    }

    private fun showSoftInputFromWindow(editText: EditText){
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val inputMethodManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText,0)
    }

}
