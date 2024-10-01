package com.example.voicemixed.audiorecording

import android.content.Intent

object RecordingManager {

    private var mResultCode = 0
    private lateinit var mData: Intent

    fun setResultCode(mResultCode: Int) {
        this.mResultCode = mResultCode
    }

    fun getResultCode(): Int {
        return mResultCode
    }

    fun setIntentData(mData: Intent) {
        this.mData = mData
    }

    fun getIntentData(): Intent {
        return mData
    }
}