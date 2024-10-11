package com.example.voicemixed.audiorecording

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.voicemixed.R
import com.example.voicemixed.databinding.ActivityRecordingSaveBinding
import com.example.voicemixed.mediamixer.UtilKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class RecordingSaveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordingSaveBinding
    private lateinit var audioFileAdapter: AudioFileAdapter
    private val RETURN_CODE_SUCCESS = 0
    private val RETURN_CODE_CANCEL = 1

    private val outputMergedFile =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}/final_merged_audio.wav"

    private val initAudioList = ArrayList<AudioData>()

    //    val recordingSoundPlayer = RecordingSoundPlayer(this)
    val playMultipleAudio = PlayMultipleAudio(this)


    private var seconds = 0f
    private var isTimerRunning = false
    private var job: Job? = null

    private var clickCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartRecording.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
                getAllRecordingData()
            } else {
                playMultipleAudio.clearAllDataThenStart()
                startTimer()
//                recordingSoundPlayer.stopAllSounds()
            }
        }

        makeSoundList()
        setListData()
    }

    private fun startTimer() {
        seconds = 0f
        clickCount = 0
        binding.txtTime.text = seconds.toString()
        if (!isTimerRunning) {
            isTimerRunning = true
            binding.btnStartRecording.text = "Stop Recording"
            // Start the coroutine timer
            job = CoroutineScope(Dispatchers.Main).launch {
                while (isTimerRunning) {
                    delay(1000) // Wait for 1 second
                    seconds++
                    binding.txtTime.text = seconds.toString() // Update UI
                }
            }
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            binding.btnStartRecording.text = "Start Recording"
            job?.cancel() // Cancel the coroutine job
        }
    }

    private fun makeSoundList() {
        initAudioList.add(AudioData("dubstep_club_01.wav", R.raw.dubstep_club_01))
        initAudioList.add(AudioData("dubstep_club_02.wav", R.raw.dubstep_club_02))
        initAudioList.add(AudioData("dubstep_club_03.wav", R.raw.dubstep_club_03))
        initAudioList.add(AudioData("dubstep_club_04.wav", R.raw.dubstep_club_04))
        initAudioList.add(AudioData("dubstep_club_05.wav", R.raw.dubstep_club_05))
        initAudioList.add(AudioData("dubstep_club_06.wav", R.raw.dubstep_club_06))
        initAudioList.add(AudioData("dubstep_club_07.wav", R.raw.dubstep_club_07))
        initAudioList.add(AudioData("dubstep_club_08.wav", R.raw.dubstep_club_08))
        initAudioList.add(AudioData("dubstep_club_09.wav", R.raw.dubstep_club_09))
        initAudioList.add(AudioData("dubstep_club_10.wav", R.raw.dubstep_club_10))
        initAudioList.add(AudioData("dubstep_club_11.wav", R.raw.dubstep_club_11))
    }

    private fun setListData() {
        audioFileAdapter = AudioFileAdapter(this, initAudioList, soundClick)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = audioFileAdapter
    }


    private val soundClick = object : AudioFileAdapter.SoundItem {
        override fun soundClick(audioData: AudioData) {
//            recordingSoundPlayer.play(soundName, seconds)
            clickCount++
            Log.d("Hello==>>", "clickCount: $clickCount")
            val filePathSongs = UtilKt.getFileFromAssets(
                this@RecordingSaveActivity,
                audioData.songsName
            ).absolutePath
            playMultipleAudio.playSongs(
                AudioData(
                    audioData.songsName,
                    audioData.songsId,
                    seconds,
                    songsFilepath = filePathSongs
                )
            )
        }
    }

    private fun getAllRecordingData() {
        playMultipleAudio.stopAll()
        val list = playMultipleAudio.getAllCompletedSoundsData()
        Log.d("Hello==>>", "listSize: ${list.size}")
        /*for (i in list.indices) {
            Log.d(
                "Hello==>>",
                "list: $i : ${list[i].songsName} : ${list[i].delayOffset} : ${list[i].endDuration}"
            )
        }*/
//        trimAndMergeAudioFilesWithDelay(list, outputMergedFile)
    }


    override fun onDestroy() {
        super.onDestroy()
        stopTimer() // Stop timer if activity is destroyed
    }

    /* private fun makeSoundList() {
         initAudioList.add("raw/dubstep_club_01.wav")
         initAudioList.add("raw/dubstep_club_02.wav")
         initAudioList.add("raw/dubstep_club_03.wav")
         initAudioList.add("raw/dubstep_club_04.wav")
         initAudioList.add("raw/dubstep_club_05.wav")
         initAudioList.add("raw/dubstep_club_06.wav")
         initAudioList.add("raw/dubstep_club_07.wav")
         initAudioList.add("raw/dubstep_club_08.wav")
         initAudioList.add("raw/dubstep_club_09.wav")
         initAudioList.add("raw/dubstep_club_10.wav")
         initAudioList.add("raw/dubstep_club_11.wav")
     }*/


    /*private fun trimAndMergeAudioFilesWithDelay(
        audioFiles: ArrayList<AudioData>,
        outFilePath: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
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
                inputFiles.append("-i ").append(audio.songsFilepath).append(" ")

                filterComplex
                    .append("[$i]atrim=start=${0}:end=${audio.endDuration / 1000}")
                    .append(",adelay=${audio.delayOffset * 1000}|${audio.delayOffset * 1000}")
                    .append("-af,volume=${10}")
                    .append("[a$i];")
            }

            for (i in audioFiles.indices) {
                filterComplex.append("[a$i]")
            }

            filterComplex.append("amix=inputs=${audioFiles.size},dynaudnorm[mixout]")
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

    }*/


    fun copyRawToInternalStorage(rawResourceId: Int, fileName: String): File {
        val inputStream = resources.openRawResource(rawResourceId)
        val outputFile = File(filesDir, fileName)

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile
    }


}
