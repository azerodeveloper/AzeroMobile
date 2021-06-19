package com.soundai.azero.azeromobile.manager

import android.content.Context
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.common.bean.walk.StepData
import com.soundai.azero.azeromobile.sendSensorDataEvent
import com.soundai.azero.lib_todayrunrecord.IRunRecorderInterface
import com.soundai.azero.lib_todaystepcounter.ISportStepInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExerciseDataUploader {
    var iRunRecorderInterface: IRunRecorderInterface? = null
    var iSportStepInterface: ISportStepInterface? = null
    private var upLoadJob: Job? = null

    fun startUpload() {
        when {
            upLoadJob?.isActive == true -> {
                log.d("upLoad isActive")
            }
            else -> {
                log.d("upLoad start")
                upLoadJob = setUpLoadJob()
                upLoadJob!!.start()
            }
        }
    }

    private fun setUpLoadJob() =
        CoroutineScope(Dispatchers.Default).launch {
            while (this.isActive) {
                val stepCount = iSportStepInterface?.currentTimeSportStep
                val runRecord = iRunRecorderInterface?.currentRunData
                val stepData = stepCount?.let { steps ->
                    val simpleDateFormat =
                        SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                    val date = Date(System.currentTimeMillis())
                    StepData(
                        simpleDateFormat.format(date),
                        steps,
                        (steps * 0.6f * 60 * 1.036f / 1000).toInt(),
                        steps * 0.6
                    )
                }
                if (stepData != null || runRecord != null) {
                    sendSensorDataEvent(runRecord, stepData)
                }
                var delayTime = 60 * 1000L
                if (runRecord != null) {
                    delayTime = 30 * 1000L
                }
                delay(delayTime)
            }
        }
}