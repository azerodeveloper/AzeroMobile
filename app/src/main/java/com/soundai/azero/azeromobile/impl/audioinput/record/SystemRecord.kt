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

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log

import com.azero.sdk.util.log

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

/**
 * 默认的数据读取工具，使用AudioTrack读取单路数据，可运行在任何设备上
 * 由于获取不到回采数据，在播放音频时不易唤醒，仅供体验使用
 */
class SystemRecord : Record() {
    private val mAudioInput: AudioRecord?
    private var mReaderRunnable: AudioReaderRunnable = AudioReaderRunnable()

    private val mExecutor = Executors.newFixedThreadPool(1)

    init {
        mAudioInput = createAudioInput()
    }

    override fun start() {
        try {
            mAudioInput!!.startRecording()
            mExecutor.submit(mReaderRunnable) // Submit the audio reader thread
        } catch (e: IllegalStateException) {
            log.e("AudioRecord cannot start recording. Error: " + e.message)
        } catch (e: RejectedExecutionException) {
            log.e("Audio reader task cannot be scheduled for execution. Error: " + e.message)
        }
    }

    override fun stop() {
        log.d("stop")
        mReaderRunnable.cancel()
        try {
            mAudioInput!!.stop()
        } catch (e: IllegalStateException) {
            log.e("AudioRecord cannot stop recording. Error: " + e.message)
        }
    }

    override fun isRecording(): Boolean {
        if (mReaderRunnable != null) return false
        return mReaderRunnable.isRunning
    }

    override fun release() {
        mAudioInput?.release()
    }

    private fun createAudioInput(): AudioRecord? {
        var audioRecord: AudioRecord? = null
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                sSampleRateInHz,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize =
                minBufferSize + sAudioFramesInBuffer * sSamplesToCollectInOneCycle * sBytesInEachSample
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sSampleRateInHz)
                    .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
                audioRecord = AudioRecord.Builder()
                    .setAudioFormat(audioFormat)
                    .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                    .setBufferSizeInBytes(bufferSize)
                    .build()
            } else {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sSampleRateInHz,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            }
        } catch (e: IllegalArgumentException) {
            log.e("Cannot create audio input. Error: " + e.message)
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
        private val mBuffer2 =
            ByteArray((sSamplesToCollectInOneCycle.toDouble() * sBytesInEachSample.toDouble() * 1.5).toInt())

        internal fun cancel() {
            isRunning = false
        }

        override fun run() {
            var size: Int
            isRunning = true
            while (isRunning) {
                size = mAudioInput!!.read(mBuffer, 0, mBuffer.size)
                if(size<=0){
                        index ++;
                        if(index >=10){
                            isRunning = false
                        }
                }
                else if (size > 0 && isRunning && listener != null) {
                    index = 0
                    // 算法库至少需要一路回采数据
                    // 为双通道数据添加一个空的回采通道
                    var i = 0
                    var j = 0
                    while (i < size) {
                        mBuffer2[j] = mBuffer[i]
                        mBuffer2[j + 1] = mBuffer[i + 1]
                        mBuffer2[j + 2] = mBuffer[i + 2]
                        mBuffer2[j + 3] = mBuffer[i + 3]
                        mBuffer2[j + 4] = 0.toByte()
                        mBuffer2[j + 5] = 0.toByte()
                        i += 4
                        j += 6
                    }
                    val data = ByteArray((size * 1.5).toInt())
                    System.arraycopy(mBuffer2, 0, data, 0, data.size)

                    listener?.onData(data, data.size)
                }
            }
            isRunning = false
        }
    }
    companion object {
        // All audio input consumers expect PCM & 16bit & 16 Khz data. We divide this consumption into 10 ms
        // chunks. If we use STEREO mic data , wen need add mimo reference channel which data is 0 . In result
        // we need give two ch mic data + one ch ref data.
        private val sSamplesToCollectInOneCycle = 160 * 2
        private val sBytesInEachSample = 2 // PCM 16 = 2 bytes per sample
        private val sSampleRateInHz = 16000 //16 khz
        private val sAudioFramesInBuffer = 5 // Create large enough buffer for 5 audio frames.
    }
}
