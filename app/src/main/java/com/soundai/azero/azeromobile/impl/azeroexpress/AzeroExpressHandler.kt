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

package com.soundai.azero.azeromobile.impl.azeroexpress

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.azero.platforms.iface.AzeroExpress
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.impl.azeroexpress.exercise.ExerciseHandler
import com.soundai.azero.azeromobile.impl.azeroexpress.navigation.NavigationHandler
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.system.LocaleModeHandle
import com.soundai.azero.azeromobile.ui.activity.guide.GuidePageActivity
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity
import com.soundai.azero.azeromobile.ui.widget.ASRDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * 对一些自定义的Directive接收和处理，可用于开发自定义技能
 *
 *
 * 1.AndLink
 * 2.ASR 结果
 * 3.ReportLog Andlink上报日志
 * 4.Navigation 页面跳转
 */
class AzeroExpressHandler(private val mContext: Context) :
    AzeroExpress() {
    var navigationHandler: NavigationHandler? = null
    var exerciseHandler: ExerciseHandler? = null
    var handler:Handler = Handler(Looper.getMainLooper())

    override fun handleExpressDirective(name: String, payload: String) {
        log.e("name:" + name + "payload:" + payload)
        try {
            val expressDirective = JSONObject(payload)
            when (name) {
                "ASRText" -> if (expressDirective.has("text")) {
                    val text = expressDirective.getString("text")
                    CoroutineScope(Dispatchers.Main).launch {
                        val gone = expressDirective.getBoolean("finished")
                        if(ActivityLifecycleManager.getInstance().topActivity is LauncherActivity){
                            (ActivityLifecycleManager.getInstance().topActivity as LauncherActivity).showAsrText(text,gone)
                        }else if(ActivityLifecycleManager.getInstance().topActivity is GuidePageActivity){
                            (ActivityLifecycleManager.getInstance().topActivity as GuidePageActivity).showAsrText(text,gone)
                        }
                        if(gone){
                            ASRDialog.clearText()
                            delay(ASRDialog.ASR_TEXT_DISMISS_DELAY)
                        }else{
                            ASRDialog.setAsrText(text)
                        }
                    }
                }
                "TTSText" -> if (expressDirective.has("text")) {
                    val text = expressDirective.getString("text")
//                    if ("耳机模式已打开".equals(text)) {
//                        log.e("=====EarMode 打开耳机模式")
//                        MyApplication.isEarphoneMode =  true;
//                        if (MyApplication.isEarphoneMode) {
//                            (AzeroManager.getInstance().getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER) as SpeechRecognizerHandler).startPhoneMode()
//                            handler.post{Toast.makeText(mContext,"打开耳机模式",Toast.LENGTH_LONG).show()}
//                            handler.post{WakeupButton.getInstance(mContext).showHeadsetMode()}
//                            AzeroManager.getInstance().setInteractMode(1)
//                        }
//                    }else if("耳机模式已退出".equals(text)) {
//                        log.e("=====EarMode 退出耳机模式")
//                        MyApplication.isEarphoneMode =  false;
//                        AzeroManager.getInstance().setInteractMode(0)
//                        (AzeroManager.getInstance().getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER) as SpeechRecognizerHandler).exitPhoneMode()
//                        handler.post{Toast.makeText(mContext,"退出耳机模式",Toast.LENGTH_LONG).show()}
//                        handler.post{WakeupButton.getInstance(mContext).exitShowHeadsetMode()}
//                    }
                }
                "Navigation" -> navigationHandler?.handleDirective(expressDirective)
                "Exercise" -> exerciseHandler?.handleDirective(expressDirective)
                "LocaleMode" -> {
                    LocaleModeHandle.handleLocaleMode(expressDirective)
                }
                else -> {
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}

