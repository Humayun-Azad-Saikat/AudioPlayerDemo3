package com.example.audioplayerdemo3.notificationService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.example.audioplayerdemo3.R
import kotlin.math.exp


const val NOTIFICATION_ID = 1
const val NOTIFICATION_CHANNEL_NAME = "Audio player"
const val NOTIFICATION_CHANNEL_ID = "Audio player channel id 1"


class AudioNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer
) {

    init {
        createNotificationChannel()
    }

    private val notificationManger: NotificationManagerCompat = NotificationManagerCompat.from(context)


    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession
    ){
        buildNotification(mediaSession)
        startForegroundNotificationService(mediaSessionService)
    }

    private fun startForegroundNotificationService(mediaSessionService: MediaSessionService){
        val notification = Notification.Builder(context,NOTIFICATION_CHANNEL_ID)
            .setCategory(Notification.CATEGORY_SERVICE).build()

        mediaSessionService.startForeground(NOTIFICATION_ID,notification)
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(
        mediaSession: MediaSession
    ){

        PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            //.setMediaDescriptionAdapter()
            .setSmallIconResourceId(R.drawable.ic_launcher_foreground)
            .build()
            .also {
                //it.setMediaSessionToken(mediaSession.ses)
                it.setUseFastForwardActionInCompactView(true)
                it.setUseRewindActionInCompactView(true)
                it.setUseNextActionInCompactView(true)
                it.setPriority(NotificationCompat.PRIORITY_LOW)
                it.setPlayer(exoPlayer)
            }


    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManger.createNotificationChannel(channel)
    }

}