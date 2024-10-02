package com.example.voicemixed.activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import com.example.voicemixed.databinding.ActivityRecordingSaveBinding

class RecordingSaveActivity : AudioRecordingBaseActivity() {
    private lateinit var binding: ActivityRecordingSaveBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaPlayer02: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer()
        mediaPlayer02 = MediaPlayer()

        binding.startRecording.setOnClickListener {
            onRecordButtonClick()
        }

        binding.playSongs.setOnClickListener {
            playAudio("upbeat_pop_intro_logo.mp3")
        }

        binding.stopSong.setOnClickListener {
            stopAudio()
        }

        binding.playSongs02.setOnClickListener {
            playAudio02("tum_hi_ho_bandhu.mp3")
        }

        binding.stopSong02.setOnClickListener {
            stopAudio02()
        }

        binding.NavigateNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun onRecordButtonClick() {
        if (isRecording) {
            stopAudioCapture()
            binding.startRecording.text = "Start Recording"
        } else {
            checkPermissionsAndStartRecording()
            isRecording = true
            binding.startRecording.text = "Stop Recording"
        }
    }

    private fun playAudio(fileName: String) {
        mediaPlayer.reset()
        val assetFileDescriptor = assets.openFd(fileName)
        mediaPlayer.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private fun playAudio02(fileName: String) {
        mediaPlayer02.reset()
        val assetFileDescriptor = assets.openFd(fileName)
        mediaPlayer02.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )
        mediaPlayer02.prepare()
        mediaPlayer02.start()
    }

    private fun stopAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    private fun stopAudio02() {
        if (mediaPlayer02.isPlaying) {
            mediaPlayer02.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaPlayer02.release()
    }

    override fun onServiceBound() {
        // Handle actions once the service is bound
    }

}
