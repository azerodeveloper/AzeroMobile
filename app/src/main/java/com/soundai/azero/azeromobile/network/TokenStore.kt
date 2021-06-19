package com.soundai.azero.azeromobile.network

import android.content.Context
import androidx.core.content.edit
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.TaApp

var surrogateToken: String
    get() = TaApp.application.getSharedPreferences(
        Constant.SHARDPREF_ACCOUNT,
        Context.MODE_PRIVATE
    ).getString(Constant.SAVE_TOKEN, "")!!
    set(value) = TaApp.application.getSharedPreferences(
        Constant.SHARDPREF_ACCOUNT,
        Context.MODE_PRIVATE
    ).edit { putString(Constant.SAVE_TOKEN, value) }