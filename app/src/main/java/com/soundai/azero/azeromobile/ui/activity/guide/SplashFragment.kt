package com.soundai.azero.azeromobile.ui.activity.guide

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.ISlidePolicy
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.widget.NiceImageView

class SplashFragment:Fragment() , ISlidePolicy {
    private lateinit var title:TextView
    private var headset_imagev:NiceImageView? = null
    private var headset_state:TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_splash, container, false)
        title = root.findViewById(R.id.splash_title_tv)
        headset_imagev = root.findViewById(R.id.ear_state_im)
        headset_state = root.findViewById(R.id.tv_headset_state)
        initLinearGradient()
        return root
    }

    private fun initLinearGradient() {

        var mLinearGradient: LinearGradient = LinearGradient(
            0.toFloat(),
            0.toFloat(),
            (title.paint.textSize * title.text.length).toFloat(),
            0.toFloat(),
            Color.parseColor("#2BE1DF"),
            Color.parseColor("#0EAD6E"),
            Shader.TileMode.CLAMP
        )
        title.paint.setShader(mLinearGradient)
        title.invalidate()

    }

    fun setHeadsetState(enable:Boolean){
        if (!enable) {
            headset_imagev?.setImageResource(R.drawable.launcher_icon_headset_no_1)
            headset_state?.setText("请先连接上耳机\n 连接成功后可通过语音和我交流")
        }else{
            headset_imagev?.setImageResource(R.drawable.launcher_icon_headset)
            headset_state?.setText("已连接耳机，请等待加载")
        }
    }

    override fun isPolicyRespected(): Boolean {
        return false
    }

    override fun onUserIllegallyRequestedNextPage() {
    }
}