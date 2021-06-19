package com.soundai.azero.azeromobile.utils

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.azero.platforms.iface.config.AzeroConfiguration
import com.azero.sdk.AzeroManager
import com.azero.sdk.Config
import com.azero.sdk.HandlerContainerBuilder
import com.azero.sdk.event.Command
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler
import com.azero.sdk.impl.MediaPlayer.RawSpeakAudioMediaPlayerHandler
import com.azero.sdk.util.executors.AppExecutors
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.impl.audioinput.AudioInputManager
import com.soundai.azero.azeromobile.impl.audioinput.SpeechRecognizerHandler
import com.soundai.azero.azeromobile.impl.audioinput.record.MonoSystemRecord
import com.soundai.azero.azeromobile.impl.azeroexpress.AzeroExpressHandler
import com.soundai.azero.azeromobile.impl.azeroexpress.exercise.ExerciseHandler
import com.soundai.azero.azeromobile.impl.azeroexpress.navigation.NavigationHandler
import com.soundai.azero.azeromobile.impl.networkstatedetection.NetworkStateDetectionImpl
import com.soundai.azero.azeromobile.impl.phonecallcontroller.PhoneCallControllerImpl
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.service.network.NetworkStateDetectionHandler
import com.soundai.azero.azeromobile.system.SpeakAudioCoordinator

object AzeroHelper {
    private var mRawSpeakAudioMediaPlayerListener: LocalRawSpeakAudioMediaPlayerListener =
        LocalRawSpeakAudioMediaPlayerListener()
    private var mMediaPlayerHandler: MediaPlayerHandler? = null
    private var isMediaPlayerStopped = false
    private var mContext: Context? = null
    private var speakAudioCoordinator: SpeakAudioCoordinator? = null
    var handler: Handler = Handler(Looper.getMainLooper())

