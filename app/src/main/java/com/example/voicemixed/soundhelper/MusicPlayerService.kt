package com.example.voicemixed.soundhelper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.voicemixed.R
import com.example.voicemixed.notification.NotificationReceiver

class MusicPlayerService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    private val binder = MusicBinder()
    private lateinit var soundPlayer: SoundPlayer
    private val notificationChannelId = "music_player_channel"
    private val onGoingNotification = 1

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        soundPlayer = SoundPlayer(this)
        mediaSession = MediaSessionCompat(baseContext, "My Music")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                PlayState.PLAY.toString() -> playAllSounds()
                PlayState.PAUSE.toString() -> pauseAllSounds()
            }
        }
        return START_NOT_STICKY
    }


    fun playAllSounds() {
        val soundList = soundPlayer.currentPlayingSound()
        if (soundList.size > 0) {
            soundPlayer.playAllFromNotification()
            updateNotification(
                "Play: ${soundList[soundList.size - 1].sound.soundName}",
                PlayState.PAUSE.toString(),
                R.drawable.pause_icon
            )
            PlaybackStateUtil.sendPlaybackStateBroadcast(this, PlayState.PAUSE)
        }
    }


    fun pauseAllSounds() {
        val soundList = soundPlayer.currentPlayingSound()
        if (soundList.size > 0) {
            soundPlayer.stopAllFromNotification()
            updateNotification(
                "Pause: ${soundList[soundList.size - 1].sound.soundName}",
                PlayState.PLAY.toString(),
                R.drawable.play_icon
            )
            PlaybackStateUtil.sendPlaybackStateBroadcast(this, PlayState.PLAY)
        }
    }

    fun playMusic(sound: Sound, playPauseBtn: Int) {
        val soundList = soundPlayer.currentPlayingSound()
        if (soundList.size > 0) {
            val playing = soundList.filter { it.mediaPlayer.isPlaying }
            if (playing.isEmpty()) {
                soundPlayer.play(sound)
                updateNotification(
                    "Pause: ${sound.soundName}",
                    PlayState.PLAY.toString(),
                    playPauseBtn
                )
            } else {
                soundPlayer.play(sound)
                updateNotification(
                    "Play: ${sound.soundName}",
                    PlayState.PAUSE.toString(),
                    playPauseBtn
                )
            }
        } else {
            soundPlayer.play(sound)
            startForeground(
                onGoingNotification,
                createNotification(
                    "Play: ${sound.soundName}",
                    PlayState.PAUSE.toString(),
                    playPauseBtn
                )
            )
        }
    }


    // Pause music by sound resource ID
    fun pauseMusic(sound: Sound) {
        soundPlayer.pause(sound)
        val soundList = soundPlayer.currentPlayingSound()
        if (soundList.size > 0) {
            val playing = soundList.filter { it.mediaPlayer.isPlaying }
            if (playing.isEmpty()) {
                updateNotification(
                    "Pause: ${soundList[soundList.size - 1].sound.soundName}",
                    PlayState.PLAY.toString(),
                    R.drawable.play_icon
                )
                PlaybackStateUtil.sendPlaybackStateBroadcast(this, PlayState.PLAY)
            } else {
                updateNotification(
                    "Play: ${soundList[soundList.size - 1].sound.soundName}",
                    PlayState.PAUSE.toString(),
                    R.drawable.pause_icon
                )
                PlaybackStateUtil.sendPlaybackStateBroadcast(this, PlayState.PAUSE)
            }
        } else {
            stopForeground(true)
            PlaybackStateUtil.sendPlaybackStateBroadcast(this, PlayState.PLAY)
        }
    }


    // Set volume for a specific sound
    fun setVolume(sound: Sound) {
        soundPlayer.setVolume(sound)
    }

    // get currentPlayingSound list
    fun getCurrentPlaying(): ArrayList<PlayingSound> {
        return soundPlayer.currentPlayingSound()
    }

    // any single songs play or not
    private fun oneSongObjectData(): Sound? {
        return soundPlayer.oneSongPlay()
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            notificationChannelId,
            "Music Player",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    // Update the existing notification
    private fun updateNotification(songsPlay: String, playPauseText: String, playPauseBtn: Int) {
        val notification = createNotification(songsPlay, playPauseText, playPauseBtn)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(onGoingNotification, notification)
    }

    private fun createNotification(
        songsPlay: String,
        playPauseText: String,
        playPauseBtn: Int
    ): Notification {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(playPauseText)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)


        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.exit_icon)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setContentTitle(songsPlay)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) // Ensure alert only on the first display
            .addAction(playPauseBtn, playPauseText, playPendingIntent)
            .build()

        return notification
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.stopAll()
        PlaybackStateUtil.sendPlaybackStateBroadcast(this, PlayState.PLAY)
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }
}
