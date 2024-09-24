package com.example.voicemixed.soundhelper

import android.content.Context
import android.media.MediaPlayer

data class PlayingSound(val mediaPlayer: MediaPlayer, val sound: Sound)


class SoundPlayer(private val context: Context) {

    private val playingSounds = ArrayList<PlayingSound>()

    fun play(sound: Sound) {
        val existingSound = playingSounds.find { it.sound.soundName == sound.soundName }
        if (existingSound == null) {
            val mediaPlayer = MediaPlayer.create(context, sound.mp3File).apply {
                isLooping = true // Configure looping as needed
                val currentlyPlaying = playingSounds.any { it.mediaPlayer.isPlaying }
                if (playingSounds.size == 0) {
                    start()
                    PlaybackStateUtil.sendPlaybackStateBroadcast(context, PlayState.PAUSE)
                } else if (currentlyPlaying) {
                    start()
                    PlaybackStateUtil.sendPlaybackStateBroadcast(context, PlayState.PAUSE)
                }
            }
            // Set the volume after starting the media player
            mediaPlayer.setVolume(sound.soundVolume, sound.soundVolume)
            playingSounds.add(PlayingSound(mediaPlayer, sound))
        }
    }

    // Pause the current sound
    fun pause(sound: Sound) {
        val soundInstance = playingSounds.find { it.sound.soundName == sound.soundName }
        soundInstance?.let {
            it.mediaPlayer.stop()
            it.mediaPlayer.release()
            playingSounds.remove(it) // Remove from the list
        }
    }


    // Set volume for a specific sound
    fun setVolume(sound: Sound) {
        val soundInstance = playingSounds.find { it.sound.soundName == sound.soundName }
        soundInstance?.mediaPlayer?.setVolume(sound.soundVolume, sound.soundVolume)
    }


    // Play all sounds from notification
    fun playAllFromNotification() {
        for (sound in playingSounds) {
            sound.mediaPlayer.start()
        }
    }


    // Stop all sounds from notification
    fun stopAllFromNotification() {
        for (sound in playingSounds) {
            if (sound.mediaPlayer.isPlaying) {
                sound.mediaPlayer.pause()  // Pause instead of stop
            }
        }
    }


    // Stop all sounds
    fun stopAll() {
        for (sound in playingSounds) {
            if (sound.mediaPlayer.isPlaying) {
                sound.mediaPlayer.stop()
            }
            sound.mediaPlayer.release()
        }
        playingSounds.clear()
    }


    // Get the list of currently playing sounds
    fun currentPlayingSound(): ArrayList<PlayingSound> {
        return this.playingSounds
    }


    fun oneSongPlay(): Sound? {
        val playing = playingSounds.filter { it.mediaPlayer.isPlaying }
        return if (playing.size == 1) {
            playing.first().sound // Return the single playing sound
        } else {
            null // Return null if no sound is playing or if multiple sounds are playing
        }
    }

}
