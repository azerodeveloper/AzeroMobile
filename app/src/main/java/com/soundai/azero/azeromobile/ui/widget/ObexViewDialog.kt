package com.soundai.azero.azeromobile.ui.widget

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.utils.Utils

class ObexViewDialog(context: Context) : LinearLayout(context) {
    companion object {
        private var instance: ObexViewDialog? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: ObexViewDialog(context).also {
                    instance = it
                }
            }
    }

    private var mView: LinearLayout =
        LayoutInflater.from(context).inflate(R.layout.view_obex_dialog, this) as LinearLayout
    private var windowManager: WindowManager =
        getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var isShowOnWindow = false
    private var mLayoutParams: WindowManager.LayoutParams
    // private val mUnityPlayer: UnityPlayer = UnityPlayer(context)

    init {
        mLayoutParams = WindowManager.LayoutParams()
        mLayoutParams.type = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 -> {
                WindowManager.LayoutParams.TYPE_PHONE
            } else -> {
                WindowManager.LayoutParams.TYPE_TOAST
            }
        }

        mLayoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        // 设置图片格式，效果为背景透明
        mLayoutParams.format = PixelFormat.RGBA_8888
        mLayoutParams.width = Utils.dp2px(200f).toInt()
        mLayoutParams.height = Utils.dp2px(300f).toInt()

        mLayoutParams.gravity = Gravity.BOTTOM or Gravity.END
        mLayoutParams.verticalMargin = 0.1f
    }

    fun show() {
        if (checkOverlayPermission() && !isShowOnWindow) {
            isShowOnWindow = !isShowOnWindow
            windowManager.addView(mView, mLayoutParams)
        }
    }

    fun hide() {
        if (isShowOnWindow) {
            isShowOnWindow = !isShowOnWindow
            windowManager.removeView(mView)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // mUnityPlayer.destroy()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(context)
        else
            true
    }
}