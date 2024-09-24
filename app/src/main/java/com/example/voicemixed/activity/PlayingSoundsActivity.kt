package com.example.voicemixed.activity

import android.os.Bundle
import com.example.voicemixed.adapter.OnlyPlayingSoundAdapter
import com.example.voicemixed.soundhelper.PlayingSound
import com.example.voicemixed.soundhelper.Sound
import com.example.voicemixed.util.UserManager
import com.example.voicemixed.databinding.ActivityPlayingSoundsBinding

class PlayingSoundsActivity : BaseActivity() {
    private lateinit var binding: ActivityPlayingSoundsBinding
    private var onlyPlayingSoundList = ArrayList<PlayingSound>()
    private lateinit var onlyPlayingSoundAdapter: OnlyPlayingSoundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayingSoundsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onlyPlayingSoundList = UserManager.getPlayingSoundList()
        onlyPlayingSoundAdapter =
            OnlyPlayingSoundAdapter(this, onlyPlayingSoundList, changeVolumeCallback)
        binding.recyclerviewSound.adapter = onlyPlayingSoundAdapter
    }

    private val changeVolumeCallback = object : OnlyPlayingSoundAdapter.SoundVolumeChangeListener {
        override fun volumeControl(sound: Sound, volume: Float) {
            setVolume(sound)
        }
    }
}