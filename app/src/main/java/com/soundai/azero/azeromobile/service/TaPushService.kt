package com.soundai.azero.azeromobile.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.azero.sdk.util.log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.soundai.azero.azeromobile.R
import com.umeng.message.UmengMessageService

class TaPushService : UmengMessageService() {
    override fun onMessage(context: Context, intent: Intent) {
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val message = Gson().fromJson(intent.getStringExtra("body"), Message::class.java)
        log.i("onReceive push message: $message")
        val notification = NotificationCompat.Builder(this, "azero_push")
            .setContentTitle(message.body.title)
            .setContentText(message.body.text)
            .setSmallIcon(R.mipmap.azero_logo)
            .build()
        manager.notify(1, notification)
    }
}

private data class Message(
    @SerializedName("display_type")
    val displayType: String,
    val body: Body,
    @SerializedName("msg_id")
    val msgId: String
)

private data class Body(
    @SerializedName("after_open")
    val afterOpen: String,
    val ticker: String,
    val title: String,
    @SerializedName("play_sound")
    val playSound: String,
    @SerializedName("play_lights")
    val playLights: String,
    @SerializedName("play_vibrate")
    val playVibrate: String,
    val text: String
)
