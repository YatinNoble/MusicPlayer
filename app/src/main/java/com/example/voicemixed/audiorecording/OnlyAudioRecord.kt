package com.example.voicemixed.audiorecording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

class OnlyAudioRecord {

    private val samplingRateInHz = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSizeFactor = 2
    private val bufferSize = AudioRecord.getMinBufferSize(
        samplingRateInHz, channelConfig, audioFormat
    ) * bufferSizeFactor

    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private val recordingInProgress = AtomicBoolean(false)
    private var outputWavFile: File? = null

    fun startRecording(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Initialize AudioRecord with REMOTE_SUBMIX
        try {
            recorder = AudioRecord(
                MediaRecorder.AudioSource.REMOTE_SUBMIX,
                samplingRateInHz,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } catch (e: Exception) {
            Log.e("AudioRecord", "Error initializing AudioRecord: ${e.message}")
            return // Exit if initialization fails
        }


        // Check if the recorder was initialized correctly
        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "AudioRecord initialization failed: ${recorder?.state}")
            return
        }

        recorder?.startRecording()

        if (recorder?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            throw RuntimeException("Failed to start recording")
        }

        recordingInProgress.set(true)

        // Initialize the output WAV file
        outputWavFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "recordingWav.wav"
        )

        // Delete the existing file if it exists
        outputWavFile?.delete()

        recordingThread = Thread(RecordingRunnable(), "Recording Thread")
        recordingThread?.start()
    }

    fun stopRecording(context: Context) {
        if (recorder == null) return

        recordingInProgress.set(false)

        recorder?.stop()
        recorder?.release()

        recorder = null
        recordingThread = null


        // Convert WAV to MP3 (optional)
//        outputWavFile?.let { convertWavToMp3(it) }

//        playRecordedAudio(context)
    }

    private inner class RecordingRunnable : Runnable {
        override fun run() {
            val buffer = ByteArray(bufferSize)
            val outputStream = ByteArrayOutputStream()

            try {
                while (recordingInProgress.get()) {
                    val result = recorder?.read(buffer, 0, buffer.size) ?: AudioRecord.ERROR
                    if (result < 0) {
                        throw RuntimeException(
                            "Reading of audio buffer failed: ${getBufferReadFailureReason(result)}"
                        )
                    }
                    outputStream.write(buffer, 0, result) // Write only valid bytes
                }

                // Save the output to a file after recording is stopped
                writeWavFile(outputWavFile, outputStream.toByteArray())


            } catch (e: IOException) {
                throw RuntimeException("Writing of recorded audio failed", e)
            }
        }

        private fun getBufferReadFailureReason(errorCode: Int?): String {
            return when (errorCode) {
                AudioRecord.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
                AudioRecord.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
                AudioRecord.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT"
                AudioRecord.ERROR -> "ERROR"
                else -> "Unknown ($errorCode)"
            }
        }
    }

    // Function to write the final WAV file
    private fun writeWavFile(outputFile: File?, audioData: ByteArray) {
        outputFile?.let { file ->
            try {
                FileOutputStream(file).use { fos ->
                    fos.write(createWavHeader(audioData.size))
                    fos.write(audioData)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Helper function to create a valid WAV header
    private fun createWavHeader(audioDataLength: Int): ByteArray {
        val totalDataLen = audioDataLength + 36
        val byteRate = 16 * samplingRateInHz / 8

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())                // ChunkID
        buffer.putInt(totalDataLen)                     // ChunkSize
        buffer.put("WAVE".toByteArray())                // Format
        buffer.put("fmt ".toByteArray())                // Subchunk1ID
        buffer.putInt(16)                               // Subchunk1Size (16 for PCM)
        buffer.putShort(1.toShort())                    // AudioFormat (1 for PCM)
        buffer.putShort(1.toShort())                    // NumChannels (1 for mono)
        buffer.putInt(samplingRateInHz)                 // SampleRate
        buffer.putInt(byteRate)                         // ByteRate
        buffer.putShort((1 * 16 / 8).toShort())         // BlockAlign (NumChannels * BitsPerSample / 8)
        buffer.putShort(16.toShort())                   // BitsPerSample
        buffer.put("data".toByteArray())                // Subchunk2ID
        buffer.putInt(audioDataLength)                  // Subchunk2Size

        return header
    }

    private fun convertWavToMp3(waveFile: File) {
        val mp3File = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "recording.mp3"
        )

        if (mp3File.exists()) {
            mp3File.delete()
        }

        if (waveFile.exists()) {
            val ffmpegCommand = arrayOf(
                "-i", waveFile.absolutePath,
                "-codec:a", "libmp3lame",
                "-qscale:a", "2",
                mp3File.absolutePath
            )

            val rc = FFmpeg.execute(ffmpegCommand)
            if (rc == 0) {
                Log.i("FFmpeg", "Conversion to MP3 successful")
            } else {
                Log.e("FFmpeg", "Error in MP3 conversion")
            }
        }
    }

    private fun playRecordedAudio(context: Context) {
        outputWavFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                val mediaPlayer = MediaPlayer()
                try {
                    mediaPlayer.setDataSource(file.absolutePath)

                    // Set error listener
                    mediaPlayer.setOnErrorListener { mp, what, extra ->
                        Log.e("AudioPlayback", "MediaPlayer error: what=$what, extra=$extra")
                        true // Indicates that the error was handled
                    }

                    // Prepare the MediaPlayer asynchronously
                    mediaPlayer.prepareAsync()

                    // Start playback when prepared
                    mediaPlayer.setOnPreparedListener {
                        mediaPlayer.setVolume(1f, 1f)
                        mediaPlayer.start() // Start playback once prepared
                    }

                    // Release resources after playback completion
                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.release() // Release resources after playback
                    }
                } catch (e: IOException) {
                    Log.e(
                        "AudioPlayback",
                        "Error setting data source or preparing media player: ${e.message}"
                    )
                }
            } else {
                Log.e("AudioPlayback", "WAV file does not exist or is empty!")
            }
        }
    }

}
