package com.soundai.azero.azeromobile.ui.widget.guide

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.R
import org.json.JSONArray
import org.json.JSONObject

class GuideSphereItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val textView by lazy { this.findViewById<TextView>(R.id.tv_launcher_item) }
    private val dot by lazy { this.findViewById<GuideSphereDotView>(R.id.v_dot) }
    private val alphaAnimation by lazy {
        ObjectAnimator.ofFloat(dot, "alpha", 1.0f, (0..2).random() * .1f).apply {
            duration = (2000..3500).random().toLong()
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
            startDelay = (500..3500).random().toLong()
        }
    }
    var payload: JSONObject? = null
        set(value) {
            field = value
            parseData(value)
        }

    init {
        initView()
    }

    private fun parseData(payload: JSONObject?) {
        if (payload == null || payload.has("null")) {
            return
        }
        textView.text = payload.getString("intent")
    }

    fun getText(): String {
        return textView.text.toString()
    }

    fun setDotColor(color: Int) {
        dot.color = color
    }

    fun startAlphaAnim() {
        dot.post {
            alphaAnimation.start()
        }
    }

    fun resumeAlphaAnim() {
        dot.post {
            alphaAnimation.resume()
        }
    }

    fun pauseAlphaAnim() {
        alphaAnimation.pause()
    }

    private fun initView() {
        View.inflate(context, R.layout.item_guide_sphere, this)
    }
}