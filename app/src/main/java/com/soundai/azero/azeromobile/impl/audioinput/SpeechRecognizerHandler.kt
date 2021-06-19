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
import android.os.Looper

import com.azero.sdk.impl.Common.InputManager
import com.azero.sdk.impl.SpeechRecognizer.AbsSpeechRecognizer
import com.azero.sdk.util.executors.AppExecutors
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.ui.activity.guide.GuidePageActivity
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity
import com.soundai.azero.azeromobile.ui.widget.ASRDialog
import com.soundai.azero.azeromobile.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 语音识别数据管理模块
 * 唤醒时 调用[.onTapToTalk] 或 [.onHoldToTalk] 请求唤醒
 * 成功后会回调[.startAudioInput]
 * 然后通过[.write]接口灌入数据
 *
 *
 * [.onTapToTalk] 唤醒后由云端下发识别结束事件，被动停止识别。
 * [.onHoldToTalk] 按下按钮识别内容，松开后停止识别。由本地控制识别内容长度，主动停止识别。
 *
 *
 * 识别停止后回调[.stopAudioInput] 停止灌入数据
 */
class SpeechRecognizerHandler(
    private val appExecutors: AppExecutors,
    private val mContext: Context,
    private val mAudioInputManager: InputManager,
    wakeWordSupported: Boolean,
    wakeWordEnabled: Boolean
) : AbsSpeechRecognizer(wakeWordSupported && wakeWordEnabled), InputManager.AudioInputConsumer,
    AudioInputManager.LocalVadListener {
    private var mAllowStopCapture = false // Only true if holdToTalk() returned true
    private var beginTime: Long = 0

    init {
        (mAudioInputManager as AudioInputManager).setLocalVadListener(this)
    }

    fun startPhoneMode(){
        (mAudioInputManager as AudioInputManager).startPhoneMode()
    }

    fun exitPhoneMode(){
        (mAudioInputManager as AudioInputManager).exitPhoneMode()
    }

    /**
     * SDK回调，要求开始灌入数据
     *
     * @return 是否可以灌入数据
     */
    override fun startAudioInput(): Boolean {
        log.d("startAudioInput")
        beginTime = System.currentTimeMillis()
        return mAudioInputManager.startAudioInput(this)
    }

    /**
     * SDK回调，要求停止灌入数据
     *
     * @return 停止灌入是否成功
     */
    override fun stopAudioInput(): Boolean {
        log.d("stopAudioInput")
//        hideWakeupDialog()
        return mAudioInputManager.stopAudioInput(this)
    }

    /**
     * 唤醒成功
     *
     * @param wakeWord 唤醒词
     */
    override fun wakewordDetected(wakeWord: String?): Boolean {
        log.e("wakewordDetected")
        if (!TaApp.isEarphoneMode) {
            mAudioCueObservable.playAudioCue(AbsSpeechRecognizer.AudioCueState.START_VOICE)
        }
        return true
    }

    /**
     * 唤醒过程结束
     */
    override fun endOfSpeechDetected() {
        log.e("endOfSpeechDetected")
        if (!TaApp.isEarphoneMode) {
            mAudioCueObservable.playAudioCue(AbsSpeechRecognizer.AudioCueState.END)
        }
    }

    /**
     * TapToTalk唤醒模式，唤醒后由云端下发识别结束事件，被动停止识别。
     */
    override fun onTapToTalk() {
        log.e("onTapToTalk")
        if (tapToTalk()) {
            if (!TaApp.isEarphoneMode) {
                mAudioCueObservable.playAudioCue(AbsSpeechRecognizer.AudioCueState.START_TOUCH)
            }
        }
    }

    /**
     * HoldToTalk唤醒模式，按下按钮识别内容，松开后停止识别。由本地控制识别内容长度，主动停止识别。
     */
    override fun onHoldToTalk() {
        log.e("onHoldToTalk")
        if (holdToTalk()) {
            mAllowStopCapture = true
            if (!TaApp.isEarphoneMode) {
                mAudioCueObservable.playAudioCue(AbsSpeechRecognizer.AudioCueState.START_TOUCH)
            }
        }
    }

    /**
     * 与[.onHoldToTalk]配套，用于主动结束识别。
     */
    override fun onReleaseHoldToTalk() {
        log.e("onReleaseHoldToTalk")
        if (mAllowStopCapture) {
            if (stopCapture()) {
                mAllowStopCapture = false
            }
        }
    }

    /**
     * SDK回调，获取模块名称
     *
     * @return 模块名称
     */
    override fun getAudioInputConsumerName(): String {
        return "SpeechRecognizer"
    }

    /**
     * 回调音频数据
     *
     * @param buffer 数据buffer
     * @param size   数据长度
     * @name AudioInputConsumer Functions
     */
    override fun onAudioInputAvailable(buffer: ByteArray, size: Int) {
        write(buffer, size.toLong()) // Write audio samples to engine
        if (TaApp.isConfigAllowToSavePcm) {
            FileUtils.writeFile(buffer,"/sdcard/asr.pcm",true);
        }
        if (!ActivityLifecycleManager.getInstance().isAppForeground) {
            return
        }
        ASRDialog.getRecordData(buffer, size)
        if(ActivityLifecycleManager.getInstance().topActivity is LauncherActivity){
            (ActivityLifecycleManager.getInstance().topActivity as LauncherActivity).getRecordData(buffer, size)
        }else if(ActivityLifecycleManager.getInstance().topActivity is GuidePageActivity){
            (ActivityLifecycleManager.getInstance().topActivity as GuidePageActivity).getRecordData(buffer, size)
        }
    }

    /**
     * 弹出唤醒提示框
     */
    private fun showWakeupDialog() {
        CoroutineScope(Dispatchers.Main).launch(coroutineExceptionHandler) {
            if(ActivityLifecycleManager.getInstance().topActivity is LauncherActivity){
                (ActivityLifecycleManager.getInstance().topActivity as LauncherActivity).showAsrText("",false)
            }else if(!(ActivityLifecycleManager.getInstance().topActivity is GuidePageActivity) ){
                ASRDialog.show()

            }
        }
    }

    /**
     * 收回唤醒提示框
     */
    private fun hideWakeupDialog() {
        CoroutineScope(Dispatchers.Main).launch(coroutineExceptionHandler) {
            if(ActivityLifecycleManager.getInstance().topActivity is LauncherActivity){
                (ActivityLifecycleManager.getInstance().topActivity as LauncherActivity).showAsrText("vad",true)
            }
            else if(!(ActivityLifecycleManager.getInstance().topActivity is GuidePageActivity) ){
                ASRDialog.hide(2000L)

            }
        }
    }

    private fun clearAsrText(){
        android.os.Handler(Looper.getMainLooper()).post {
            ASRDialog.clearText()
        }
    }

    /**
     * 本地VAD事件，收到后主动停止识别
     */
    override fun onLocalVadEnd() {
        val diffTime = System.currentTimeMillis() - beginTime
        //VAD截断时间过短，忽略此次事件
        if (TaApp.denoise_tye == 0 && diffTime < 500) {
            log.d("diffTime < 1000 :$diffTime")
        } else {
            log.e("onLocalVadEnd")
            onReleaseHoldToTalk()
        }
    }
}