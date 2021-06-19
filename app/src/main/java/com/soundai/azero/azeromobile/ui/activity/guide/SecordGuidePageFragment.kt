package com.soundai.azero.azeromobile.ui.activity.guide

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

class SecordGuidePageFragment : BaseFragment(), ISlidePolicy {
    private var waveView: AudioWaveView? = null
    private var asrTextView: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_guide_second, container, false)
        waveView = root.findViewById(R.id.skill_show_wave_view_2)
        asrTextView = root.findViewById(R.id.tv_asr_2)
        return root
    }

    override fun isPolicyRespected(): Boolean {
        return false
    }

    override fun onUserIllegallyRequestedNextPage() {
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