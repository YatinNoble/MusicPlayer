package com.example.voicemixed.activity

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.voicemixed.databinding.ActivityPlaySongsBinding

class PlaySongsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaySongsBinding

    private lateinit var mediaPlayer1: MediaPlayer
    private lateinit var mediaPlayer2: MediaPlayer
    private lateinit var mediaPlayer3: MediaPlayer
    private lateinit var mediaPlayer4: MediaPlayer
    private lateinit var mediaPlayer5: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer1 = MediaPlayer()
        mediaPlayer2 = MediaPlayer()
        mediaPlayer3 = MediaPlayer()
        mediaPlayer4 = MediaPlayer()
        mediaPlayer5 = MediaPlayer()

        binding.playSongs1.setOnClickListener {
            playAudio01("upbeat_pop_intro_logo.mp3", mediaPlayer1)
        }

        binding.stopSong1.setOnClickListener {
            stopAudio(mediaPlayer1)
        }

        binding.playSongs2.setOnClickListener {
            playAudio01("dhuni_re_dhakhavi.mp3", mediaPlayer2)
        }

        binding.stopSong2.setOnClickListener {
            stopAudio(mediaPlayer2)
        }

        binding.playSongs3.setOnClickListener {
            playAudio01("news_ident.mp3", mediaPlayer3)
        }

        binding.stopSong3.setOnClickListener {
            stopAudio(mediaPlayer3)
        }

        binding.playSongs4.setOnClickListener {
            playAudio01("sunn_raha_hai_na_tu.mp3", mediaPlayer4)
        }

        binding.stopSong4.setOnClickListener {
            stopAudio(mediaPlayer4)
        }

        binding.playSongs5.setOnClickListener {
            playAudio01("tum_hi_ho_bandhu.mp3", mediaPlayer5)
        }

        binding.stopSong5.setOnClickListener {
            stopAudio(mediaPlayer5)
        }

    }

    private fun playAudio01(fileName: String, mediaPlayer: MediaPlayer) {
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

    private fun stopAudio(mediaPlayer: MediaPlayer) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }
}