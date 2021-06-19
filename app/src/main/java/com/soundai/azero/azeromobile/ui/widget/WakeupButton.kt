package com.soundai.azero.azeromobile.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.soundai.azero.azeromobile.R
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import com.azero.sdk.AzeroManager
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import kotlinx.coroutines.*
import java.io.File

/**
 * Create by xingw on 2019/10/30
 */
class WakeupButton(context: Context) : LinearLayout(context) {
    private var mView: View
    var floatingActionButton: FloatingActionButton
    private val windowManager: WindowManager
    private val mLayoutParams: WindowManager.LayoutParams
    var SAVE_DATA = false
    var isShowOnWindow = false


    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: WakeupButton? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: WakeupButton(context).also { instance = it }
            }
    }

    init {
        mView = LayoutInflater.from(context).inflate(R.layout.view_wakeup_button, this)
        floatingActionButton = mView.findViewById(R.id.bt_wakeup)
        val debugButton = mView.findViewById<FloatingActionButton>(R.id.bt_debug)
        debugButton.setOnClickListener {
            SAVE_DATA = !SAVE_DATA
            if (SAVE_DATA) {
                val file = File("/sdcard/stereo.pcm")
                if(file.exists()){
                    file.delete()
                }
                val file2 = File("/sdcard/mono.pcm")
                if(file2.exists()){
                    file2.delete()
                }
                debugButton.backgroundTintList = getColorStateList(R.color.colorButtonPrimary2)
            } else {
                debugButton.backgroundTintList = getColorStateList(R.color.colorButtonPrimary)
            }
        }
        debugButton.backgroundTintList = getColorStateList(R.color.colorButtonPrimary)
        debugButton.alpha = 0.7F
        floatingActionButton.backgroundTintList = getColorStateList(R.color.colorButtonPrimary)
        floatingActionButton.alpha = 0.7F
        windowManager = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
            FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
        // 设置图片格式，效果为背景透明
        mLayoutParams.format = PixelFormat.RGBA_8888
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        mLayoutParams.gravity = Gravity.BOTTOM or Gravity.END
        mLayoutParams.verticalMargin = 0.17F
        show()
    }


    fun show() {
        /*if (checkOverlayPermission() && !isShowOnWindow) {
            isShowOnWindow = !isShowOnWindow
            windowManager.addView(mView, mLayoutParams)
        }*/
    }

    fun hide() {
        /*if (isShowOnWindow) {
            isShowOnWindow = !isShowOnWindow
            windowManager.removeView(mView)
        }*/
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(context)
        else
            true
    }

    fun showHeadsetMode(){
        floatingActionButton.setImageResource(R.drawable.vector_drawable_headsetmode)
        floatingActionButton.setEnabled(false)
    }

    fun exitShowHeadsetMode(){
        floatingActionButton.setImageResource(R.drawable.vector_drawable_azero_logo)
        floatingActionButton.setEnabled(true)
    }

    fun displayErrorAnimation() {
        /*
        CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
            if (displayErrorTask.isActive) {
                displayErrorTask.cancelAndJoin()
            }
            if (displayErrorTask.isCompleted) {
                displayErrorTask = async { changeFloatingButtonBackground() }
            } else {
                displayErrorTask.start()
            }
        }*/
    }

    private var displayErrorTask =
        CoroutineScope(Dispatchers.Main).async(start = CoroutineStart.LAZY) { changeFloatingButtonBackground() }

    private suspend fun changeFloatingButtonBackground() = withContext(Dispatchers.Main) {
        /*try {
            floatingActionButton.backgroundTintList =
                resources.getColorStateList(R.color.colorButtonError, null)
            delay(3000)
            floatingActionButton.backgroundTintList =
                resources.getColorStateList(R.color.colorButtonPrimary, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }

    private fun getColorStateList(colorId: Int): ColorStateList {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColorStateList(colorId, null)
        } else {
            resources.getColorStateList(colorId)
        }
    }
}