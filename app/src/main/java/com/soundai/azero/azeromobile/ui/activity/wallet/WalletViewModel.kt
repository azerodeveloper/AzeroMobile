package com.soundai.azero.azeromobile.ui.activity.wallet

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.network.Surrogate
import com.soundai.azero.azeromobile.postNext
import com.soundai.azero.azeromobile.setNext
import com.soundai.azero.azeromobile.ui.activity.base.viewmodel.BaseRequestViewModel
import com.soundai.azero.azeromobile.ui.activity.base.viewmodel.wxChecker
import com.soundai.azero.azeromobile.utils.MD5Utils
import com.soundai.azero.lib_surrogate.exception.RetrofitException
import com.soundai.azero.lib_surrogate.response.WXAccessResponse
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.util.*

class WalletViewModel(application: Application) : BaseRequestViewModel(application) {
    private val TAG: String = WalletViewModel::class.java.simpleName

    val stateLiveData: MutableLiveData<WalletViewState> = MutableLiveData(WalletViewState.initial())
    val balanceLiveData = MutableLiveData<Long>()
    val enableWithdrawalLiveData = MutableLiveData<Boolean>()
    val withdrawalSuccess = MutableLiveData<Boolean>(false)

    fun wxRequest(
        block: suspend () -> WXAccessResponse,
        success: suspend (WXAccessResponse) -> Unit,
        error: suspend (e: RetrofitException) -> Unit = {},
        complete: suspend () -> Unit = {}
    ) {
        request(block, success, error, complete, ::wxChecker)
    }

    private fun getSignString(params: SortedMap<String, String>): String {
        val builder = StringBuilder()
        for (key in params.keys) {
            val value = params[key]
            if (!value.isNullOrEmpty()) {
                builder.append(key).append("=").append(value).append("&")
            }
        }
        builder.append("secretKey=").append(Constant.APP_SECRET)
        return builder.toString()
    }

    fun inputBalance(value: Double) {
        enableWithdrawalLiveData.value = value.times(100).toLong() > balanceLiveData.value!!
    }

    fun getBalance(token: String, userId: String) {
        when (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
            true -> stateLiveData.postNext { state ->
                state.copy(
                    isLoading = true,
                    throwable = Throwable("Token或UserId为空")
                )
            }
            false -> {
                stateLiveData.postNext { state ->
                    state.copy(isLoading = true, throwable = null)
                }
                surrogateRequest(
                    {
                        Surrogate.getBalance(userId)
                    }, {
                        stateLiveData.postNext { state ->
                            state.copy(isLoading = false, throwable = null)
                        }
                        balanceLiveData.postValue(it.balance)
                    }, {
                        stateLiveData.postNext { state ->
                            state.copy(
                                isLoading = false,
                                throwable = Throwable(it.message)
                            )
                        }
                    }
                )
            }
        }
    }

    fun wxWithdrawal(
        token: String,
        userId: String,
        amount: Double,
        desc: String,
        appId: String,
        code: String
    ) {
        val amountL = (amount * 100).toLong()
        val applyNo = UUID.randomUUID().toString().replace("-", "")
        val treeMap =
            sortedMapOf(
                "amount" to amountL.toString(),
                "applyNo" to applyNo,
                "desc" to desc,
                "appId" to appId,
                "code" to code
            )
        val sign = MD5Utils.encodeString(getSignString(treeMap)).toUpperCase()
        surrogateRequest(
            { Surrogate.wxWithdrawal(amountL, applyNo, appId, desc, sign,code) },
            {
                stateLiveData.postNext { state ->
                    state.copy(isLoading = false, throwable = null)
                }
                withdrawalSuccess.postValue(true)
                getBalance(token, userId)
            }, {
                stateLiveData.postNext { state ->
                    state.copy(
                        isLoading = false,
                        throwable = Throwable(it.message)
                    )
                }
            }
        )
    }

    fun wxAuth(context: Context) {
        val wxApi = WXAPIFactory.createWXAPI(context.applicationContext, Constant.WX_APP_ID, false)
        if (wxApi.isWXAppInstalled) {
            val req = SendAuth.Req().apply {
                scope = "snsapi_userinfo"
                state = "none"
            }
            wxApi.sendReq(req)
        } else {
            stateLiveData.setNext { state ->
                state.copy(isLoading = false, throwable = Throwable("微信应用未安装"))
            }
        }
    }
}