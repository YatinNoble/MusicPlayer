package com.example.voicemixed.activity

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.voicemixed.R
import com.example.voicemixed.adapter.SoundAdapter
import com.example.voicemixed.audiorecording.AudioCaptureService
import com.example.voicemixed.databinding.ActivityMainBinding
import com.example.voicemixed.soundhelper.PlayState
import com.example.voicemixed.soundhelper.Sound
import com.example.voicemixed.util.UserManager


class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var soundAdapter: SoundAdapter
    private var soundList = ArrayList<Sound>()
    private var buttonState = ""

    companion object {
        var playAnySongs = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createSoundList()
        showSoundListData()

        // Register the receiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(playbackStateReceiver, IntentFilter("PlaybackStateChanged"))

        binding.imvPlayAllIcon.setOnClickListener {
            if (musicService.getCurrentPlaying().size > 0) {
                if (buttonState == PlayState.PLAY.toString()) {
                    musicService.playAllSounds()
                } else if (buttonState == PlayState.PAUSE.toString()) {
                    musicService.pauseAllSounds()
                }
            }
        }

        binding.btnOnlyPlayingSound.setOnClickListener {
            UserManager.setPlayingSoundList(musicService.getCurrentPlaying())
            startActivity(Intent(this@MainActivity, PlayingSoundsActivity::class.java))
        }


        binding.txtSoundName.setOnClickListener {

        }

        val isRun = isServiceRunning(AudioCaptureService::class.java)
        Log.d("Hello==>>", "isRunOrnOt: $isRun")
    }


    private fun createSoundList() {
        soundList.add(Sound("Air Raid Siren", R.raw.air_raid_siren_sound_effect))
        soundList.add(Sound("Cinematic", R.raw.cinematic_intro_6097))
        soundList.add(Sound("Dark Future", R.raw.dark_future_logo))
        soundList.add(Sound("Epic Logo", R.raw.epic_logo))
        soundList.add(Sound("Intro Piano", R.raw.intro_piano_loop))
        soundList.add(Sound("Light Rain", R.raw.light_rain_109591))
        soundList.add(Sound("SunRise", R.raw.sunrise))
        soundList.add(Sound("Upbeat Pop", R.raw.upbeat_pop_intro_logo))
        soundList.add(Sound("Wind Blowing", R.raw.wind_blowing_sfx_12809))
    }

    private fun showSoundListData() {
        soundAdapter = SoundAdapter(this, soundList, soundCallBack, ArrayList())
        binding.recyclerviewSound.adapter = soundAdapter
    }

    private val soundCallBack = object : SoundAdapter.SoundItemClick {
        override fun soundClick(sound: Sound, isPlay: Boolean) {
            if (isPlay) {
                playMusic(sound, R.drawable.pause_icon)
            } else {
                pauseMusic(sound)
            }
        }

        override fun volumeControl(sound: Sound) {
            setVolume(sound)
        }
    }


    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("state")
            updateUI(state)
        }
    }

    private fun updateUI(state: String?) {
        buttonState = state ?: ""
        if (state == PlayState.PLAY.toString()) {
            binding.imvPlayAllIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.play_icon
                )
            )
            playAnySongs = true
        } else if (state == PlayState.PAUSE.toString()) {
            binding.imvPlayAllIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pause_icon
                )
            )
            playAnySongs = false
        }
    }


    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}