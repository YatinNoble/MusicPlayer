package com.example.voicemixed.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.voicemixed.R
import com.example.voicemixed.adapter.SoundAdapter
import com.example.voicemixed.audiorecording.AudioCaptureService
import com.example.voicemixed.audiorecording.YourParcelable
import com.example.voicemixed.databinding.ActivityMainBinding
import com.example.voicemixed.soundhelper.PlayState
import com.example.voicemixed.soundhelper.Sound


@RequiresApi(Build.VERSION_CODES.Q)

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var soundAdapter: SoundAdapter
    private var soundList = ArrayList<Sound>()
    private var buttonState = ""

    companion object {
        var playAnySongs = false
    }

    private lateinit var mediaProjectionService: AudioCaptureService
    private var serviceBound = false
    private val REQUEST_CODE_SCREEN_CAPTURE = 1000
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val REQUEST_CODE_PERMISSIONS = 1001
    private var isRecording = false

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
//            UserManager.setPlayingSoundList(musicService.getCurrentPlaying())
//            startActivity(Intent(this@MainActivity, PlayingSoundsActivity::class.java))
            startActivity(Intent(this@MainActivity, RecordingSaveActivity::class.java))
        }

        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        binding.txtSoundName.setOnClickListener {
            onRecordButtonClick()
        }
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

    private fun onRecordButtonClick() {
        if (isRecording) {
            stopAudioCapture()
        } else {
            checkPermissionsAndStartRecording() // Check permissions before starting
        }
    }

    private fun startScreenCapture() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_CODE_SCREEN_CAPTURE)
    }

    private fun startAudioCapture() {
        if (!isRecording) {
            startScreenCapture()
            isRecording = true
            binding.txtSoundName.text = "Stop Recording"
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun stopAudioCapture() {
        if (isRecording) {
            mediaProjectionService.stopRecording()
            isRecording = false
            binding.txtSoundName.text = "Start Recording"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                val resultData = YourParcelable(data)
                // Start the media projection service
                val serviceIntent = Intent(this, AudioCaptureService::class.java)
                serviceIntent.putExtra("RESULT_CODE", resultCode) // Make sure this is a Parcelable
                serviceIntent.putExtra("RESULT_DATA", resultData) // Make sure this is a Parcelable
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                startService(serviceIntent)
            } else {
                Log.e("ScreenCapture", "User denied screen capture")
            }
        }
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Get the service instance
            mediaProjectionService = (service as AudioCaptureService.LocalBinder).getService()
            // Initialize media projection
            serviceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }

    private fun checkPermissionsAndStartRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            // Permissions are granted, start audio capture
            startAudioCapture()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startAudioCapture()
            } else {
                // Permissions denied, handle appropriately
                Toast.makeText(this, "Permissions denied. Cannot record audio.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}