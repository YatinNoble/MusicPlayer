package com.example.voicemixed.util

import com.example.voicemixed.soundhelper.PlayingSound

object UserManager {

    private var playingSoundList = ArrayList<PlayingSound>()

    fun setPlayingSoundList(playingSoundList: ArrayList<PlayingSound>) {
        UserManager.playingSoundList = playingSoundList
    }

    fun getPlayingSoundList(): ArrayList<PlayingSound> {
        return playingSoundList
    }
}