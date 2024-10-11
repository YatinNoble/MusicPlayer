package com.example.voicemixed.audiorecording

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundPoolSoundPlayer(val context: Context) {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var streamId: Int = 0
    private val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private var isPlaying = false

    init {
        soundPool = SoundPool.Builder()
            .setMaxStreams(1) // Set max streams to 1 for this example
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun loadSound(assetFileName: String) {
        // Load the sound from the assets folder
        val afd = context.assets.openFd(assetFileName)
        soundId = soundPool!!.load(afd, 1)
    }

    fun playSound() {
        if (soundId != 0 && !isPlaying) {
            streamId = soundPool?.play(soundId, 1f, 1f, 1, 0, 1f) ?: 0
            isPlaying = true
        }
    }

    fun pauseSound() {
        if (isPlaying) {
            soundPool?.pause(streamId)
            isPlaying = false
        }
    }

    fun resumeSound() {
        if (!isPlaying) {
            soundPool?.resume(streamId)
            isPlaying = true
        }
    }

    fun release() {
        soundPool?.release()
    }
}