package com.example.voicemixed.audiorecording

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import com.example.voicemixed.mediamixer.TrimAudioModel
import com.example.voicemixed.mediamixer.UtilKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RecordingSoundPlayer(private val context: Context) {
    private val mediaPlayers: MutableMap<String, MediaPlayer> = mutableMapOf()
    private val completedSounds = ArrayList<TrimAudioModel>()
    private val playedDurations: MutableMap<String, Float> = mutableMapOf()
    private val delayOffsets: MutableMap<String, Float> =
        mutableMapOf()  // Store delay offsets for each audio
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var lastPlayTime = 0L
    private val playCooldown = 500L // Milliseconds

    fun play(recordingSound: String, delayOffset: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPlayTime < playCooldown) {
            Log.w("RecordingSoundPlayer", "Play request throttled for sound: $recordingSound")
            return
        }
        lastPlayTime = currentTime

        synchronized(this) {
            delayOffsets[recordingSound] = delayOffset  // Store the delay for the specific sound
            coroutineScope.launch(Dispatchers.IO) {
                val existingPlayer = mediaPlayers[recordingSound]
                val afd = context.assets.openFd(recordingSound)
                if (existingPlayer != null) {
                    if (existingPlayer.isPlaying) {
                        val currentPosition = existingPlayer.currentPosition.toFloat() / 1000
                        playedDurations[recordingSound] = currentPosition
                        addDataInCompletionList(
                            UtilKt.getFileFromAssets(context, recordingSound).absolutePath,
                            playedDurations[recordingSound] ?: 0f,
                            delayOffsets[recordingSound] ?: 0f
                        )
                        existingPlayer.stop()
                        existingPlayer.reset()
                    }
                    // Reinitialize the player with new data source
                    existingPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    existingPlayer.prepare()
                    existingPlayer.start()
                } else {
                    val mediaPlayer = MediaPlayer().apply {
                        setAudioStreamType(AudioManager.STREAM_MUSIC)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        prepare()
//                    val currentPosition = currentPosition.toFloat() / 1000
//                    playedDurations[recordingSound] = currentPosition
                        setOnCompletionListener {
                            val totalDurationInSeconds: Float = duration.toFloat() / 1000
                            playedDurations[recordingSound] = totalDurationInSeconds
                            addDataInCompletionList(
                                UtilKt.getFileFromAssets(
                                    context,
                                    recordingSound
                                ).absolutePath,
                                playedDurations[recordingSound] ?: 0f,
                                delayOffsets[recordingSound] ?: 0f
                            )
                            mediaPlayers.remove(recordingSound)  // Remove player from map after completion
                        }
                    }
                    mediaPlayer.start()
                    mediaPlayers[recordingSound] = mediaPlayer
                }
            }
        }
    }

    /*fun getCompletedSounds(): ArrayList<TrimAudioModel> {
        for ((recordingSound, player) in mediaPlayers) {
            if (player.isPlaying) {
                val currentPosition: Float = player.currentPosition.toFloat() / 1000
                playedDurations[recordingSound] = currentPosition
                addDataInCompletionList(
                    UtilKt.getFileFromAssets(context, recordingSound).absolutePath,
                    playedDurations[recordingSound] ?: 0f,
                    delayOffsets[recordingSound] ?: 0f  // Use the specific delay for each sound
                )
            }
            player.stop()
            player.release()
        }
        return completedSounds
    }*/

    fun getCompletedSounds(): ArrayList<TrimAudioModel> {
        synchronized(this) {
            for ((recordingSound, player) in mediaPlayers) {
                if (player.isPlaying) {
                    val currentPosition: Float = player.currentPosition.toFloat() / 1000
                    playedDurations[recordingSound] = currentPosition
                    addDataInCompletionList(
                        UtilKt.getFileFromAssets(context, recordingSound).absolutePath,
                        playedDurations[recordingSound] ?: 0f,
                        delayOffsets[recordingSound] ?: 0f // Use the specific delay for each sound
                    )
                }
                try {
                    player.stop()
                    player.release()
                } catch (e: IllegalStateException) {
                    Log.e("RecordingSoundPlayer", "Error while stopping MediaPlayer: ${e.message}")
                }
            }
            return completedSounds
        }
    }

    private fun addDataInCompletionList(soundName: String, endOffset: Float, delayOffset: Float) {
        Log.d("Hello==>>", "delayOffset: $delayOffset")
        Log.d("Hello==>>", "endOffset: $endOffset")
        completedSounds.add(TrimAudioModel(soundName, 0, endOffset, 3f, delayOffset))
    }

    fun stopAllSounds() {
        synchronized(this) {
            for (recording in mediaPlayers.keys.toList()) {
                val player = mediaPlayers[recording]
                try {
                    if (player != null) {
                        if (player.isPlaying) {
                            player.stop()
                        }
                        player.release()
                    }
                } catch (e: IllegalStateException) {
                    Log.e(
                        "RecordingSoundPlayer",
                        "IllegalStateException while stopping and releasing MediaPlayer: ${e.message}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "RecordingSoundPlayer",
                        "Error while stopping and releasing MediaPlayer: ${e.message}",
                        e
                    )
                } finally {
                    mediaPlayers.remove(recording)
                }
            }
            completedSounds.clear()
            playedDurations.clear()
            delayOffsets.clear() // Clear all stored delay offsets
        }
    }

}
