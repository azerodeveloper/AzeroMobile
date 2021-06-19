package com.soundai.azero.azeromobile.system

import com.azero.sdk.AzeroManager
import com.azero.sdk.util.log
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * 模式切换，除normal外，需要将当前模式关闭再打开其他模式
 */
object LocaleModeHandle {
    private var currentMode: LocaleMode = LocaleMode.NORMAL
    private var exceptMode: LocaleMode = LocaleMode.NORMAL
    private var switchModeJob: Job? = null

    enum class LocaleMode(val type: String) {
        NORMAL("normal"),
        HEADSET("headset"),
        GUIDE("guide"),
        ANSWER("answer")
    }

    fun handleLocaleMode(directive: JSONObject) {
        if (directive.has("mode") && directive.has("value")) {
            val mode = directive.getString("mode")
            val switch = directive.getString("value")
            log.d("handleLocaleMode mode= $mode, switch= $switch")
            if (switch == "ON") {
                currentMode = when (mode) {
                    LocaleMode.HEADSET.type -> LocaleMode.HEADSET
                    LocaleMode.ANSWER.type -> LocaleMode.ANSWER
                    LocaleMode.GUIDE.type -> LocaleMode.GUIDE
                    else -> LocaleMode.NORMAL
                }
            } else if (switch == "OFF") {
                currentMode = LocaleMode.NORMAL
            }
            tryToSwitchMode()
        }
    }

    fun switchLocaleMode(mode: LocaleMode) {
        log.e("switchLocaleMode mode= ${mode.type}")
        switchModeJob?.cancel()
        exceptMode = mode
        switchModeJob = CoroutineScope(Dispatchers.IO).launch {
            while (currentMode != exceptMode) {
                tryToSwitchMode()
                delay(2000)
            }
        }
    }

    fun checkMode(mode: LocaleMode) = mode == currentMode

    private fun tryToSwitchMode() {
        log.e("tryToSwitchMode currentMode= ${currentMode.type}, exceptMode= ${exceptMode.type}")
        if (currentMode == exceptMode) return
        if (currentMode != LocaleMode.NORMAL) {
            AzeroManager.getInstance().changeLocaleMode(currentMode.type, "OFF")
        } else {
            AzeroManager.getInstance().changeLocaleMode(exceptMode.type, "ON")
        }
    }
}