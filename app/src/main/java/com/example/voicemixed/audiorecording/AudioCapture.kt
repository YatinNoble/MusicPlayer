package com.example.voicemixed.audiorecording

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class AudioCapture(private val context: Context) {

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
    private var mediaProjection: MediaProjection? = null

    // Request code for capturing audio
    private val REQUEST_CODE_AUDIO_CAPTURE = 1001
    // Request MediaProjection to capture audio

    private lateinit var mediaProjectionManager: MediaProjectionManager

    // Start the audio recording
    fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mediaProjectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        (context as? Activity)?.startActivityForResult(captureIntent, REQUEST_CODE_AUDIO_CAPTURE)

        // Initialize the AudioRecord object
        recorder = AudioRecord(
            MediaRecorder.AudioSource.REMOTE_SUBMIX,
            samplingRateInHz,
            channelConfig,
            audioFormat,
            bufferSize
        )


        recorder?.startRecording()
        recordingInProgress.set(true)

        // Initialize the output WAV file
        outputWavFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "recordingWav.wav"
        )
        outputWavFile?.delete() // Delete existing file

        recordingThread = Thread(RecordingRunnable(), "Recording Thread")
        recordingThread?.start()
    }

    // Stop the audio recording
    fun stopRecording() {
        if (recorder == null) return

        recordingInProgress.set(false)

        recorder?.stop()
        recorder?.release()

        recorder = null
        recordingThread = null

        // Convert WAV to MP3 after recording is stopped
//        outputWavFile?.let { convertWavToMp3(it) }
    }

    // Runnable class to handle audio recording in a separate thread
    private inner class RecordingRunnable : Runnable {
        override fun run() {
            val buffer = ByteArray(bufferSize)
            val outputStream = FileOutputStream(outputWavFile)

            try {
                while (recordingInProgress.get()) {
                    val result = recorder?.read(buffer, 0, buffer.size) ?: AudioRecord.ERROR
                    if (result < 0) {
                        throw RuntimeException("Reading of audio buffer failed")
                    }
                    outputStream.write(buffer, 0, result)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                outputStream.close()
            }
        }
    }

    // Convert the recorded WAV file to MP3 format
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

    // Handle MediaProjection result
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_AUDIO_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Get the MediaProjection instance
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)

            // You can now proceed with starting the audio capture or recording
            startRecording() // Call your recording start method
        } else {
            // Handle the case where permission was denied
            Log.e("AudioCapture", "Audio capture permission denied")
        }
    }
}
