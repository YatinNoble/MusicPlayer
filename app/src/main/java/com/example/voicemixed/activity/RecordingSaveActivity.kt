package com.example.voicemixed.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.voicemixed.audiorecording.AudioCaptureService
import com.example.voicemixed.audiorecording.YourParcelable
import com.example.voicemixed.databinding.ActivityRecordingSaveBinding
import java.io.File

class RecordingSaveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordingSaveBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaPlayer02: MediaPlayer

    private val REQUEST_CODE_PERMISSIONS = 1001
    private var isRecording = false

    private var recordingFile =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "recording.mp3"
        )


    private lateinit var mediaProjectionService: AudioCaptureService
    private var serviceBound = false
    private val REQUEST_CODE_SCREEN_CAPTURE = 1000
    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer()
        mediaPlayer02 = MediaPlayer()
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        binding.startRecording.setOnClickListener {
            onRecordButtonClick()
        }

        binding.playSongs.setOnClickListener {
            playAudio("upbeat_pop_intro_logo.mp3") // Replace with your actual file name
        }

        binding.stopSong.setOnClickListener {
            stopAudio()
        }

        binding.playSongs02.setOnClickListener {
            playAudio02("tum_hi_ho_bandhu.mp3") // Replace with your actual file name
        }

        binding.stopSong02.setOnClickListener {
            stopAudio02()
        }

        binding.btnNavigate.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun playAudio(fileName: String) {
        // Release any previously initialized media player
        mediaPlayer.reset()

        // Set data source from assets
        val assetFileDescriptor = assets.openFd(fileName)
        mediaPlayer.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )

        // Prepare and start playing
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private fun playAudio02(fileName: String) {
        // Release any previously initialized media player
        mediaPlayer02.reset()

        // Set data source from assets
        val assetFileDescriptor = assets.openFd(fileName)
        mediaPlayer02.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )

        // Prepare and start playing
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
//        if (serviceBound) {
//            unbindService(serviceConnection)
//            serviceBound = false
//        }
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

    private fun startScreenCapture() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_CODE_SCREEN_CAPTURE)
    }

    private fun startAudioCapture() {
        if (!isRecording) {
            startScreenCapture()
            isRecording = true
            binding.startRecording.text = "Stop Recording"
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun stopAudioCapture() {
        if (isRecording) {
            mediaProjectionService.stopRecording()
            isRecording = false
            binding.startRecording.text = "Start Recording"
        }
    }


    private fun onRecordButtonClick() {
        if (isRecording) {
            stopAudioCapture()
        } else {
            checkPermissionsAndStartRecording() // Check permissions before starting
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

}
