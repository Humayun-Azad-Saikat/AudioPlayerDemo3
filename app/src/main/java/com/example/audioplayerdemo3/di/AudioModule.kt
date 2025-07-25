package com.example.audioplayerdemo3.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.example.audioplayerdemo3.audioService.AudioServiceHandler
import com.example.audioplayerdemo3.notificationService.AudioNotificationManager
import com.example.audioplayerdemo3.notificationService.NOTIFICATION_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule  {

    @Provides
    @Singleton
    fun providesAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Singleton
    @UnstableApi
    fun providesExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()


    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer,
    ): MediaSession = MediaSession.Builder(context, player).build()


    @Provides
    @Singleton
    fun providesAudioNotificationManager(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer
    ): AudioNotificationManager{
        return AudioNotificationManager(context,exoPlayer)
    }


    @Provides
    @Singleton
    fun providesAudioServiceHandler(
        exoPlayer: ExoPlayer
    ): AudioServiceHandler = AudioServiceHandler(exoPlayer)

}