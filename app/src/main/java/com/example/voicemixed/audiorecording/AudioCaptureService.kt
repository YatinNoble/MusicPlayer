package com.example.voicemixed.audiorecording


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
class AudioCaptureService : Service() {

    private lateinit var mMediaProjection: MediaProjection
    private var mResultCode: Int = 0

    //    private lateinit var mResultData: Intent
    private var mResultData: YourParcelable? = null

    private val binder = LocalBinder() // Instance of LocalBinder

    private lateinit var internalAudioRecorder: InternalAudioRecorder

    inner class LocalBinder : Binder() {
        fun getService(): AudioCaptureService =
            this@AudioCaptureService // Return the service instance
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder // Return the LocalBinder instance
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            intent?.let {
                mResultCode = it.getIntExtra("RESULT_CODE", 4) // Get any necessary data from intent
                mResultData = it.getParcelableExtra<YourParcelable>("RESULT_DATA")
                if (mResultData != null) {
                    initMediaProjection(mResultCode, mResultData)
                } else {
                    Log.d("AudioCaptureService", "RESULT_DATA is null!")
                }
            }
        } catch (e: Exception) {
            Log.d("Hello==>>", "Excep: ${e.message}")
        }

        return START_STICKY
    }


    private fun initMediaProjection(resultCode: Int, resultData: YourParcelable?) {
        try {
            mResultCode = resultCode
            mResultData = resultData!!

            // Get MediaProjectionManager
            mMediaProjection =
                (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(
                    mResultCode,
                    mResultData!!.someProperty
                )

            // Register the callback
            val handler = Handler(Looper.getMainLooper())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                mMediaProjection.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        super.onStop()
                        stopSelf()
                    }
                }, handler)
            } else {
                mMediaProjection.registerCallback(object : MediaProjection.Callback() {
                    // No-op for SDK versions below UPSIDE_DOWN_CAKE
                }, handler)
            }

            internalAudioRecorder = InternalAudioRecorder(this, mMediaProjection)
            startRecording()
        } catch (e: Exception) {
            Log.d("Hello==>>", "Exception: ${e.message}")
        }

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(11, createNotification())
    }

    private fun startRecording() {
        internalAudioRecorder.startRecording()
    }

    fun stopRecording() {
        internalAudioRecorder.stopRecording()
    }

    private fun createNotification(): Notification {
        val builder = Notification.Builder(this, "media_projection_channel")
            .setContentTitle("Screen Capture Service")
            .setContentText("Capturing screen...")
            .setSmallIcon(android.R.drawable.ic_media_play)

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "media_projection_channel",
            "Screen Capture Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaProjection.stop()
        stopForeground(true)
    }
}
