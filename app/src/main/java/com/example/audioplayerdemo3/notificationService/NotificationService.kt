package com.example.audioplayerdemo3.notificationService

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService: MediaSessionService() {


    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var audioNotificationManager: AudioNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        audioNotificationManager.startNotificationService(this,mediaSession)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.apply {
            release()
            if (player.playbackState != Player.STATE_IDLE){
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
    }
}