/*
 * Copyright (c) 2019 SoundAI. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.soundai.azero.azeromobile.impl.audioinput

import android.content.Context
import android.os.CountDownTimer
import com.azero.sdk.AzeroManager
import com.azero.sdk.util.log
import com.konovalov.vad.Vad
import com.konovalov.vad.VadConfig
import com.konovalov.vad.VadListener
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.impl.audioinput.record.Record
import com.soundai.azero.azeromobile.impl.audioinput.record.SystemRecord
import com.soundai.azero.azeromobile.system.TaAudioManager
import com.soundai.azero.azeromobile.utils.CopyConfigTask
import com.soundai.azero.azeromobile.utils.FileUtils
import com.soundai.open_denoise.denoise.SaiClient

/**
 * SoundAI算法库模块
 * 用于处理语音数据，可提供ASR数据及VoIP数据
 */
class OpenDenoiseManager(
    private val context: Context, //数据源
    val mRecord: Record, enableLocalVad: Boolean, //算法库回调
    private val denoiseCallback: DenoiseCallback?
) {
    //core
    private var saiClient: SaiClient? = null
    private var vad: Vad? = null
    //是否处于唤醒状态
    var isWakeUp = false
        private set
    //过滤首个VAD事件
    private var filterFirstVAD = false
    //过滤唤醒事件
    private var filterWakeup = false
    //是否开启本地VAD
    private var mEnableLocalVad = true
    //唤醒角度
    private var angle = 0f
    //前次VAD事件
    private var lastVAD = -1
    //唤醒后未检测到人声，超时时间
    private val VADBEGINTIMEOUT = 4 * 1000
    //唤醒后检测到人声，截断超时时间
    private val VADENDTIMEOUT = 10 * 1000
    private val VADCUTTIMEOUT_WAKEFREE = 3 * 1000
    //静态变量
    private val VAD_BEGIN = 0
    private val VAD_ENG = 1
    private var index = 2

    private val DEFAULT_SAMPLE_RATE: VadConfig.SampleRate = VadConfig.SampleRate.SAMPLE_RATE_16K
    private val DEFAULT_FRAME_SIZE: VadConfig.FrameSize = VadConfig.FrameSize.FRAME_SIZE_320
    private val DEFAULT_MODE: VadConfig.Mode = VadConfig.Mode.VERY_AGGRESSIVE
    private val DEFAULT_SILENCE_DURATION = 200
    private val DEFAULT_VOICE_DURATION = 200

    private val vadbeginTimer = object : CountDownTimer(VADBEGINTIMEOUT.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (!mEnableLocalVad) {
                return
            }
            lastVAD = VAD_ENG
            denoiseCallback?.onVadCallback(VAD_ENG)
            log.e("VAD Begin Timeout!")
        }
    }

    private val vadEndTimer = object : CountDownTimer(VADENDTIMEOUT.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (!mEnableLocalVad) {
                return
            }
            lastVAD = VAD_ENG
            denoiseCallback?.onVadCallback(VAD_ENG)
            log.e("VAD End Timeout!")
        }
    }

    private val vadCutWhileWakeFreeMode = object : CountDownTimer(VADCUTTIMEOUT_WAKEFREE.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (TaApp.isEarphoneMode) {
                log.e("VAD CUT from Open denoise Manager because it up to TIMEOUT!")
                denoiseCallback?.onVadCallback(VAD_ENG)
                lastVAD = 1
                vad?.resetVad()
            }
        }
    }

    init {
        when(TaApp.denoise_tye) {
            0-> {
                saiClient = SaiClient.getInstance()
                mEnableLocalVad = enableLocalVad
                val assertsDirName: String
                if (mRecord is SystemRecord) {
                    assertsDirName = "config"
                } else {
                    assertsDirName = "headsetconfig"
                }
                log.e("初始化算法库：record type ${mRecord::class.java}, assertsDirName = ${assertsDirName}")
                CopyConfigTask(context, assertsDirName)
                    .setConfigListener(object :
                        CopyConfigTask.ConfigListener {
                        override fun onSuccess(configPath: String) {
                            if (initSaiClient(configPath, denoiseCallback) > 0) {
                                log.e("OpenDenoise init Error!")
                            } else {
                                log.e("OpenDenoise init Succeed")
                                startAudioInput()
                                if (TaApp.isEarphoneMode) {
                                    startPhoneMode()
                                }
                            }
                        }

                        override fun onFailed(errorMsg: String) {
                            log.d("onFailed: $errorMsg")
                        }
                    }).execute()
            }
            1->{
                log.e("OpenDenoise Manager start webrtc vad")
                var config: VadConfig? = null
                config = VadConfig.newBuilder()
                    .setSampleRate(DEFAULT_SAMPLE_RATE)
                    .setFrameSize(DEFAULT_FRAME_SIZE)
                    .setMode(DEFAULT_MODE)
                    .setSilenceDurationMillis(DEFAULT_SILENCE_DURATION)
                    .setVoiceDurationMillis(DEFAULT_VOICE_DURATION)
                    .build()
                vad = Vad(config,object :VadListener{
                    override fun onVadEventCallback(p0: Int)  {
                        log.e("OpenDenoise VadListener() vad event = ${p0}")
                        if(p0 == 1){
                            vadCutWhileWakeFreeMode.start()
                            denoiseCallback?.onWakeUp("xiaoyixiaoyi")
                        }else{
                            vadCutWhileWakeFreeMode.cancel()
                            denoiseCallback?.onVadCallback(VAD_ENG)
                        }
                    }

                    override fun onDataCallback(p0: ByteArray?) {
                        denoiseCallback?.onAsrData(p0!!, p0.size)
                        if (TaApp.isConfigAllowToSavePcm) {
                            FileUtils.writeFile(p0!!,"/sdcard/asr_origin.pcm",true);
                        }
                    }
                })
                startAudioInput()
                vad?.start()
                vad?.setDebug(true)
            }
            2 ->{

            }
        }
        mRecord.setDataListener(object : Record.Listener {
            override fun onData(data: ByteArray, size: Int) {
//                if (ActivityLifecycleManager.getInstance().topActivity is LauncherActivity) {
//                    if (WakeupButton.getInstance(context).SAVE_DATA) {
//                        if (mRecord is SystemRecord) {
//                            FileUtils.writeFile(data, "/sdcard/stereo.pcm", true)
//                        } else {
//                            FileUtils.writeFile(data, "/sdcard/mono.pcm", true)
//                        }
//                    }
//                }
                saiClient?.feedData(data)
            }

            override fun onData(data: ShortArray, size: Int) {
                vad?.isContinuousSpeech(data)
            }
        })
    }

    private fun initSaiClient(configPath: String, denoiseCallback: DenoiseCallback?): Int {
        return saiClient!!.init(
            context,
            true,
            configPath,
            "ViewPageHelper",
            AzeroManager.getInstance().generateToken(context),
            object : SaiClient.Callback {
                override fun onAsrDataCallback(data: ByteArray, size: Int) {
                    denoiseCallback?.onAsrData(data, size)
                }

                override fun onVoipDataCallback(bytes: ByteArray, size: Int) {
                    denoiseCallback?.onVoIpData(bytes, size)
                }

                override fun onWakeupCallback(
                    wakeup_angle: Float,
                    wakeup_word: String,
                    score: Float,
                    data: ByteArray
                ) {
                    if (!TaApp.isEarphoneMode) {
                        log.e("wakeup word = " + wakeup_word)
                        when (wakeup_word) {
                            "xiaoyixiaoyi" -> {
//                                if (filterWakeup) {
//                                    filterWakeup = false
//                                    return
//                                }
                                log.d("=====EarMode Wake up!angle:" + wakeup_angle + "word:" + wakeup_word)
                                angle = wakeup_angle
                                isWakeUp = true
                                filterFirstVAD = false
                                denoiseCallback?.onWakeUp(wakeup_word)
                                if (lastVAD != VAD_BEGIN) {
                                    vadEndTimer.cancel()
                                    vadbeginTimer.cancel()
                                    vadbeginTimer.start()
                                }
                            }
                            else -> {
                                denoiseCallback?.onWakeUp(wakeup_word)
                            }
                        }
                    }
                }

                override fun onVadCallback(vadResult: Int) {
                    log.e("localVad:$vadResult BT = ${!TaAudioManager.isBTHeadsetConnected()}, lastVad = ${lastVAD}")
                    if (!mEnableLocalVad || !TaAudioManager.isBTorWiredHeadsetConnected()) {
                        return
                    }
                    if (TaApp.isEarphoneMode) {
                        if (vadResult == VAD_BEGIN && lastVAD != 0) {
                            vadCutWhileWakeFreeMode.start()
                            denoiseCallback?.onWakeUp("xiaoyixiaoyi")
                        } else if (vadResult == VAD_ENG || vadResult == 2) {
                            vadCutWhileWakeFreeMode.cancel()
                            denoiseCallback?.onVadCallback(VAD_ENG)
                        }
                        lastVAD = vadResult
                    }else{
                        log.d("localVad:$vadResult")
                        if (filterFirstVAD) {
                            filterFirstVAD = false
                            log.e("filter")
                            return
                        }
                        denoiseCallback?.onVadCallback(vadResult)
                        if (vadResult == VAD_BEGIN) {
                            log.d("lastVAD: $lastVAD")
                            //重复收到vadBegin事件，避免重新计时
                            if (lastVAD != VAD_BEGIN) {
                                vadbeginTimer.cancel()
                                vadEndTimer.start()
                            }
                        } else if (vadResult == VAD_ENG) {
                            vadEndTimer.cancel()
                        }
                        lastVAD = vadResult
                    }
                }
            }
        )
    }

    interface DenoiseCallback {
        fun onAsrData(data: ByteArray, size: Int)

        fun onWakeUp(wakeWord: String)

        fun onVoIpData(data: ByteArray, size: Int)

        /**
         * return local VAD result
         * 0 vadBegin
         * 1 vadEnd
         */
        fun onVadCallback(result: Int)
    }

    fun startVoip() {
        saiClient?.startVoip()
    }

    fun stopVoip() {
        saiClient?.stopVoip()
    }

    fun startPhoneMode(){
        saiClient?.startBeam(90f)
        filterFirstVAD = true
    }
    fun exitPhoneMode(){
        saiClient?.stopBeam()
        filterWakeup = false
    }

    fun startRecognize() {
        if (TaApp.isEarphoneMode) {
            log.e("localVad: recognize start!")
            //只需要数据回调，阻止唤醒事件
            filterWakeup = true
        }else{
            saiClient?.startBeam(angle)
            filterFirstVAD = true
            //只需要数据回调，阻止唤醒事件
            filterWakeup = true
        }
    }

    fun stopRecognize() {
        log.e("localVad: recognize stop!")
        saiClient?.stopBeam()
        if (TaApp.isEarphoneMode) {
            saiClient?.startBeam(90f)
            filterFirstVAD = true
        }
        isWakeUp = false
    }

    fun startAudioInput() {
        mRecord.start()
    }

    fun stopAudioInput() {
        mRecord.stop()
    }

    fun isRecording():Boolean {
        return mRecord.isRecording()
    }

    fun release() {
        mRecord.stop()
        mRecord.release()
        saiClient?.release()
    }

    //快捷词
    interface QuickWordListener {
        //接通电话
        fun onGetThrough()

        //挂断电话
        fun onHangUp()

        //继续播放
        fun onPlayResume()

        //暂停播放
        fun onPlayPause()

        //下一个
        fun onNext()
    }
}