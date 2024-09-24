package com.example.voicemixed.activity

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.example.voicemixed.soundhelper.MusicPlayerService
import com.example.voicemixed.soundhelper.Sound

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var musicService: MusicPlayerService
    private var isBound = false

    companion object {
        var activityCount = 0
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        activityCount++
        val intent = Intent(this, MusicPlayerService::class.java)
        if (!isServiceRunning(MusicPlayerService::class.java)) {
            startService(intent)  // Start the service if it isn't running
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }


    override fun onStop() {
        super.onStop()
        activityCount--
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            if (activityCount == 0) {
                musicService.onDestroy()
            }
            isBound = false
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // Methods to interact with the service
    protected fun playMusic(sound: Sound, iconRes: Int) {
        if (isBound) {
            musicService.playMusic(sound, iconRes)
        }
    }

    protected fun pauseMusic(sound: Sound) {
        if (isBound) {
            musicService.pauseMusic(sound)
        }
    }

    protected fun setVolume(sound: Sound) {
        if (isBound) {
            musicService.setVolume(sound)
        }
    }
}