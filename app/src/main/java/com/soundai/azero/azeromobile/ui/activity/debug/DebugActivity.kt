package com.soundai.azero.azeromobile.ui.activity.debug

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.azero.platforms.core.PlatformInterface
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.Logger.LoggerHandler
import com.soundai.azero.azeromobile.TaApp.Companion.isConfigAllowToSaveLog
import com.soundai.azero.azeromobile.TaApp.Companion.isConfigAllowToSavePcm
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.activity.base.activity.BaseSwipeActivity

/**
 * Description ：用于帮助我们提升界面，主要包括是否保存日志、原始音频等
 * Created by SoundAI jiesean on 2020-05-29.
 */

class DebugActivity : BaseSwipeActivity(), View.OnClickListener {
    private val TAG = DebugActivity::class.java.simpleName

    private val btnDebugSet: Button by lazy { findViewById<Button>(R.id.btn_debug_set) }
    private val switchSaveLog: Switch by lazy { findViewById<Switch>(R.id.switch_save_log) }
    private val switchSavePcm: Switch by lazy { findViewById<Switch>(R.id.switch_save_pcm) }
    private val tipSavePcm: TextView by lazy { findViewById<TextView>(R.id.tv_tips_savepcm) }

    private var isAllowedToSaveLog :Boolean= false
    private var isAllowedToSavePcm :Boolean= false

    private lateinit var loggerHandler :PlatformInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        switchSavePcm.setOnClickListener(this)

        btnDebugSet.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()

        switchSaveLog.isChecked = isConfigAllowToSaveLog
        switchSavePcm.isChecked = isConfigAllowToSavePcm

    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btn_debug_set ->{
                isAllowedToSaveLog = switchSaveLog.isChecked
                isAllowedToSavePcm = switchSavePcm.isChecked

                Log.e("Azero.SDK.${TAG}","save config isAllowedToSaveLog = ${isAllowedToSaveLog}, isAllowedToSavePcm = ${isAllowedToSavePcm}")
                isConfigAllowToSavePcm = isAllowedToSavePcm
                isConfigAllowToSaveLog = isAllowedToSaveLog
                loggerHandler = AzeroManager.getInstance().getHandler("LoggerHandler")
                if(loggerHandler != null){
                    (loggerHandler as LoggerHandler).setDebug(isAllowedToSaveLog)
                }
                Toast.makeText(this,"已保存配置",Toast.LENGTH_SHORT).show()
            }
            R.id.switch_save_pcm ->{
                tipSavePcm.visibility = if(switchSavePcm.isChecked){View.VISIBLE}else{ View.GONE}
            }

        }

    }


}