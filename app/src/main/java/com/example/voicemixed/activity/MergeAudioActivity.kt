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
    private val REQUEST_CODE_CAPTURE = 111
    private val RETURN_CODE_SUCCESS = 0
    private val RETURN_CODE_CANCEL = 1
    private var trimmedAudioList = ArrayList<String>()

    private val outputFilePath =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}"

    private val outputMergedFile =
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

            val cinematicIntro =
                UtilKt.getFileFromAssets(this, "cinematic_intro_6097.mp3").absolutePath
            val newsIndent = UtilKt.getFileFromAssets(this, "news_ident.mp3").absolutePath
            val sunrise = UtilKt.getFileFromAssets(this, "sunrise.mp3").absolutePath
            val upbeat = UtilKt.getFileFromAssets(this, "upbeat_pop_intro_logo.mp3").absolutePath

            val dhunire = UtilKt.getFileFromAssets(this, "dhuni_re_dhakhavi.mp3").absolutePath
            val mahadev = UtilKt.getFileFromAssets(this, "mahadev_mahadev.mp3").absolutePath
            val sunraha = UtilKt.getFileFromAssets(this, "sunn_raha_hai_na_tu.mp3").absolutePath
            val tumHiHo = UtilKt.getFileFromAssets(this, "tum_hi_ho_bandhu.mp3").absolutePath

            val audioFiles = ArrayList<TrimAudioModel>()
            audioFiles.add(TrimAudioModel(dhunire, 0, 0, 1f, 0))
            audioFiles.add(TrimAudioModel(mahadev, 10, 0, 0.5f, 10))
            audioFiles.add(TrimAudioModel(sunraha, 10, 100, 1f, 20))
            audioFiles.add(TrimAudioModel(tumHiHo, 0, 60, 0.5f, 30))

//            mergeFiles(dhunire, sunraha, 100000, tumHiHo, 20000, outputMergedFile)
//            trimFile(lightRain, outputMergedFile, 20, 80)
//            trimAndMergeAudioFiles(audioFiles, outputMergedFile)
            trimAndMergeAudioFilesWithDelay(audioFiles, outputMergedFile)
