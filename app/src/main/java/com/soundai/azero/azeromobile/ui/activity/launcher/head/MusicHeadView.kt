package com.soundai.azero.azeromobile.ui.activity.launcher.head

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.impl.SimpleMediaChangeListener
import com.soundai.azero.azeromobile.stringForTime
import com.soundai.azero.azeromobile.ui.activity.launcher.item.EnglishGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.IGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.MusicGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.convertEnglishGridItem2MusicGridItem
import com.soundai.azero.azeromobile.ui.activity.playerinfo.PlayingDetailsActivity
import com.soundai.azero.azeromobile.utils.Utils

class MusicHeadView(private val gridItem: IGridItem) : IHeadView {
    private var positionListener: SimpleMediaChangeListener? = null
    private var seeking = false
    private val mediaPlayer: MediaPlayerHandler by lazy {
        AzeroManager.getInstance().getHandler(
            AzeroManager.AUDIO_HANDLER
        ) as MediaPlayerHandler
    }

    @SuppressLint("SetTextI18n")
    override fun inflateHeadView(container: ViewGroup) {
        val activity = Utils.findActivity(container.context) ?: return
        val isEnglish: Boolean
        val targetItem = when (gridItem) {
            is MusicGridItem -> {
                isEnglish = false
                gridItem
            }
            is EnglishGridItem -> {
                isEnglish = true
                convertEnglishGridItem2MusicGridItem(gridItem)
            }
            else -> {
                return
            }
        }
        val rootView = LayoutInflater.from(activity).inflate(R.layout.cardview_music, container)
        val title = rootView.findViewById<TextView>(R.id.tv_song)
        val singerName = rootView.findViewById<TextView>(R.id.tv_author)
        val timeDuration = rootView.findViewById<TextView>(R.id.tv_timeline_duration)
        val timeTotal = rootView.findViewById<TextView>(R.id.tv_timeline_total)
        val progressBar = rootView.findViewById<SeekBar>(R.id.sb_progress)
        title.text = targetItem.title
        singerName.text = if (targetItem.AlbumName.isNotEmpty()) {
            "${targetItem.singer}-${targetItem.AlbumName}"
        } else {
            targetItem.singer
        }
        timeDuration.text = "00:00"
        timeTotal.text = "00:00"
        positionListener = object : SimpleMediaChangeListener() {
            override fun onPositionChange(playerName: String, position: Long, duration: Long) {
                if (duration != 0L) {
                    val pos = 1000L * position / duration
                    progressBar.progress = pos.toInt()
                    timeDuration.text = stringForTime(position.toInt())
                    timeTotal.text = stringForTime(duration.toInt())
                }
            }
        }
        mediaPlayer.addOnMediaStateChangeListener(positionListener)
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (seeking) {
                    val duration = mediaPlayer.duration
                    val position = seekBar.progress * duration / 1000L
                    timeDuration.text = stringForTime(position.toInt())
                    timeTotal.text = stringForTime(duration.toInt())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seeking = false
                val duration = mediaPlayer.duration
                var progress = seekBar.progress
                //直接拖到头状态会有问题
                if (seekBar.progress == 1000) {
                    progress = 999
                }
                val position = progress * duration / 1000L
                mediaPlayer.position = position
            }
        })
        rootView.setOnClickListener {
            PlayingDetailsActivity.start(activity, targetItem, isEnglish)
        }
    }

    override fun release() {
        positionListener?.let {
            mediaPlayer.removeOnMediaStateChangeListener(it)
        }
    }
}