package com.soundai.azero.azeromobile.system

import com.azero.platforms.iface.MediaPlayer.*
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler
import com.azero.sdk.impl.MediaPlayer.RawSpeakAudioMediaPlayerHandler
import com.azero.sdk.util.log

class SpeakAudioCoordinator(
    private val speakPlayer: RawSpeakAudioMediaPlayerHandler,
    private val audioPlayer: MediaPlayerHandler
) {
    var audioMute = false
        set(value) {
            field = value
            notifyStateChange()
        }
    private var audioState = MediaState.STOPPED
    private var speakState = MediaState.STOPPED

    private val speakMediaStateListener by lazy {
        object : OnMediaStateChangeListener {
            override fun onMediaError(
                playerName: String,
                error: String,
                type: MediaError
            ) {
                speakState = MediaState.STOPPED
                notifyStateChange()
            }

            override fun onMediaStateChange(
                playerName: String,
                state: MediaState
            ) {
                speakState = state
                notifyStateChange()
            }

            override fun onPositionChange(
                playerName: String,
                position: Long,
                duration: Long
            ) {
                // do nothing
            }
        }
    }

    private val audioMediaStateListener by lazy {
        object : OnMediaStateChangeListener {
            override fun onMediaError(
                playerName: String,
                error: String,
                type: MediaError
            ) {
                audioState = MediaState.STOPPED
                notifyStateChange()
            }

            override fun onMediaStateChange(
                playerName: String,
                state: MediaState
            ) {
                audioState = state
                notifyStateChange()
            }

            override fun onPositionChange(
                playerName: String,
                position: Long,
                duration: Long
            ) {
                // do nothing
            }
        }
    }

    init {
        initListeners()
    }

    fun release() {
        speakPlayer.removeOnMediaStateChangeListener(speakMediaStateListener)
        audioPlayer.removeOnMediaStateChangeListener(audioMediaStateListener)
    }

    private fun initListeners() {
        speakPlayer.addOnMediaStateChangeListener(speakMediaStateListener)
        audioPlayer.addOnMediaStateChangeListener(audioMediaStateListener)
    }

    private fun notifyStateChange() {
        log.d("state change, speakState= $speakState  audioState= $audioState")
        if (audioMute) {
            audioPlayer.setPlayerVolume(0)
            return
        }
        if (speakState == MediaState.PLAYING && speakState == audioState) {
            audioPlayer.setPlayerVolume(10)
        } else {
            audioPlayer.setPlayerVolume(100)
        }
    }
}