package com.soundai.azero.azeromobile.ui.activity.question

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.MediaPlayer.RawSpeakAudioMediaPlayerHandler
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.system.LocaleModeHandle
import com.soundai.azero.azeromobile.ui.activity.base.activity.BaseSwipeActivity
import com.soundai.azero.azeromobile.utils.AzeroHelper


class QuestionActivity : BaseSwipeActivity() {
    companion object {
        fun start(context: Context, payload: String) {
            context.startActivity(Intent(context, QuestionActivity::class.java).also {
                it.putExtra(Constant.EXTRA_TEMPLATE, payload)
            })
        }
    }

    private val questionViewModel by lazy {
        ViewModelProviders.of(this).get(QuestionViewModel::class.java)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_question)
        initData(intent)
        LocaleModeHandle.switchLocaleMode(LocaleModeHandle.LocaleMode.ANSWER)
        AzeroHelper.setAudioMute(true)
    }

    override fun onStop() {
        super.onStop()
        (AzeroManager.getInstance().getHandler(AzeroManager.SPEAKER_HANDLER)
                as RawSpeakAudioMediaPlayerHandler).stop()
        questionViewModel.exitGame()
        AzeroHelper.setAudioMute(false)
        this.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocaleModeHandle.switchLocaleMode(LocaleModeHandle.LocaleMode.HEADSET)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initData(intent)
    }

    private fun initData(intent: Intent) {
        val template = intent.getStringExtra(Constant.EXTRA_TEMPLATE)
        questionViewModel.update(template)
    }
}
