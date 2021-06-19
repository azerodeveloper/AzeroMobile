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

package com.soundai.azero.azeromobile.impl.audioinput.record

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRouting
import android.os.Build

import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.system.TaAudioManager
import com.soundai.azero.azeromobile.utils.FileUtils.writeFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

/**
 * 默认的数据读取工具，使用AudioTrack读取单路数据，可运行在任何设备上
 * 由于获取不到回采数据，在播放音频时不易唤醒，仅供体验使用
 */
class MonoSystemRecord : Record() {
    private var mAudioInput: AudioRecord?
    private var mReaderRunnable: AudioReaderRunnable = AudioReaderRunnable()

    private val mExecutor = Executors.newFixedThreadPool(1)
    private var a :Int = 0
    private var isRecording:Boolean = false
    var routingListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        object : AudioRouting.OnRoutingChangedListener{
            override fun onRoutingChanged(p0: AudioRouting?) {
                log.e("[TaAudioManager] Audio routing changed  = ${(p0 as AudioRecord).audioSource.toString()} ,routedDevice =  ${p0?.routedDevice?.type.toString()}")
                if(p0?.routedDevice?.type == 7 || p0?.routedDevice == null){
//                    resetSco()
                }else{
                }
            }
        }
    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : AudioRecord.OnRoutingChangedListener{
                override fun onRoutingChanged(p0: AudioRecord?) {
                    log.e("[TaAudioManager] Audio routing changed  = ${(p0 as AudioRecord).audioSource.toString()} ,routedDevice =  ${p0?.routedDevice?.type.toString()}" +
                            "isSink = ${p0?.routedDevice?.isSink}, isSource = ${p0?.routedDevice?.isSource}" +
                            "id = ${p0?.routedDevice?.id}" )
                    if(p0?.routedDevice?.type == 7 || p0?.routedDevice == null){
//                        resetSco()
                    }else{
                    }
                }

            }
    }else{null}

    init {
        mAudioInput = createAudioInput()
    }

    override fun start() {
        try {
            stop()
            release()
            mAudioInput = createAudioInput()
            mAudioInput?.startRecording()
        } catch (e: IllegalStateException) {
            log.e("[TaAudioManager] AudioRecord cannot start recording. Error: " + e.message)
        }
        try {
            mExecutor.submit(mReaderRunnable) // Submit the audio reader thread
            TaAudioManager.setIsRecordFailed(false)
        } catch (e: RejectedExecutionException) {
            log.e("[TaAudioManager] Audio reader task cannot be scheduled for execution. Error: " + e.message)
        }

    }

    override fun stop() {
        if (mReaderRunnable != null) mReaderRunnable!!.cancel()
        try {
            mAudioInput!!.stop()
            release()
        } catch (e: IllegalStateException) {
            log.e("[TaAudioManager] AudioRecord cannot stop recording. Error: " + e.message)
        }

    }

    override fun isRecording(): Boolean {
        if (mReaderRunnable != null) return false
        return mReaderRunnable.isRunning
    }

    @SuppressLint("NewApi")
    override fun release() {
        mAudioInput?.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mAudioInput?.removeOnRoutingChangedListener(routingListener as AudioRouting.OnRoutingChangedListener)
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioInput?.removeOnRoutingChangedListener(routingListener as AudioRecord.OnRoutingChangedListener)
        }
    }


    @SuppressLint("NewApi")
    private fun createAudioInput(): AudioRecord? {
        var audioRecord: AudioRecord? = null
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                sSampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize =
                minBufferSize + sAudioFramesInBuffer * sSamplesToCollectInOneCycle * sBytesInEachSample
            audioRecord = AudioRecord(
                TaAudioManager.getPreferedAudioSource(), sSampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            if (audioRecord?.preferredDevice != null && audioRecord?.routedDevice != null) {
                log.e("[TaAudioManager] setPreferredDevice  preferredDevice = ${audioRecord?.preferredDevice.type}" +
                        "routed = ${audioRecord?.routedDevice.type}")
            }
            var result = audioRecord?.setPreferredDevice(TaAudioManager.getPreferedDeviceInfo())
            log.e("[TaAudioManager] setPreferredDevice result = ${result}")
//            log.e("=====EarMode setPreferredDevice result = ${result} ,routedDevice =  ${BluetoothUtil.getPreferedDeviceInfo()?.type.toString()}"+
//                    "isSink = ${BluetoothUtil.getPreferedDeviceInfo()?.isSink}, isSource = ${BluetoothUtil.getPreferedDeviceInfo()?.isSource}"+
//                    "id = ${BluetoothUtil.getPreferedDeviceInfo()?.id}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                audioRecord?.addOnRoutingChangedListener(routingListener,null)
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioRecord?.addOnRoutingChangedListener(routingListener as AudioRecord.OnRoutingChangedListener,null)
            }

        } catch (e: IllegalArgumentException) {
            log.e("[TaAudioManager] Cannot create audio input. Error: " + e.message)
        }

        return audioRecord
    }

    //
    // AudioReader class
    //
    private inner class AudioReaderRunnable : Runnable {
        var isRunning = true
        private var index :Int = 0
        private val mBuffer = ByteArray(sSamplesToCollectInOneCycle * sBytesInEachSample)
        private val mBuffer_short = ShortArray(sSamplesToCollectInOneCycle * sBytesInEachSample)
        private val mBuffer2 = ByteArray(sSamplesToCollectInOneCycle * sBytesInEachSample * 2)

        internal fun cancel() {
            isRunning = false
        }

        override fun run() {
            isRunning = true
            var size: Int

            while (isRunning) {
                if (TaApp.denoise_tye == 0) {
                    size = mAudioInput!!.read(mBuffer, 0, mBuffer.size)
                }else {
                    size = mAudioInput!!.read(mBuffer_short, 0, mBuffer_short.size)
                    if (TaApp.isConfigAllowToSavePcm) {
                        writeFile(mBuffer_short,"/sdcard/mono.pcm",true)
                    }
                }

//                log.e("=====EarMode Audiorecord size = ${size} ")
                if(size<=0){
                    index ++;
                    CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                        delay(1000)
                    }
                    if(index >=15){
                        isRunning = false
                    }
                    log.e("[TaAudioManager] Cannot record normal pcm in several times ")
                    TaAudioManager.setIsRecordFailed(true)
                    continue
                } else if (size > 0 && isRunning && listener != null) {
                    if (!TaAudioManager.isBTorWiredHeadsetConnected())  continue

                    if (TaApp.denoise_tye == 1) {
                        listener?.onData(mBuffer_short, size)
                        continue
                    }

                    // 算法库至少需要一路回采数据
                    // 为单通道数据添加一个空的回采通道
                    index = 0
                    var i = 0
                    while (i < size * 2){
                        if (i % 4 == 0) {
                            mBuffer2[i] = mBuffer[i / 2]
                            mBuffer2[i + 1] = mBuffer[i / 2 + 1]
                        }
                        i += 2
                    }

                    listener?.onData(mBuffer2, size * 2)
                }
            }
            isRunning = false
        }
    }

    companion object {
        // All audio input consumers expect PCM 16 data @ 16 Khz. We divide this consumption into 10 ms
        // chunks. It comes out at 160 samples every 10 ms to reach 16000 samples (in a second).
        private val sSamplesToCollectInOneCycle = 160
        private val sBytesInEachSample = 2 // PCM 16 = 2 bytes per sample
        private val sSampleRateInHz = 16000 //16 khz
        private val sAudioFramesInBuffer = 5 // Create large enough buffer for 5 audio frames.
    }
}
