package com.example.voicemixed.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.voicemixed.soundhelper.MusicPlayerService
import com.example.voicemixed.soundhelper.PlayState

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val action = intent?.action

            // Create an intent to start the service
            val serviceIntent = Intent(context, MusicPlayerService::class.java)

            when (action) {
                PlayState.PLAY.toString() -> {
                    serviceIntent.action = PlayState.PLAY.toString()
                    context.startService(serviceIntent)
                }

                PlayState.PAUSE.toString() -> {
                    serviceIntent.action = PlayState.PAUSE.toString()
                    context.startService(serviceIntent)
                }

                PlayState.ACTION_NOTIFICATION_REMOVED.toString(), PlayState.SET_TIMER.toString() -> {
                    val stopServiceIntent = Intent(context, MusicPlayerService::class.java)
                    context.stopService(stopServiceIntent) // Stop the service if desired
                }

                else -> {
                    Log.d("Hello==>>", "this is else")
                }
            }
        }
    }
}


class NotificationWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val stopServiceIntent = Intent(appContext, MusicPlayerService::class.java)
        appContext.stopService(stopServiceIntent) // Stop the service if desired
        return Result.success()
    }
}