    private val waitForSecondTtsTimeout = object : CountDownTimer(2*500, 500) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            ActivityLifecycleManager.getInstance()
                .clearChannel(ActivityLifecycleManager.ChannelName.TEMPLATE)
        }
    }

    fun startAzeroService(context: Context, wakeUpConsumer: AudioInputManager.WakeUpConsumer) {
        if (AzeroManager.getInstance().isEngineInitComplete) return
        mContext = context
        try {
            val appExecutors = AppExecutors()
            AzeroManager.getInstance().setLogLevel(4)
            AzeroManager.getInstance().setDebug(true)

            //第一步 配置参数 注册必要模块 @{
            val config = Config(
                TaApp.productId, //productID 网站申请
                TaApp.clientId, //ClientID  网站申请
                Utils.getimei(context), //DeviceSN 传入Mac地址或IMEI号，必须保证设备唯一
                TaApp.server, //Server    选择使用的服务器  FAT 测试环境 PRO 正式环境
                true          //localVAD  是否使用本地VAD
            )
            //定义界面消失时间，不填则使用如下默认值
            config.timeoutList = arrayOf(
                //Template界面在TTS播放完后消失的时间
                AzeroConfiguration.TemplateRuntimeTimeout(
                    AzeroConfiguration.TemplateRuntimeTimeoutType.DISPLAY_CARD_TTS_FINISHED_TIMEOUT,
                    300
                ),
                //音频播放完后界面消失时间
                AzeroConfiguration.TemplateRuntimeTimeout(
                    AzeroConfiguration.TemplateRuntimeTimeoutType.DISPLAY_CARD_AUDIO_PLAYBACK_FINISHED_TIMEOUT,
                    300000
                ),
                //音频播放暂停时界面消失时间
                AzeroConfiguration.TemplateRuntimeTimeout(
                    AzeroConfiguration.TemplateRuntimeTimeoutType.DISPLAY_CARD_AUDIO_PLAYBACK_STOPPED_PAUSED_TIMEOUT,
                    8000
                )
            )

            //初始化数据读取模块
//            val record = if (BluetoothUtil.isBTHeadsetConnected()) {
//                MonoSystemRecord()
//            } else {
//                SystemRecord()
//            }
            val record = MonoSystemRecord()
            val audioInputManager =
                AudioInputManager.getInstance().setOpenDenoise(context, record, false)
            audioInputManager.addWakeUpObserver(wakeUpConsumer)
            //识别数据模块
            val speechRecognizerHandler = SpeechRecognizerHandler(
                appExecutors,
                context,
                audioInputManager,
                true,
                true
            )

            //选择和注册必要模块
            val handlerContainer = HandlerContainerBuilder(context)
                .setAudioHandler(HandlerContainerBuilder.AUDIO.SOUNDAI)
                .setMusicHandler(HandlerContainerBuilder.MUSIC.SOUNDAI)
                .setVideoHandler(HandlerContainerBuilder.VIDEO.MIFENG)
                .setPhoneCallHandler(HandlerContainerBuilder.PHONE.PHONE)
                .setSpeechRecognizer(speechRecognizerHandler) //必须注册识别模块
                .create()
            //@}

            //第二歩 启动引擎 @{
            AzeroManager.getInstance().startEngine(context, config, handlerContainer)
            AzeroManager.getInstance().setInteractMode(1)
//            if (MyApplication.isFirstIntall) {
//                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_WELCOME_1)
//            }else{
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_WELCOME_2)
//            }
//            initTemplateDistributor()
            registerCustomerAgent()

            mMediaPlayerHandler =
                AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER) as MediaPlayerHandler
            val rawAudioMediaPlayerHandler: RawSpeakAudioMediaPlayerHandler =
                AzeroManager.getInstance().getHandler(AzeroManager.SPEAKER_HANDLER) as RawSpeakAudioMediaPlayerHandler
            speakAudioCoordinator = SpeakAudioCoordinator(rawAudioMediaPlayerHandler, mMediaPlayerHandler!!)
            rawAudioMediaPlayerHandler.setRawSpeakAudioMediaPlayerListener(
                mRawSpeakAudioMediaPlayerListener
            )
            val phoneCallControllerHandler = PhoneCallControllerImpl(context)

            //开始监听测试网络状态
            NetworkStateDetectionHandler.instance.init(context)
            NetworkStateDetectionImpl(context.applicationContext, appExecutors)
            //@}
        } catch (e: RuntimeException) {
            log.e("Could not start engine. Reason: " + e.message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAudioMute(mute: Boolean) {
        speakAudioCoordinator?.audioMute = mute
    }

    private class LocalRawSpeakAudioMediaPlayerListener :
        RawSpeakAudioMediaPlayerHandler.RawSpeakAudioMediaPlayerListener {
        override fun play() {
            Log.d("Azero.SDK", "tts play ${mMediaPlayerHandler?.isPlaying}")
            if (TaApp.isEarphoneMode) {

                if (mMediaPlayerHandler?.isPlaying!!) {
                    Log.e("Azero.SDK", " ask CMD_PLAY_PAUSE")
//                    handler.post{AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)}
//                mMediaPlayerHandler?.pause()

                    isMediaPlayerStopped = true
                }
            }
        }

        override fun prepare() {
            Log.d("Azero.SDK", " tts prepare ${mMediaPlayerHandler?.isPlaying}")
            if (TaApp.isEarphoneMode) {
                waitForSecondTtsTimeout.cancel()
                if (mMediaPlayerHandler?.isPlaying!!) {
                    Log.e("Azero.SDK", " ask CMD_PLAY_PAUSE")
                }
            }
        }

        override fun stop() {
            Log.d("Azero.SDK", " tts stop ")
        }

        override fun pause() {
            Log.d("Azero.SDK", " tts pause ")
        }

        override fun resume() {
            Log.d("Azero.SDK", " tts resume ")
        }

        override fun onSpeakerMediaPlayerStarted() {
            Log.d("Azero.SDK", " tts onSpeakerMediaPlayerStarted ")
        }

        override fun seekTo(p0: Long) {
        }

        override fun onSpeakerMediaPlayerStopped() {
            Log.d("Azero.SDK", " tts PlayerStopped ")
            if (TaApp.isEarphoneMode) {
                waitForSecondTtsTimeout.start()
                if (isMediaPlayerStopped) {
                    Log.e("Azero.SDK", " ask CMD_PLAY_PLAY")
//                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY)
//                mMediaPlayerHandler?.resume()
//                mMediaPlayerHandler?.speaker?.setVolume(70)
//                    if (mMediaPlayerHandler?.isPlaying!!) {
//                        mMediaPlayerHandler?.resume()
//                    }
//                    isMediaPlayerStopped = false
                }
            }
        }

    }

    private fun registerCustomerAgent() {
        //自定义内容模块
        mContext?.let {
            val mAzeroExpressHandler = AzeroExpressHandler(it)
            mAzeroExpressHandler.navigationHandler = NavigationHandler(it)
            mAzeroExpressHandler.exerciseHandler = ExerciseHandler(it)
            AzeroManager.getInstance().customAgent = mAzeroExpressHandler

        }
    }
}