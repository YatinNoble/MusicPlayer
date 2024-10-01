package com.example.voicemixed.audiorecording

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}