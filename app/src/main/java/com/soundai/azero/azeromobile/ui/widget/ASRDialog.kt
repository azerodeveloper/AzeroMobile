package com.soundai.azero.azeromobile.ui.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.widget.FrameLayout
import android.widget.TextView
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity
import com.soundai.azero.azeromobile.ui.activity.launcher.SkillShowFragment
import com.soundai.azero.azeromobile.utils.dp
import kotlinx.coroutines.*

object ASRDialog {
    const val ASR_TEXT_DISMISS_DELAY = 200L

    private lateinit var displayView: ViewGroup
    private lateinit var asrTextView: TextView
    private lateinit var audioWaveView: AudioWaveView
    private lateinit var windowManager: WindowManager
    private lateinit var windowLayParams: ViewGroup.LayoutParams
    private var activity: Activity? = null
    private var isShowOnWindow = false
    private var isAttachToWindow = false

    /**
     * 目前在LauncherActivity中attach
     */
    fun attachToWindow(activity: Activity) {
        if (isAttachToWindow || !checkOverlayPermission()) return
        this.activity = activity
        initView(activity)
        windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowLayParams = WindowManager.LayoutParams().apply {
            type = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1
                }
                Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 -> {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                else -> {
                    WindowManager.LayoutParams.TYPE_TOAST
                }
            }
            // 设置图片格式，效果为背景透明
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = 60.dp.toInt()
            format = PixelFormat.RGBA_8888
            flags = FLAG_NOT_TOUCHABLE or FLAG_NOT_FOCUSABLE
            gravity = Gravity.BOTTOM
        }
//        windowManager.addView(displayView, windowLayParams)
        isShowOnWindow = true
        isAttachToWindow = true
    }

    fun detachToWindow() {
        hide()
        activity = null
        isAttachToWindow = false
    }

    private fun initView(context: Context) {
        displayView = FrameLayout(context)
        audioWaveView = AudioWaveView(context).apply {
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                30.dp.toInt()
            )
            lp.marginStart = 30.dp.toInt()
            lp.marginEnd = 30.dp.toInt()
            lp.bottomMargin = 15.dp.toInt()
            lp.topMargin = 15.dp.toInt()
            layoutParams = lp
            displayView.addView(this)
        }
        asrTextView = TextView(context).apply {
            setTextColor(Color.parseColor("#4985FF"))
            textSize = 20f
            gravity = Gravity.CENTER
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.CENTER
            layoutParams = lp
            displayView.addView(this)
        }
    }

    fun show() {
        CoroutineScope(Dispatchers.Main).launch {
            val topActivity: Activity = ActivityLifecycleManager.getInstance().topActivity
            if (!isShowOnWindow && ActivityLifecycleManager.getInstance().isAppForeground && isAttachToWindow
                && activity?.isFinishing == false
            ) {
                if (topActivity is LauncherActivity && topActivity.getCurrentFragment() is SkillShowFragment) {
                    return@launch
                }
                asrTextView.text = ""
                isShowOnWindow = true
//                windowManager.addView(displayView, windowLayParams)
            }
        }
    }

    fun hide() {
        CoroutineScope(Dispatchers.Main).launch {
            if (isShowOnWindow && isAttachToWindow) {
                isShowOnWindow = false
//                windowManager.removeView(displayView)
            }
        }
    }

    fun hide(delay: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delay)
            hide()
        }
    }

    fun getRecordData(byteArray: ByteArray, size: Int) {
        if (!isAttachToWindow) return
        audioWaveView.post {
            audioWaveView.setWaveData(byteArray, size)
        }
    }

    fun setAsrText(content: String) {
        if (isAttachToWindow) {
            asrTextView.text = content
        }
    }

    fun clearText() {
        if (isAttachToWindow) {
            asrTextView.text = ""
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(TaApp.application)
        else
            true
    }
}