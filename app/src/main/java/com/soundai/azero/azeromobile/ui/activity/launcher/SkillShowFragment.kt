package com.soundai.azero.azeromobile.ui.activity.launcher

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.widget.ASRDialog
import com.soundai.azero.azeromobile.ui.widget.AudioWaveView
import com.soundai.azero.azeromobile.ui.widget.AutoHideBottomUIDialog
import com.soundai.azero.azeromobile.ui.widget.SphereView
import com.soundai.azero.azeromobile.ui.widget.guide.GuideSphereItemView
import com.soundai.azero.azeromobile.utils.SPUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-03-13.
 */
class SkillShowFragment : BaseFragment() {
    private val iCCardDialog by lazy { AutoHideBottomUIDialog(this.requireContext()) }
    private var waveView: AudioWaveView? = null
    private var asrTextView: TextView? = null
    private var sphereView: SphereView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.activity_skill_show, container, false)
        waveView = root.findViewById(R.id.skill_show_wave_view)
        asrTextView = root.findViewById(R.id.tv_asr)
        sphereView = root.findViewById(R.id.sphere_view)
        return root
    }

    fun initData(template: String?, context: Context) {
        val sphereViewTemplate = if (template.isNullOrBlank()) {
            SPUtils.fetchSphereViewModel(context)
        } else {
            template
        }
        log.e("SkillShowFragment child count = ${sphereView?.childCount}, template = ${template}, sptemplate = ${sphereViewTemplate}")
        if (!template.isNullOrEmpty()) SPUtils.storeSphereViewModel(context, template)
        if (sphereView?.childCount ?: 3 > 2 || sphereViewTemplate.isBlank()) {
            return
        }
        sphereView?.run {
            val json = JSONObject(sphereViewTemplate)
            val jsonArray: JSONArray = json.getJSONArray("items")
            var count = 0
            while (count < 2) {
                for (i in 0 until jsonArray.length()) {
                    val l = GuideSphereItemView(activity!!)
                    l.payload = jsonArray.get(i) as JSONObject
                    l.setDotColor(
                        when ((0..4).random()) {
                            0 -> Color.parseColor("#FB8E2D")
                            1 -> Color.parseColor("#B770FE")
                            2 -> Color.parseColor("#FF6150")
                            3 -> Color.parseColor("#4780F1")
                            4 -> Color.parseColor("#1EBE8A")
                            else -> Color.WHITE
                        }
                    )
                    l.setOnClickListener { v ->
                        Log.i(
                            "Azero.SDK",
                            "GuideSphereItemView click ${(v as GuideSphereItemView).getText()}"
                        )
                        v.post {
                            v.payload?.let { showSkillInfo(it) }
                        }
                    }
                    addView(l)
                }
                count++
            }
            post {
                startLoop()
                children.forEach {
                    if (it is GuideSphereItemView) {
                        it.startAlphaAnim()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLoop()
    }

    fun stopLoop() {
        sphereView?.run {
            stopLoop()
            children.forEach {
                if (it is GuideSphereItemView) {
                    it.pauseAlphaAnim()
                }
            }
        }
    }

    fun startLoop(delay: Long = 100) {
        launch(coroutineExceptionHandler) {
            delay(delay)
            sphereView?.run {
                startLoop()
                children.forEach {
                    if (it is GuideSphereItemView) {
                        it.resumeAlphaAnim()
                    }
                }
            }
        }
    }

    fun getRecordData(byteArray: ByteArray, size: Int) {
        waveView?.post {
            waveView?.setWaveData(byteArray, size)
        }
    }

    fun setAsrText(asrText: String, gone: Boolean) {
        if (gone) {
            launch {
                asrTextView?.text = asrText
                delay(ASRDialog.ASR_TEXT_DISMISS_DELAY)
                asrTextView?.text = ""
            }
        } else {
            asrTextView?.text = asrText
        }
    }

    private fun showSkillInfo(payload: JSONObject) {
        Log.e("Azero.SDK", "showSkillInfo() ${payload.getString("skill")}")
        iCCardDialog.show()
        iCCardDialog.setTitle(payload.getString("skill"))
        iCCardDialog.setInfo(payload.get("query") as JSONArray)
        iCCardDialog.setLogo(payload.getString("ic_url"))
    }

    fun hideSkillInfo() {
        iCCardDialog.dismiss()
    }
}