package com.example.voicemixed.audiorecording

import android.content.Context
import android.media.MediaPlayer
import com.example.voicemixed.mediamixer.UtilKt

data class AudioData(
    val songsName: String,
    val songsId: Int,
    val delayOffset: Float = 0f,
    val endDuration: Float = 0f,
    val songsFilepath: String = ""
)

class PlayMultipleAudio(val context: Context) {

    private val mediaPlayers = mutableMapOf<String, MediaPlayer>()
    private val completedSounds = ArrayList<AudioData>()
    private val playDurations = mutableMapOf<String, Float>()
    private val delayOffsets = mutableMapOf<String, Float>() // Store delay offsets for each song

    fun playSongs(audioData: AudioData) {
        delayOffsets[audioData.songsName] = audioData.delayOffset
        if (mediaPlayers.containsKey(audioData.songsName)) {
            val mediaPlayer = mediaPlayers[audioData.songsName]
            if (mediaPlayer?.isPlaying == true) {
                val currentPosition: Float =
                    mediaPlayer.currentPosition.toFloat() // Current position in milliseconds
                mediaPlayer.pause() // Pause the playback

                val totalPlayedDuration = (playDurations[audioData.songsName]
                    ?: 0f) + currentPosition // Total played time
                playDurations[audioData.songsName] = totalPlayedDuration
                completedSounds.add(audioData.copy(endDuration = totalPlayedDuration))
                playDurations.remove(audioData.songsName)
                mediaPlayer.seekTo(0)
                mediaPlayer.start()

            } else {
                mediaPlayer?.start()
            }
        } else {
            val mediaPlayer = MediaPlayer.create(context, audioData.songsId)
            mediaPlayer.start()
            mediaPlayers[audioData.songsName] = mediaPlayer
            mediaPlayer.setOnCompletionListener {
                val actualDuration: Float = mediaPlayer.duration.toFloat()
                val totalPlayedDuration =
                    (playDurations[audioData.songsName] ?: 0f) + actualDuration // Total played time
                mediaPlayer.release()
                mediaPlayers.remove(audioData.songsName)
                completedSounds.add(audioData.copy(endDuration = totalPlayedDuration))
                playDurations.remove(audioData.songsName)
            }
        }
    }

    fun getAllCompletedSoundsData(): ArrayList<AudioData> {
        return completedSounds
    }


    fun stopAll() {
        // Iterate over a copy of the keys to avoid ConcurrentModificationException
        mediaPlayers.keys.toList().forEach { songName ->
            val mediaPlayer = mediaPlayers[songName]
            if (mediaPlayer?.isPlaying == true) {
                val currentPosition = mediaPlayer.currentPosition // Get current playback position
                mediaPlayer.stop() // Stop playback
                mediaPlayer.release() // Release the MediaPlayer resources

                // Add the song's data to completed sounds
                val totalPlayedDuration = (playDurations[songName] ?: 0f) + currentPosition
                val filePathSongs = UtilKt.getFileFromAssets(
                    context,
                    songName
                ).absolutePath
                completedSounds.add(
                    AudioData(
                        songName,
                        mediaPlayer.hashCode(),
                        delayOffset = delayOffsets[songName] ?: 0f,
                        endDuration = totalPlayedDuration,
                        songsFilepath = filePathSongs
                    )
                )
                // Clean up
                mediaPlayers.remove(songName)
                playDurations.remove(songName)
            }
        }
    }


    fun clearAllDataThenStart() {
        mediaPlayers.clear()
        playDurations.clear()
        completedSounds.clear()
    }
}