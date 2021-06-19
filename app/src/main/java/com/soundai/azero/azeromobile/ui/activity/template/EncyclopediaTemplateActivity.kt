package com.soundai.azero.azeromobile.ui.activity.template

import android.content.Intent
import android.text.method.ScrollingMovementMethod
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.azero.platforms.iface.MediaPlayer
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.MediaPlayer.RawSpeakAudioMediaPlayerHandler
import com.azero.sdk.util.Constant
import com.azero.sdk.util.log
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.soundai.azero.azeromobile.GlideApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.widget.HighlightTextView
import org.json.JSONException
import org.json.JSONObject

class EncyclopediaTemplateActivity : BaseDisplayCardActivity(),
    MediaPlayer.OnMediaStateChangeListener {
    override val layoutResId: Int
        get() = R.layout.activity_encyclopedia_template

    private val mTitle: TextView by lazy { findViewById<TextView>(R.id.encyclopedia_main_title) }

    private val mScrollView: ScrollView by lazy { findViewById<ScrollView>(R.id.scrollView) }
    private val mTextField: HighlightTextView by lazy { findViewById<HighlightTextView>(R.id.tv_details) }
    private val mImageView: ImageView by lazy { findViewById<ImageView>(R.id.encyclopedia_image) }
    override fun initView() {
    }

    override fun initData(intent: Intent) {
        super.initData(intent)
        try {
            val template = JSONObject(intent.getStringExtra(Constant.EXTRA_TEMPLATE))
            configureBodyTemplate1(template)
        } catch (e: JSONException) {
            log.e(e.message)
            finish()
        } catch (e: Exception) {
        }

        val speaker =
            AzeroManager.getInstance().getHandler(AzeroManager.SPEAKER_HANDLER) as RawSpeakAudioMediaPlayerHandler
        speaker.addOnMediaStateChangeListener(this)
        mTextField.setOnHighlightChangeListener { view, line, offset ->
            if (offset > mScrollView.height / 2 - 80) {
                if (mScrollView.scrollY != view.height) {
                    mScrollView.smoothScrollTo(0, offset - (mScrollView.height / 2 - 80))
                }
            }
        }
    }

    private fun configureBodyTemplate1(
        template: JSONObject
    ) {
        try {
            if (template.has("extField")) {
                val extFieldObject = template.getJSONObject("extField")
                val asrText = extFieldObject.getString("ASRText")
                mTitle.text = asrText
            }

            if (template.has("textField")) {
                val textField = template.getString("textField")
                mTextField.text = textField
                mTextField.post { mTextField.startAnimation() }
                mTextField.movementMethod = ScrollingMovementMethod.getInstance()
            }

            if (template.has("backgroundImage")) {
                val backgroundImage = template.getJSONObject("backgroundImage")
                if (backgroundImage.has("sources")) {
                    val sources = backgroundImage.getJSONArray("sources")
                    val source = sources.get(0) as JSONObject
                    if (source.has("url")) {
                        val url = source.getString("url")
                        GlideApp.with(mImageView)
                            .load(url)
                            .apply(
                                RequestOptions().centerCrop().transform(
                                    RoundedCorners(
                                        resources.getDimensionPixelOffset(
                                            R.dimen.encyclopedia_image_radius
                                        )
                                    )
                                )
                            )
                            .into(mImageView)
                    }
                }
            }
        } catch (e: JSONException) {
            log.e(e.message)
        }
    }

    override fun onMediaError(name: String, s1: String, mediaError: MediaPlayer.MediaError) {
    }

    override fun onMediaStateChange(name: String, mediaState: MediaPlayer.MediaState) {
        when (mediaState) {
            MediaPlayer.MediaState.STOPPED -> mTextField.stopAnimation()
            MediaPlayer.MediaState.PLAYING -> {
            }
            MediaPlayer.MediaState.BUFFERING -> {
            }
        }
    }

    override fun onPositionChange(name: String, position: Long, duration: Long) {
        mTextField.updatePosition(position)
    }

}
