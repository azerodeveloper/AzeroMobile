package com.soundai.azero.azeromobile.impl

import com.azero.platforms.iface.MediaPlayer

/**
 * Create by xingw on 2019/11/9
 */
open class SimpleMediaChangeListener : MediaPlayer.OnMediaStateChangeListener{
    override fun onPositionChange(playerName: String, position: Long, duration: Long) {
    }

    override fun onMediaStateChange(playerName: String, mediaState: MediaPlayer.MediaState?) {
    }

    override fun onMediaError(playerName: String, msg: String, mediaError: MediaPlayer.MediaError?) {
    }

}