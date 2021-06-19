package com.soundai.azero.azeromobile.ui.activity.playerinfo

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ToggleButton
import com.azero.platforms.iface.MediaPlayer
import com.azero.sdk.AzeroManager
import com.azero.sdk.event.Command
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.GlideApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.impl.SimpleMediaChangeListener
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.stringForTime
import com.soundai.azero.azeromobile.ui.activity.launcher.item.MusicGridItem
import com.soundai.azero.azeromobile.ui.widget.CircleImageView
import com.soundai.azero.azeromobile.utils.DownloadHelper
import com.soundai.azero.azeromobile.utils.Utils
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_details_playing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PlayingDetailsActivity : BasePlayerInfoActivity(), DownloadHelper.DownloadListener {
    private val title: TextView by lazy { findViewById<TextView>(R.id.tv_title) }
    private val subTitle: TextView by lazy { findViewById<TextView>(R.id.tv_sub_title) }
    private val type: ImageView by lazy { findViewById<ImageView>(R.id.playing_playlist) }
    private val art: CircleImageView by lazy { findViewById<CircleImageView>(R.id.bg_art) }
    private val mControlPlayPause: ToggleButton by lazy { findViewById<ToggleButton>(R.id.playing_play) }
    private val mControlPrev: ImageView by lazy { findViewById<ImageView>(R.id.playing_pre) }
    private val mControlNext: ImageView by lazy { findViewById<ImageView>(R.id.playing_next) }
    private val mBackAlbum: ImageView by lazy { findViewById<ImageView>(R.id.iv_albumArt) }
    private val mDurationPlayed: TextView by lazy { findViewById<TextView>(R.id.tv_music_duration_played) }
    private val mDuration: TextView by lazy { findViewById<TextView>(R.id.tv_music_duration) }
    private val progressBar: SeekBar by lazy { findViewById<SeekBar>(R.id.sb_progress) }
    private val title_tv: TextView by lazy { findViewById<TextView>(R.id.title_music_textview) }
    private val author_tv: TextView by lazy { findViewById<TextView>(R.id.author_music_textview) }

    private var mMediaPlayer: MediaPlayerHandler? = null
    private var musicGridItem: MusicGridItem? = null
    private var seeking = false

    var handler: Handler = Handler(Looper.getMainLooper())

    companion object {
        fun start(activity: Activity, musicGridItem: MusicGridItem, isEnglish: Boolean = false) {
            CoroutineScope(Dispatchers.Main).launch(coroutineExceptionHandler) {
                val intent = Intent(activity, PlayingDetailsActivity::class.java)
                intent.putExtra(Constant.KEY_MUSIC_PARCELABLE, musicGridItem)
                intent.putExtra(Constant.KEY_IS_ENGLISH_ITEM, isEnglish)
                activity.startActivity(intent)
            }
        }
    }

    override val layoutResId: Int
        get() = R.layout.activity_details_playing

    override fun initData(intent: Intent) {
        musicGridItem = intent.getParcelableExtra(Constant.KEY_MUSIC_PARCELABLE)
        val imgUrl = musicGridItem?.imgUrl
        progressBar.progress = 0
        if (imgUrl!!.isNotEmpty()) {
            if (intent.getBooleanExtra(Constant.KEY_IS_ENGLISH_ITEM, false)) {
                mBackAlbum.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.gradient_background,
                        null
                    )
                )
            } else {
                GlideApp.with(baseContext)
                    .load(imgUrl)
                    .placeholder(R.drawable.img_default)
                    .into(art)
                GlideApp.with(baseContext)
                    .load(imgUrl)
                    .placeholder(R.drawable.login_bg_night)
                    //18模糊度 4缩放倍数
                    .transform(BlurTransformation(18, 4))
                    .into(mBackAlbum)
            }
        }

        if (Utils.isLandscape(this)) {
            title.text = musicGridItem?.title
            subTitle.text = musicGridItem?.singer
        } else {
            title.text = ""
            subTitle.text = ""
        }
        lyricView.visibility = View.GONE
        headerView.visibility = View.VISIBLE
        title_tv.text = musicGridItem?.title
        author_tv.text = musicGridItem?.singer

        val lyricUrl = musicGridItem?.lyricUrl
        if (!lyricUrl.isNullOrBlank()) {
            val fileName = lyricUrl.substring(lyricUrl.lastIndexOf("/"))
            val pathName: String =
                this.getExternalFilesDir(android.os.Environment.DIRECTORY_PODCASTS)!!.path
            DownloadHelper.download(lyricUrl, pathName, fileName, this)
        }

        mMediaPlayer =
            AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER) as MediaPlayerHandler
        mMediaPlayer?.addOnMediaStateChangeListener(positionListener)

        mControlPlayPause.isChecked = mMediaPlayer!!.isPlaying

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (seeking) {
                    val duration = mMediaPlayer!!.duration
                    val position = seekBar.progress * duration / 1000L
                    mDurationPlayed.text = stringForTime(position.toInt())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seeking = false
                val duration = mMediaPlayer!!.duration
                var progress = seekBar.progress
                //直接拖到头状态会有问题
                if (seekBar.progress == 1000) {
                    progress = 999
                }
                val position = progress * duration / 1000L
                mMediaPlayer!!.position = position
            }
        })
    }

    override fun initView() {
        initActionBar()
        mControlPrev.setOnClickListener {
            AzeroManager.getInstance().executeCommand(
                Command.CMD_PLAY_PREVIOUS
            )
        }
        mControlNext.setOnClickListener {
            AzeroManager.getInstance().executeCommand(
                Command.CMD_PLAY_NEXT
            )
        }
        mControlPlayPause.setOnClickListener {
            if (mControlPlayPause.isChecked) {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY)
            } else {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)
            }
        }
    }


    fun updateIntent(musicGridItem: MusicGridItem, isEnglish: Boolean = false) {
        intent.putExtra(Constant.KEY_MUSIC_PARCELABLE, musicGridItem)
        intent.putExtra(Constant.KEY_IS_ENGLISH_ITEM, isEnglish)
        launch { initData(intent) }
    }

    private fun initActionBar() {
        val ab = supportActionBar
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeAsUpIndicator(R.drawable.ty_icon_beback_white)
        }
    }

    private val positionListener = object : SimpleMediaChangeListener() {
        override fun onPositionChange(playerName: String, position: Long, duration: Long) {
            if (duration != 0L) {
                val pos = 1000L * position / duration
                progressBar.progress = pos.toInt()
                mDuration.text = stringForTime(duration.toInt())
                mDurationPlayed.text = stringForTime(position.toInt())
            }
            lyricView.setCurrentTimeMillis(position)
        }

        override fun onMediaStateChange(playerName: String, mediaState: MediaPlayer.MediaState?) {
            when (mediaState) {
                MediaPlayer.MediaState.STOPPED,
                MediaPlayer.MediaState.PLAYING,
                MediaPlayer.MediaState.BUFFERING ->
                    runOnUiThread {
                        mControlPlayPause.isChecked = mMediaPlayer?.isPlaying ?: false
                    }
                null -> {
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constant.KEY_MUSIC_PARCELABLE, musicGridItem)
    }

    override fun onDownloading(progress: Float) {
    }

    override fun onDownloadFailed(exception: Exception) {
        handler.post {
            if (headerView.visibility == View.GONE) {
                if (this.getResources()
                        .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ) {
                    title.text = musicGridItem?.title
                    subTitle.text = musicGridItem?.singer
                } else {
                    title.text = ""
                    subTitle.text = ""
                }
                type.setImageResource(R.drawable.play_icon_song)
                title_tv.setText(musicGridItem?.title)
                author_tv.setText(musicGridItem?.singer)
                lyricView.visibility = View.GONE
                headerView.visibility = View.VISIBLE
            }
        }

    }

    override fun onDownloadSuccess(file: File) {
        handler.post {
            title.text = musicGridItem?.title
            subTitle.text = musicGridItem?.singer
            window.enterTransition = Fade()
            window.exitTransition = Fade()
            val transitionSet = TransitionSet()
            transitionSet.addTransition(ChangeBounds())
            transitionSet.addTransition(ChangeTransform())
            window.sharedElementEnterTransition = transitionSet
            window.sharedElementExitTransition = transitionSet
            type.setImageResource(R.drawable.play_icon_lyric)
            lyricView.setLyricFile(file)
            lyricView.visibility = View.VISIBLE
            headerView.visibility = View.GONE
        }
    }

}