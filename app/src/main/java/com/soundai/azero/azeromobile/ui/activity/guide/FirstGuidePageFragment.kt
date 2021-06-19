package com.soundai.azero.azeromobile.ui.activity.guide

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.paolorotolo.appintro.ISlidePolicy
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.widget.ASRDialog
import com.soundai.azero.azeromobile.ui.widget.AudioWaveView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FirstGuidePageFragment : BaseFragment(), ISlidePolicy {
    private lateinit var title: TextView
    private var waveView: AudioWaveView? = null
    private var asrTextView: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_guide_first, container, false)
        title = root.findViewById(R.id.title_textView)
        waveView = root.findViewById(R.id.skill_show_wave_view_1)
        asrTextView = root.findViewById(R.id.tv_asr_1)
        initLinearGradient()
        return root
    }

    private fun initLinearGradient() {
        val linearGradient = LinearGradient(
            0.toFloat(),
            0.toFloat(),
            (title.paint.textSize * title.text.length),
            0.toFloat(),
            Color.parseColor("#2BE1DF"),
            Color.parseColor("#0EAD6E"),
            Shader.TileMode.CLAMP
        )
        title.paint.shader = linearGradient
        title.invalidate()

    }

    override fun isPolicyRespected(): Boolean {
        return false
    }

    override fun onUserIllegallyRequestedNextPage() {
        // do nothing
    }

    fun getRecordData(byteArray: ByteArray, size: Int) {
        waveView?.post {
            waveView?.setWaveData(byteArray, size)
        }
    }

    fun setAsrText(asrText: String, gone: Boolean) {
        if (gone) {
            launch {
                delay(ASRDialog.ASR_TEXT_DISMISS_DELAY)
                asrTextView?.text = ""
            }
        } else {
            asrTextView?.text = asrText
        }
    }
}