//            concatenateAudioFiles(audioFiles, outputMergedFile)
//            trimFilesRecursively(audioFiles, 0, outputFilePath)
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
        } else if (requestCode == REQUEST_CODE_CAPTURE && resultCode == RESULT_OK) {

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


    // working demo se
    fun trimAndMergeAudioFiles(
        audioFiles: List<TrimAudioModel>,
        outFilePath: String
    ) {
        // Delete output file if it already exists
        val file = File(outFilePath)
        if (file.exists()) {
            file.delete()
        }

        // Initialize FFmpeg command with input files
        val inputFiles = StringBuilder()
        val filterComplex = StringBuilder()

        // Loop through the audio files to prepare the inputs and filters
        for (i in audioFiles.indices) {
            val audio = audioFiles[i]
            inputFiles.append("-i ").append(audio.filePath).append(" ")

            // Generate atrim filter for each file
            filterComplex.append("[$i]atrim=start=")
                .append(audio.startOffset)
                .append(":end=")
                .append(audio.endOffset)
                .append("[a")
                .append(i)
                .append("];")
        }

        // Concatenate all trimmed streams into a final amix filter
        for (i in audioFiles.indices) {
            filterComplex.append("[a").append(i).append("]")
        }
        filterComplex.append("amix=inputs=").append(audioFiles.size).append("[mixout]")

        // Construct the full FFmpeg command
        val commands =
            "$inputFiles-filter_complex $filterComplex -map [mixout] -c:v copy $outFilePath"

        // Split the command into an array for FFmpeg
        val cmd1 = commands.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Execute the FFmpeg command
        val rc = FFmpeg.execute(cmd1)

        if (rc == RETURN_CODE_SUCCESS) {
            Log.d("Hello==>>", "onSuccess: $cmd1")
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.d("Hello==>>", "onCancel: $cmd1")
        } else {
            Log.d("Hello==>>", "onFailed: $cmd1 $rc")
        }
    }


    private fun trimAndMergeAudioFilesWithDelay(
        audioFiles: List<TrimAudioModel>,
        outFilePath: String
    ) {
        // Delete output file if it already exists
        val file = File(outFilePath)
        if (file.exists()) {
            file.delete()
        }

        val inputFiles = StringBuilder()
        val filterComplex = StringBuilder()

        // Loop through the audio files to prepare the inputs and filters
        for (i in audioFiles.indices) {
            val audio = audioFiles[i]
            inputFiles.append("-i ").append(audio.filePath).append(" ")
            val effectiveEndOffset = if (audio.endOffset > 0) audio.endOffset else Integer.MAX_VALUE

            filterComplex
                .append("[$i]atrim=start=${audio.startOffset}:end=$effectiveEndOffset")
                .append(",adelay=${audio.delayOffsets * 1000}|${audio.delayOffsets * 1000}")
                .append(",volume=${audio.volume}")
                .append("[a$i];")
        }

        for (i in audioFiles.indices) {
            filterComplex.append("[a$i]")
        }
        filterComplex.append("amix=inputs=${audioFiles.size}[mixout]")

        val commands =
            "$inputFiles-filter_complex $filterComplex -map [mixout] -c:v copy $outFilePath"

        val cmd1 = commands.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val rc = FFmpeg.execute(cmd1)

        if (rc == RETURN_CODE_SUCCESS) {
            Log.d("Hello==>>", "onSuccess: $cmd1")
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.d("Hello==>>", "onCancel: $cmd1")
        } else {
            Log.d("Hello==>>", "onFailed: $cmd1 $rc")
        }
    }

    private fun concatenateAudioFiles(
        audioFiles: List<TrimAudioModel>, // List of audio file paths
        outputFilePath: String // Output file path
    ) {
        // Delete output file if it already exists
        val outputFile = File(outputFilePath)
        if (outputFile.exists()) {
            outputFile.delete()
        }

        // Create a temporary file list for concatenation
        val fileList = File.createTempFile("audio_files_list", ".txt")

        // Write audio file paths to the list file
        fileList.bufferedWriter().use { writer ->
            audioFiles.forEach { audioFile ->
                writer.write("file '${audioFile.filePath}'\n") // Use single quotes for file paths
            }
        }

        // Construct the FFmpeg command to concatenate the files
        val command = "-f concat -safe 0 -i ${fileList.absolutePath} -c copy $outputFilePath"

        // Split the command into an array for FFmpeg
        val cmdArray = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Execute the FFmpeg command
        val rc = FFmpeg.execute(cmdArray)

        // Clean up the temporary list file
        fileList.delete()


        if (rc == RETURN_CODE_SUCCESS) {
            Log.d("Hello==>>", "onSuccess: $cmdArray")
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.d("Hello==>>", "onCancel: $cmdArray")
        } else {
            Log.d("Hello==>>", "onFailed: $cmdArray $rc ")
        }
    }

    fun mergeFiles(
        source1: String,
        effect1: String,
        startOffset: Int,
        effect2: String,
        startOffset2: Int,
        destination: String?
    ) {
        val destination02 =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .toString() + "/well.mp3"
        val file = File(destination02)
        if (file.exists()) {
            file.delete()
        }
        val commands = "-i " + source1 + " -i " + effect1 + " -i " + effect2 +
                " -filter_complex [1]adelay=" + startOffset + "|" + startOffset + "[s1];" +
                "[2]adelay=" + startOffset2 + "|" + startOffset2 + "[s2];" +
                "[0][s1][s2]amix=3[mixout]" +
                " -map [mixout] -c:v copy " + destination02
        Log.d("Hello==>>", "commands: $commands")
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


    fun trimFile(sourceFilePath: String, outFilePath: String, startOffset: Int, endOffset: Int) {
        val file = File(outFilePath)
        if (file.exists()) {
            file.delete()
        }

        val commands =
            "-i $sourceFilePath -ss $startOffset -to $endOffset -c copy $outFilePath"
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