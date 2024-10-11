package com.example.voicemixed.mediamixer

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.voicemixed.databinding.ActivityMergeAudioBinding

class MergeAudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMergeAudioBinding
    private val RETURN_CODE_SUCCESS = 0
    private val RETURN_CODE_CANCEL = 1

    private val outputMergedFile =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}/final_merged_audio.mp3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMergeAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnSelectAudio.setOnClickListener {

            val dhunire = UtilKt.getFileFromAssets(this, "dhuni_re_dhakhavi.mp3").absolutePath
            val mahadev = UtilKt.getFileFromAssets(this, "mahadev_mahadev.mp3").absolutePath
            val sunraha = UtilKt.getFileFromAssets(this, "sunn_raha_hai_na_tu.mp3").absolutePath
            val tumHiHo = UtilKt.getFileFromAssets(this, "tum_hi_ho_bandhu.mp3").absolutePath

            val audioFiles = ArrayList<TrimAudioModel>()
//            audioFiles.add(TrimAudioModel(dhunire, 0, 0, 1f, 0))
//            audioFiles.add(TrimAudioModel(mahadev, 10, 0, 0.5f, 10))
//            audioFiles.add(TrimAudioModel(sunraha, 10, 100, 1f, 20))
//            audioFiles.add(TrimAudioModel(tumHiHo, 0, 60, 0.5f, 30))

//            trimAndMergeAudioFilesWithDelay(audioFiles, outputMergedFile)
        }
    }


    /*private fun trimAndMergeAudioFilesWithDelay(
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
        filterComplex.append("amix=inputs=${audioFiles.size}:normalize=0[mixout]")

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
    }*/

}