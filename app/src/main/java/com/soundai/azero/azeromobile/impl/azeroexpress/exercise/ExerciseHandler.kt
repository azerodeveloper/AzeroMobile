package com.soundai.azero.azeromobile.impl.azeroexpress.exercise

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.impl.azeroexpress.AzeroExpressInterface
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.manager.ExerciseDataUploader
import com.soundai.azero.azeromobile.ui.activity.runner.RunningActivity
import com.soundai.azero.lib_todayrunrecord.IRunRecorderInterface
import com.soundai.azero.lib_todayrunrecord.TodayRunManager
import com.soundai.azero.lib_todaystepcounter.ISportStepInterface
import com.soundai.azero.lib_todaystepcounter.TodayStepManager
import org.json.JSONException
import org.json.JSONObject

class ExerciseHandler(val context: Context) : AzeroExpressInterface {
    private var iRunRecorderInterface: IRunRecorderInterface? = null
    private var isRunConnect = false
    private var isStepConnect = false

    private val runConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            iRunRecorderInterface = null
            ExerciseDataUploader.iRunRecorderInterface = null
            isRunConnect = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            iRunRecorderInterface = IRunRecorderInterface.Stub.asInterface(service)
            ExerciseDataUploader.iRunRecorderInterface = iRunRecorderInterface
        }
    }

    private val stepConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            ExerciseDataUploader.iSportStepInterface = null
            isStepConnect = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            ExerciseDataUploader.iSportStepInterface =
                ISportStepInterface.Stub.asInterface(service)
        }
    }

    init {
        isRunConnect = TodayRunManager.bindService(context, runConnection)
        isStepConnect = TodayStepManager.bindService(context, stepConnection)
        ExerciseDataUploader.startUpload()
    }

    override fun handleDirective(payload: JSONObject) {
        try {
            val type = payload.getString("type")
            val action = payload.getString("action")
            log.d("$payload")
            when (type) {
                "Run" -> handleRunDirective(action)
                "Walk" -> handleWalkDirective()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleWalkDirective() {

    }

    private fun handleRunDirective(action: String) {
        when (action) {
            "begin" -> {
                //开始跑步
            }
            "pause" -> iRunRecorderInterface?.pause()
            "resume" -> iRunRecorderInterface?.resume()
            "finish" -> {
                iRunRecorderInterface?.finish()
                ActivityLifecycleManager.getInstance().topActivity.let {
                    val intent =
                        Intent(it, RunningActivity::class.java).apply { putExtra("isFinish", true) }
                    it.startActivity(intent)
                }
            }
        }
    }

    fun release() {
        if (isRunConnect) context.unbindService(runConnection)
        if (isStepConnect) context.unbindService(stepConnection)
    }
}