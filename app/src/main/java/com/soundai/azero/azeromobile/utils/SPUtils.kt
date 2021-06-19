package com.soundai.azero.azeromobile.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.Constant.BOND_STATE
import com.soundai.azero.azeromobile.Constant.SAVE_PHONE_NUMBER
import com.soundai.azero.azeromobile.TaApp

object SPUtils {
    private const val APP_SHARD = "AZMOBILE"
    private const val APP_SHARD_FIRST_INSTALL = "AZMOBILE.INSTALL"
    private const val APP_SHARD_SPHERE_MODEL = "AZMOBILE.SPHEREVIEWMODEL"

    fun firstInstall(context: Context) {
        context.getSharedPreferences(APP_SHARD, Context.MODE_PRIVATE).edit()
            .putString(APP_SHARD_FIRST_INSTALL, "INSTALLED").apply()
    }

    fun isFirstInstall(context: Context): Boolean {
        return "INSTALLED" != context
            .getSharedPreferences(APP_SHARD, Context.MODE_PRIVATE)
            .getString(APP_SHARD_FIRST_INSTALL, "")
    }

    fun storeSphereViewModel(context: Context, payload: String) {
        context.getSharedPreferences(APP_SHARD, Context.MODE_PRIVATE).edit()
            .putString(APP_SHARD_SPHERE_MODEL, payload).apply()
    }

    fun fetchSphereViewModel(context: Context): String {
        return context.getSharedPreferences(APP_SHARD, Context.MODE_PRIVATE)
            .getString(APP_SHARD_SPHERE_MODEL, "")!!
    }

    fun saveAccountInfo(
        token: String?,
        userId: String?,
        phoneNumber: String
    ) = TaApp.application.getSharedPreferences(
        Constant.SHARDPREF_ACCOUNT,
        Context.MODE_PRIVATE
    ).edit(true) {
        token?.let { token ->
            putString(Constant.SAVE_TOKEN, token)
        }
        userId?.let { userId ->
            putString(Constant.SAVE_USERID, userId)
        }
        putString(SAVE_PHONE_NUMBER, phoneNumber)
    }

    fun saveBondState(bonded: Boolean) = TaApp.application.getSharedPreferences(
        Constant.SHARDPREF_ACCOUNT,
        Context.MODE_PRIVATE
    ).edit {
        putString(
            BOND_STATE, if (bonded) {
                "bonded"
            } else {
                null
            }
        )
    }

    fun getAccountPref(): SharedPreferences = TaApp.application.getSharedPreferences(
        Constant.SHARDPREF_ACCOUNT,
        Context.MODE_PRIVATE
    )
}