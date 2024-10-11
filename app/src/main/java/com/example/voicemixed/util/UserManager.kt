package com.example.voicemixed.util

import com.example.voicemixed.soundhelper.PlayingSound
import kotlin.time.Duration

object UserManager {

    private var playingSoundList = ArrayList<PlayingSound>()
    private var timerDuration = 0L

    fun setPlayingSoundList(playingSoundList: ArrayList<PlayingSound>) {
        UserManager.playingSoundList = playingSoundList
    }

    fun getPlayingSoundList(): ArrayList<PlayingSound> {
        return playingSoundList
    }

    fun setTimerDuration(timerDuration: Long) {
        this.timerDuration = timerDuration
    }

    fun getTimerDuration(): Long {
        return timerDuration
    }
}