package com.example.voicemixed.mediamixer

import android.content.Context
import java.io.File

object UtilKt {
    fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            it.outputStream().use { cache -> context.assets.open(fileName).use { it.copyTo(cache) } }
        }
}