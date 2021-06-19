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

import android.annotation.SuppressLint
import android.content.Context

import com.azero.sdk.impl.Common.InputManager
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.impl.audioinput.record.Record
import java.util.*
import java.util.LinkedList

/**
 * 算法库处理后的数据输送模块
 * 提供降噪数据和VoIP数据
 * 提供唤醒回调
 * 提供本地VAD回调（需要enableLocalVAD）
 */
class AudioInputManager private constructor() : InputManager(),
    OpenDenoiseManager.DenoiseCallback {
    //降噪音频数据回调
    private val mAudioInputConsumers: MutableList<InputManager.AudioInputConsumer> = ArrayList()
    //唤醒回调
    private val mWakeUpConsumers: MutableList<WakeUpConsumer> = ArrayList()
    //VoIP数据回调
    private val mVoipInputConsumers: MutableList<InputManager.VoipInputConsumer> = ArrayList()
    //算法库模块
    private var openDenoiseManager: OpenDenoiseManager? = null
    //Vad回调
    private var localVadListener: LocalVadListener? = null

    private var isFirstFrame = false
    private var asrData = LinkedList<ByteArray>()

    interface WakeUpConsumer {
        fun onWakewordDetected(wakeWord: String)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AudioInputManager? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: AudioInputManager().also { instance = it }
            }
    }

    override fun stopAudioInput(consumer: InputManager.AudioInputConsumer): Boolean {
        log.i("Stop recording request received from " + consumer.audioInputConsumerName)

        var consumersLeft = 0
        synchronized(mAudioInputConsumers) {
            mAudioInputConsumers.remove(consumer)
            consumersLeft = mAudioInputConsumers.size
        }

        if (consumersLeft == 0) {
            log.i("Stopping recording for the last client " + consumer.audioInputConsumerName)
            openDenoiseManager?.stopRecognize()
            return true
        }

        log.i("Audio recording wouldnt be stopped on account of remaining clients")
        return true
    }

    override fun startvoip(voipInputConsumer: InputManager.VoipInputConsumer) {
        onStartVoIp(voipInputConsumer)
    }

    override fun stopvoip(voipInputConsumer: InputManager.VoipInputConsumer) {
        onStopVoIp(voipInputConsumer)
    }

    override fun startAudioInput(consumer: InputManager.AudioInputConsumer): Boolean {
        log.i("Start recording request received from " + consumer.audioInputConsumerName)

        synchronized(mAudioInputConsumers) {
            if (TaApp.isEarphoneMode) {
                isFirstFrame = true
            }
            mAudioInputConsumers.add(consumer)
        }
        if (!TaApp.isEarphoneMode) {
            if (openDenoiseManager?.isWakeUp == true) {
                log.i("Audio recording already in progress")
                return true
            }
        }
        openDenoiseManager?.startRecognize()
        return true
    }

    fun onStopVoIp(consumer: InputManager.VoipInputConsumer) {
        log.i("Stop recording request received from " + consumer.voIpInputConsumerName)
        var consumersLeft = 0
        synchronized(mVoipInputConsumers) {
            mVoipInputConsumers.remove(consumer)
            consumersLeft = mVoipInputConsumers.size
        }

        if (consumersLeft == 0) {
            log.i("Stopping recording for the last client " + consumer.voIpInputConsumerName)
            openDenoiseManager?.stopVoip()
        }
        log.i("Audio recording wouldnt be stopped on account of remaining clients")
    }

    fun onStartVoIp(consumer: InputManager.VoipInputConsumer) {
        log.i("Start recording request received from " + consumer.voIpInputConsumerName)
        synchronized(mVoipInputConsumers) {
            mVoipInputConsumers.add(consumer)
        }
        openDenoiseManager?.startVoip()
    }

    fun addWakeUpObserver(consumer: WakeUpConsumer) {
        synchronized(mWakeUpConsumers) {
            mWakeUpConsumers.add(consumer)
        }
    }

    fun removeWakeUpObserver(consumer: WakeUpConsumer) {
        synchronized(mWakeUpConsumers) {
            mWakeUpConsumers.remove(consumer)
        }
    }


    private fun notifyDataAvailableToAudioInputConsumers(buffer: ByteArray, size: Int) {
        if (TaApp.isEarphoneMode) {
            var collectedData = LinkedList<ByteArray>()
            if (isFirstFrame) {
                 collectedData = collectAsrData(buffer,size)
            }else{
                collectAsrData(buffer,size)
            }
            synchronized(mAudioInputConsumers) {
                for (consumer in mAudioInputConsumers) {
                    if (isFirstFrame) {
                        var y = 0
                        while (y < collectedData.size){
                            consumer.onAudioInputAvailable(collectedData[y], size)
                            y++
                        }
                        isFirstFrame = false
                        asrData.clear()
                    }else{
                        consumer.onAudioInputAvailable(buffer, size)
                    }
                }
            }
        }else{
            synchronized(mAudioInputConsumers) {
                for (consumer in mAudioInputConsumers) {
                    consumer.onAudioInputAvailable(buffer, size)
                }
            }
        }
    }

    private fun collectAsrData(buffer: ByteArray, size: Int):LinkedList<ByteArray> {
        //Log.e("Azero.SDK", "collectAsrData size = ${asrData.size}")
        if (asrData.size >= 33) {
            asrData.pollFirst()
            asrData.add(buffer)
        } else {
            asrData.add(buffer)
        }
        return asrData
    }

    private fun notifyWakeUpToWakeUpConsumer(wakeWord: String) {
        synchronized(mAudioInputConsumers) {
            for (consumer in mWakeUpConsumers) {
                consumer.onWakewordDetected(wakeWord)
            }
        }
    }

    fun setLocalVadListener(localVadListener: LocalVadListener) {
        this.localVadListener = localVadListener
    }

    interface LocalVadListener {
        fun onLocalVadEnd()
    }

    private fun notifyDataAvailableToVoIpInputConsumers(data: ByteArray, size: Int) {
        synchronized(mVoipInputConsumers) {
            for (consumer in mVoipInputConsumers) {
                consumer.onVoIpInputAvailable(data, size)
            }
        }
    }

    override fun onAsrData(data: ByteArray, size: Int) {
        notifyDataAvailableToAudioInputConsumers(data, size)
    }

    override fun onWakeUp(wakeWord: String) {
        notifyWakeUpToWakeUpConsumer(wakeWord)
    }

    override fun onVadCallback(result: Int) {
        if (result == 1 && localVadListener != null) {
            localVadListener!!.onLocalVadEnd()
        }
    }

    override fun onVoIpData(data: ByteArray, size: Int) {
        notifyDataAvailableToVoIpInputConsumers(data, size)
    }

    fun setOpenDenoise(context: Context, record: Record, replace: Boolean): AudioInputManager {
//        if (openDenoiseManager != null) {
//            if ((openDenoiseManager!!.mRecord is SystemRecord && record is SystemRecord)
//                || (openDenoiseManager!!.mRecord is MonoSystemRecord && record is MonoSystemRecord)
//            ) {return this}
//            openDenoiseManager?.release()
//        } else if (replace) {
//            return this
//        }
        openDenoiseManager = OpenDenoiseManager(context, record, true, this)
        return this
    }

    fun stopAudioInput(){
        openDenoiseManager?.stopAudioInput()
    }

    fun startAudioInput(){
        openDenoiseManager?.startAudioInput()
    }

    fun startPhoneMode(){
        openDenoiseManager?.startPhoneMode()
    }

    fun exitPhoneMode(){
        openDenoiseManager?.exitPhoneMode()
    }

    fun isRecording(): Boolean? {
        return openDenoiseManager?.isRecording()
    }
}
