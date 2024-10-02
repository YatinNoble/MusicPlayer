package com.example.voicemixed.activity

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.voicemixed.audiorecording.AudioCaptureService

abstract class AudioRecordingBaseActivity : AppCompatActivity() {

    protected lateinit var mediaProjectionManager: MediaProjectionManager
    protected lateinit var audioCaptureService: AudioCaptureService
    protected var serviceBound = false
    protected var isRecording = false

    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUEST_CODE_SCREEN_CAPTURE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }


    // Service connection handling
    protected val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            audioCaptureService = (service as AudioCaptureService.LocalBinder).getService()
            serviceBound = true
            onServiceBound()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }


    protected open fun onServiceBound() {
        // Override in derived classes to handle actions when the service is bound
    }


    protected fun checkPermissionsAndStartRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            startScreenCapture()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScreenCapture()
        } else {
            Toast.makeText(this, "Permissions denied. Cannot record audio.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun startScreenCapture() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE && resultCode == RESULT_OK && data != null) {
//            val resultData = YourParcelable(data)
            startAndBindService(resultCode, data)
        } else {
            Toast.makeText(this, "Screen capture denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAndBindService(resultCode: Int, resultData: Intent) {
        val serviceIntent = Intent(this, AudioCaptureService::class.java)
        serviceIntent.putExtra("RESULT_CODE", resultCode)
        serviceIntent.putExtra("RESULT_DATA", resultData)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(serviceIntent)
    }


    protected fun stopAudioCapture() {
        if (isRecording) {
            audioCaptureService.stopRecording()
            isRecording = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
        }
    }

}