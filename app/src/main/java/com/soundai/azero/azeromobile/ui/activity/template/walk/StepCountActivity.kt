package com.soundai.azero.azeromobile.ui.activity.template.walk

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.widget.TextView
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity
import com.soundai.azero.lib_todaystepcounter.ISportStepInterface
import com.soundai.azero.lib_todaystepcounter.TodayStepService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StepCountActivity : BaseDisplayCardActivity() {
    override val layoutResId: Int
        get() = R.layout.activity_query_steps
    private lateinit var tvSteps: TextView
    private lateinit var tvKilo: TextView
    private lateinit var tvCalorie: TextView

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName,
            service: IBinder
        ) { //Activity和Service通过aidl进行通信
            val iSportStepInterface = ISportStepInterface.Stub.asInterface(service)
            CoroutineScope(Dispatchers.Default).launch(coroutineExceptionHandler) {
                try {
                    while (service.isBinderAlive) {
                        val steps = iSportStepInterface.currentTimeSportStep
                        withContext(Dispatchers.Main) {
                            tvSteps.text = steps.toString()
                            tvKilo.text = getDistanceByStep(steps.toLong())
                            tvCalorie.text = getCalorieByStep(steps.toLong())
                        }
                        delay(3000)
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun initView() {
        tvSteps = findViewById(R.id.tv_step_count)
        tvKilo = findViewById(R.id.tv_kilometer)
        tvCalorie = findViewById(R.id.tv_calorie)
    }

    override fun initData(intent: Intent) {
        super.initData(intent)
        bindService(
            Intent(this, TodayStepService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    // 公里计算公式
    fun getDistanceByStep(steps: Long): String? {
        return String.format("%.2f", steps * 0.6f / 1000)
    }

    // 千卡路里计算公式
    fun getCalorieByStep(steps: Long): String? {
        return String.format("%.1f", steps * 0.6f * 60 * 1.036f / 1000)
    }
}