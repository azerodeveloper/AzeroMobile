package com.soundai.azero.azeromobile.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.ui.activity.wallet.WalletActivity
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WXEntryActivity: Activity(),IWXAPIEventHandler {

    private var wxApi: IWXAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wxApi = WXAPIFactory.createWXAPI(this, Constant.WX_APP_ID,false)
        wxApi?.handleIntent(intent,this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        wxApi?.handleIntent(intent,this)

    }

    override fun onResp(resp: BaseResp?) {
        Log.i("WXEntryActivity","resp: ${resp?.type}")
        if(resp?.type == ConstantsAPI.COMMAND_SENDAUTH){
            val authResp = resp as SendAuth.Resp
            authResp.code?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    val intent = Intent(this@WXEntryActivity,WalletActivity::class.java)
                    intent.putExtra("wxCode",it)
                    startActivity(intent)
                }
            }
        }
        finish()
    }

    override fun onReq(p0: BaseReq?) {

    }
}