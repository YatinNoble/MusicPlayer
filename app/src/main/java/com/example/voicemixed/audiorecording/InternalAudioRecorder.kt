package com.example.voicemixed.audiorecording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RequiresApi(Build.VERSION_CODES.Q)
class InternalAudioRecorder(
    private val context: Context,
    private val mediaProjection: MediaProjection
) {
    private val samplingRateInHz = 44100
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private val bufferSize: Int
    private var outputFilePath: File? = null

    init {
        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(samplingRateInHz)
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .build()

        bufferSize = AudioRecord.getMinBufferSize(
            samplingRateInHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val playbackCaptureConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        audioRecord = AudioRecord.Builder()
            .setAudioFormat(audioFormat)
            .setAudioPlaybackCaptureConfig(playbackCaptureConfig)
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

    fun startRecording() {
        // Initialize the output WAV file
        outputFilePath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "recordingWav.wav"
        )
        audioRecord?.startRecording()
        isRecording = true
        // Delete the existing file if it exists
        outputFilePath?.delete()
        recordingThread = Thread(RecordingRunnable(), "Recording Thread")
        recordingThread?.start()
    }

    private inner class RecordingRunnable : Runnable {
        override fun run() {
            val buffer = ByteArray(bufferSize)
            val outputStream = ByteArrayOutputStream()

            try {
                while (isRecording) {
                    val result = audioRecord?.read(buffer, 0, buffer.size) ?: AudioRecord.ERROR
                    if (result < 0) {
                        throw RuntimeException(
                            "Reading of audio buffer failed: ${getBufferReadFailureReason(result)}"
                        )
                    }
                    outputStream.write(buffer, 0, result) // Write only valid bytes
                }

                // Save the output to a WAV file after recording is stopped
                writeWavFile(outputFilePath, outputStream.toByteArray())

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

    private fun writeWavFile(outputFilePath: File?, audioData: ByteArray) {
        outputFilePath?.let { file ->
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

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        recordingThread?.join()
    }
}
