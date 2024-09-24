package com.example.voicemixed.soundhelper

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.voicemixed.soundhelper.PlayState

object PlaybackStateUtil {
    fun sendPlaybackStateBroadcast(context: Context, playState: PlayState) {
        val intent = Intent("PlaybackStateChanged")
        intent.putExtra("state", playState.toString())
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}