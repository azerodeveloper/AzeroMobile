package com.soundai.azero.azeromobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.azero.platforms.iface.AlexaClient
import com.azero.sdk.AzeroManager
import com.azero.sdk.Config
import com.azero.sdk.event.Command
import com.azero.sdk.impl.Alerts.AlertsHandler
import com.azero.sdk.impl.AzeroClient.AzeroClientHandler
import com.azero.sdk.util.executors.AppExecutors
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.impl.audioinput.AudioInputManager
import com.soundai.azero.azeromobile.impl.audioinput.SpeechRecognizerHandler
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.service.NotificationService
import com.soundai.azero.azeromobile.service.TaPushService
import com.soundai.azero.azeromobile.system.TaAudioManager
import com.soundai.azero.azeromobile.ui.Setting
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity
import com.soundai.azero.azeromobile.ui.widget.WakeupButton
import com.soundai.azero.azeromobile.utils.AzeroHelper
import com.soundai.azero.azeromobile.utils.SPUtils
import com.soundai.azero.azeromobile.utils.Utils
import com.soundai.azero.lib_todayrunrecord.TodayRunManager
import com.soundai.azero.lib_todaystepcounter.TodayStepManager
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent

/**
 * Create by xingw on 2019/10/31
 */
class TaApp : Application(), AudioInputManager.WakeUpConsumer {
    private val appExecutors = AppExecutors()

    companion object {
        lateinit var application: TaApp
        var isEarphoneMode: Boolean = true
        var isFirstIntall: Boolean = true
        var isInNoneedToNotifyEngineState: Boolean = false
        var isHelloSkill = false
        var isConfigAllowToSaveLog: Boolean = false
        var isConfigAllowToSavePcm: Boolean = false
        var denoise_tye: Int = 0
        var productId = Constant.PRO_PRODUCTID
        var clientId = Constant.PRO_CLIENTID
        var server = Config.SERVER.PRO
    }

    override fun onCreate() {
        super.onCreate()
        application = this

        UMConfigure.init(getApplicationContext(), UMConfigure.DEVICE_TYPE_PHONE, null)
        UMConfigure.setLogEnabled(true)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)

        if (Utils.getVersion(this)!!.contains("FAT")) {
            productId = Constant.FAT_PRODUCTID
            clientId = Constant.FAT_CLIENTID
            server = Config.SERVER.FAT
        }

//        BluetoothUtil.startAutoBluetoothSco(this)
        isFirstIntall = SPUtils.isFirstInstall(this)
        if (isFirstIntall) {
            SPUtils.firstInstall(this)
        }
        registerActivityLifecycleCallbacks(ActivityLifecycleManager.getInstance())
        startNotification()
        AzeroManager.getInstance().setLogLevel(log.LOG_LEVEL_DEBUG)
        startExerciseCounter()

        // 友盟初始化
        initUmeng()
    }

    private fun initUmeng() {
        UMConfigure.init(
            this,
            "5ea92382978eea07a9783133",
            "Umeng",
            UMConfigure.DEVICE_TYPE_PHONE,
            "9667f12b59f4cab0acfd3c416729bd64"
        )

        val mPushAgent = PushAgent.getInstance(this)
        mPushAgent.register(object : IUmengRegisterCallback {
            override fun onSuccess(deviceToken: String) {
                log.i("注册成功：deviceToken：-------->  $deviceToken")
            }

            override fun onFailure(s: String, s1: String) {
                log.e("注册失败：-------->  s:$s, s1:$s1")
            }
        })

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    "azero_push",
                    "azero_push",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        mPushAgent.setPushIntentServiceClass(TaPushService::class.java)
    }

    private fun startExerciseCounter() {
        //初始化计步模块
        TodayStepManager.startTodayStepService(this)
        TodayRunManager.startTodayRunService(this)
    }

    private fun startNotification() {
        //后台和锁屏时进行录音
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, NotificationService::class.java))
        } else {
            startService(Intent(this, NotificationService::class.java))
        }
    }

    fun startAzero() {
        AzeroHelper.startAzeroService(this.applicationContext, this)
    }

    /**
     * 唤醒时触发
     *
     * @param wakeWord 唤醒词
     */
    override fun onWakewordDetected(wakeWord: String) {
        //如果闹钟正在响，停止闹钟
        stopAlerts()
//        if (!checkNetConnectState()) return
        when (wakeWord) {
            //接通电话
            "jietongdianhua" -> onGetThrough()
            //挂断电话
            "guaduandianhua" -> onHangUp()
            //继续播放
            "jixubofang" -> onPlayResume()
            //暂停播放
            "zantingbofang" -> onPlayPause()
            //下一个
            "xiayige" -> onNext()
            else -> onNomalWakeUp()
        }
    }

    private fun onNext() {
        log.d("wakeup")
        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)
        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_NEXT)
//        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_CUE_NEXT)
    }

    private fun onPlayPause() {
        log.d("wakeup")
        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)
    }

    private fun onPlayResume() {
        log.d("wakeup")
        AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY)
    }

    private fun onHangUp() {
        log.d("wakeup")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ANSWER_PHONE_CALLS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val telecomManager =
                    this.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.endCall()
            }
        } else {
            val telephonyManager =
                this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val endCallMethod = telephonyManager.javaClass.getDeclaredMethod(
                "endCall"
            )
            endCallMethod.isAccessible = true
            endCallMethod.invoke(telephonyManager)
        }
    }

    private fun onGetThrough() {
        log.d("wakeup")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ANSWER_PHONE_CALLS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val telecomManager =
                    this.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                telecomManager.acceptRingingCall()
            }
        } else {
            val telephonyManager =
                this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val answerRingCallMethod = telephonyManager.javaClass.getDeclaredMethod(
                "answerRingingCall"
            )
            answerRingCallMethod.isAccessible = true
            answerRingCallMethod.invoke(telephonyManager)
        }
    }

    private fun onNomalWakeUp() {
        log.d("wakeup")
        if (Setting.enableLocalVAD) {
            //本地判断VADEnd
            (AzeroManager.getInstance()
                .getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER) as SpeechRecognizerHandler).onHoldToTalk()
        } else {
            //云端判断VADEnd
            (AzeroManager.getInstance()
                .getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER) as SpeechRecognizerHandler).onTapToTalk()
        }
    }

    private fun checkNetConnectState(): Boolean {
        val mAzeroClient =
            AzeroManager.getInstance()
                .getHandler(AzeroManager.AZERO_CLIENT_HANDLER) as AzeroClientHandler
        //确认已与服务器连接
        if (mAzeroClient.connectionStatus == AlexaClient.ConnectionStatus.CONNECTED) {
            return true
        } else {
            if (isInNoneedToNotifyEngineState) {
                return false
            }
            val message =
                "Wakeword Detected but AlexaClient not connected. ConnectionStatus: " + mAzeroClient.connectionStatus
            appExecutors.mainThread().execute {
                if (ActivityLifecycleManager.getInstance().topActivity is LauncherActivity) {
                    WakeupButton.getInstance(this).displayErrorAnimation()
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_SERVER_ERROR)
                }
            }
            log.w(message)
        }
        return false
    }

    private fun stopAlerts() {
        val alerts =
            if (AzeroManager.getInstance().getHandler(AzeroManager.ALERT_HANDLER) != null) {
                AzeroManager.getInstance().getHandler(AzeroManager.ALERT_HANDLER) as AlertsHandler
            } else {
                null
            }
        alerts?.localStop()
    }

    override fun onTerminate() {
        super.onTerminate()
        TaAudioManager.destroy()
    }
}


