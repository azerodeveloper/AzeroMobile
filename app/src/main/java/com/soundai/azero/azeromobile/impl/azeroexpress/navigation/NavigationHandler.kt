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

package com.soundai.azero.azeromobile.impl.azeroexpress.navigation

import android.content.Context
import android.content.Intent
import com.azero.sdk.AzeroManager
import com.azero.sdk.event.Command
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.impl.azeroexpress.AzeroExpressInterface
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity
import com.soundai.azero.azeromobile.ui.activity.playerinfo.BasePlayerInfoActivity
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity
import com.soundai.azero.azeromobile.utils.Utils
import org.json.JSONException
import org.json.JSONObject

/**
 * 导航模块
 * 执行语音指令"退出"、"返回"、"返回首页"的动作
 */
class NavigationHandler(context: Context) : AzeroExpressInterface {

    private val applicationContext: Context

    init {
        applicationContext = context.applicationContext
    }

    override fun handleDirective(expressDirective: JSONObject) {
        try {
            val action = expressDirective.getString("action")
            when (action) {
                //退出
                "Exit" -> handleExit()
                //返回首页
                "goHome" -> handleGoHome()
                //返回
                "goBack" -> handleGoBack()
                else -> {
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleGoBack() {
        log.d("NavigationHandler handleGoBack")
        if (Utils.isApplicationForeground()) {
            ActivityLifecycleManager.getInstance().finishAllActivityExceptLauncher(-1)
        }
    }

    private fun handleExit() {
        log.d("NavigationHandler handleExit")
        //执行退出命令
        if (Utils.isApplicationForeground()) {
            if (ActivityLifecycleManager.getInstance().topActivity !is BaseDisplayCardActivity) {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)
//                AzeroManager.getInstance().stopAllPlayers()
            }
            ActivityLifecycleManager.getInstance().finishAllActivityExceptLauncher(1)
        }
    }

    private fun handleGoHome() {
        log.d("NavigationHandler handleGoHome")
        if (Utils.isApplicationForeground()) {
            ActivityLifecycleManager.getInstance().finishAllActivityExceptLauncher(1)
        }
    }
}
