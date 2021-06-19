package com.soundai.azero.azeromobile.network

import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.toast
import com.soundai.azero.azeromobile.ui.activity.login.LoginActivity
import com.soundai.azero.azeromobile.utils.SPUtils
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

inline fun createRetrofit(
    url: String,
    okHttpClientCreator: () -> OkHttpClient
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClientCreator())
        .build()
}

val defaultOkHttpClientCreator = fun() = OkHttpClient.Builder().build()

val surrogateOkHttpClientCreator: () -> OkHttpClient =
    fun() = OkHttpClient.Builder().authenticator { _, _ ->
        Log.i("authenticator", "token is invalid, need login!")
        ActivityLifecycleManager.getInstance().topActivity.let { activity ->
            SPUtils.getAccountPref().edit { clear() }
            CoroutineScope(Dispatchers.Main).launch { activity.toast("登录信息失效，请重新登陆。") }
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            null
        }
    }.build()