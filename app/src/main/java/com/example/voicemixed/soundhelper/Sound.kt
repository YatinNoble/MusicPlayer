package com.example.voicemixed.soundhelper

data class Sound(
    val soundName: String,
    val mp3File: Int,  // Resource ID for the mp3 file in raw
    var soundVolume: Float = 1f, // Volume (between 0.0 and 1.0)
)
