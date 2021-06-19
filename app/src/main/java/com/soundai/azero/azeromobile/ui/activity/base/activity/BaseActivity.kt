package com.soundai.azero.azeromobile.ui.activity.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.umeng.message.PushAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushAgent.getInstance(this).onAppStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}