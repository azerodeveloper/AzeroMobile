package com.soundai.azero.azeromobile.ui.widget

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-03-14.
 */


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.GlideApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray


class AutoHideBottomUIDialog : Dialog {

    var mContext: Context
    private lateinit var skillTitle: TextView
    private lateinit var skillInfo: TextView
    private lateinit var sayHello: TextView
    private lateinit var skillLogo: CircleImageView

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_entry_ic_card)
        skillTitle = findViewById(R.id.tv_skill_name)
        skillInfo = findViewById(R.id.tv_skill_query)
        sayHello = findViewById(R.id.tv_say_hello)
        skillLogo = findViewById(R.id.niv_skill_logo)
        initLinearGradient()
    }

    override fun onStart() {
        super.onStart()
        Log.e("Azero.SDK", "AutoHideBottomUIDialog onStart()")
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun show() {
//        this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
//        this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
//        var window = this.window
//        window.attributes.height = DisplayUtil.dip2px(mContext, 800f)
////
//        window.attributes.width = DisplayUtil.dip2px(mContext, 480f)
        window.setBackgroundDrawable(mContext.resources.getDrawable(R.color.color_transparent))
//        window.setGravity(Gravity.CENTER)
        super.show()
    }


    fun setLogo(imgUrl: String) {
        CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
            log.e("setLogo = $imgUrl")
            Handler(Looper.getMainLooper()).post {
                GlideApp.with(context)
                    .load(imgUrl)
                    .placeholder(R.drawable.launcher_icon_xiaopin)
                    .error(R.drawable.launcher_icon_xiaopin)
                    .into(skillLogo)
            }
        }
    }

    fun setTitle(title: String) {
        skillTitle.setText(title)
    }

    private fun initLinearGradient() {
        val mLinearGradient = LinearGradient(
            0.toFloat(),
            0.toFloat(),
            (skillTitle.paint.textSize * skillTitle.text.length),
            0.toFloat(),
            Color.parseColor("#2BE1DF"),
            Color.parseColor("#0EAD6E"),
            Shader.TileMode.CLAMP
        )
        sayHello.paint.shader = mLinearGradient
        sayHello.invalidate()

    }

    fun setInfo(info: JSONArray) {
        val infoStr: StringBuilder = StringBuilder("")
        for (i in 0 until info.length()) {
            if (i == info.length() - 1) {
                infoStr.append(info.get(i).toString())
            } else {
                infoStr.append(info.get(i).toString() + "\n")
            }
        }
        skillInfo.text = infoStr
    }

}