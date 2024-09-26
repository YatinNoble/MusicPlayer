package com.example.voicemixed.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.voicemixed.databinding.ActivityMergeAudioBinding
import com.example.voicemixed.mediamixer.TrimAudioModel
import com.example.voicemixed.mediamixer.UtilKt
import java.io.File


class MergeAudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMergeAudioBinding
    private val REQUEST_CODE_PICK_AUDIO = 123
    private val RETURN_CODE_SUCCESS = 0
    private val RETURN_CODE_CANCEL = 1
    private var trimmedAudioList = ArrayList<String>()


    private val outputFilePath =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}"

    val outputMergedFile =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}/final_merged_audio.mp3"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMergeAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectAudio.setOnClickListener {

//            val selectAudioIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "audio/*"
//                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            }
//            startActivityForResult(selectAudioIntent, REQUEST_CODE_PICK_AUDIO)


            val clap = UtilKt.getFileFromAssets(this, "clap.mp3").absolutePath
            val yeehaw = UtilKt.getFileFromAssets(this, "yeehaw.mp3").absolutePath
            val hay = UtilKt.getFileFromAssets(this, "hey.mp3").absolutePath

            val lightRain = UtilKt.getFileFromAssets(this, "light_rain_109591.mp3").absolutePath
            val newsIndent = UtilKt.getFileFromAssets(this, "news_ident.mp3").absolutePath
            val sunrise = UtilKt.getFileFromAssets(this, "sunrise.mp3").absolutePath

            val audioFiles: MutableList<TrimAudioModel> = ArrayList<TrimAudioModel>()
            audioFiles.add(TrimAudioModel(lightRain, 30, 70, 1f))
            audioFiles.add(TrimAudioModel(newsIndent, 3, 15, 0.7f))
            audioFiles.add(TrimAudioModel(sunrise, 3, 12, 0.8f))
            audioFiles.add(TrimAudioModel(hay, 30, 90, 1f))


            trimFilesRecursively(audioFiles, 0, outputFilePath)
//            mergeFiles(lightRain, clap, 0, yeehaw, 0, outputMergedFile)

        }
    }

    fun mergeFiles(
        source1: String,
        effect1: String,
        effect2: String,
        destination: String
    ) {
        val file = File(destination)
        if (file.exists()) {
            file.delete()
        }

        val commands = "-i " + source1 + " -i " + effect1 + " -i " + effect2 +
                " -filter_complex " +
                "[1][2]amix=3[mixout]" +
                " -map [mixout] -c:v copy " + destination


        val cmd1 = commands.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        Log.d("Hello==>>", "cmd: $cmd1")
        val rc = FFmpeg.execute(cmd1)
        if (rc == RETURN_CODE_SUCCESS) {
            Log.d("Hello==>>", "onSuccess: $cmd1")
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.d("Hello==>>", "onCancel: $cmd1")
        } else {
            Log.d("Hello==>>", "onFailed: $cmd1 $rc ")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null && clipData.itemCount == 3) {
                val audioUri1 = clipData.getItemAt(0).uri
                val audioUri2 = clipData.getItemAt(1).uri
                val audioUri3 = clipData.getItemAt(2).uri
            }
        }
    }

    private fun trimFilesRecursively(
        audioFiles: List<TrimAudioModel>,
        currentIndex: Int,
        outputDirectory: String
    ) {
        if (currentIndex >= audioFiles.size) {
            mergeAudioFiles(trimmedAudioList, outputMergedFile)
            return
        }
        val audioFile = audioFiles[currentIndex]
        val outFilePath = outputDirectory + "/trimmed_" + (currentIndex + 1) + ".mp3"
        trimFileAllFile(
            audioFile.filePath,
            outFilePath,
            audioFile.startOffset,
            audioFile.endOffset,
            audioFile.volume
        )
        trimFilesRecursively(audioFiles, currentIndex + 1, outputDirectory)
    }

    private fun trimFileAllFile(
        sourceFilePath: String,
        outFilePath: String,
        startOffset: Int,
        endOffset: Int,
        volume: Float
    ) {
        val file = File(outFilePath)
        if (file.exists()) {
            file.delete()
        }

        val duration = endOffset - startOffset

        val commands =
            "-ss $startOffset -i $sourceFilePath -t $duration -af volume=$volume $outFilePath"

        val cmd1 = commands.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val rc = FFmpeg.execute(cmd1)

        if (rc == RETURN_CODE_SUCCESS) {
            trimmedAudioList.add(outFilePath)
            Log.d("Hello==>>", "onSuccess: $cmd1")
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.d("Hello==>>", "onCancel: $cmd1")
        } else {
            Log.d("Hello==>>", "onFailed: $cmd1 $rc ")
        }
    }


    private fun mergeAudioFiles(
        mergedAudioList: ArrayList<String>,
        destination: String
    ) {
        val file = File(destination)
        if (file.exists()) {
            file.delete()
        }
        val inputFiles = StringBuilder()
        for (trimmedFile in mergedAudioList) {
            inputFiles.append("-i ").append(trimmedFile).append(" ")
        }


        var amixFilter = ""
        for (i in 1 until mergedAudioList.size) {
            amixFilter += "[$i]"
        }

        amixFilter = "$amixFilter${"amix=${mergedAudioList.size}[mixout]"}"


        // Construct the FFmpeg command for parallel merging
        val commands =
            "$inputFiles-filter_complex $amixFilter -map [mixout] -c:v copy " + destination

        val cmd1 = commands.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        val rc = FFmpeg.execute(cmd1)

        if (rc == RETURN_CODE_SUCCESS) {
            Log.d("Hello==>>", "onSuccess: $cmd1")
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.d("Hello==>>", "onCancel: $cmd1")
        } else {
            Log.d("Hello==>>", "onFailed: $cmd1 $rc ")
        }
    }


}