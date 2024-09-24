package com.example.voicemixed.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemixed.soundhelper.PlayingSound
import com.example.voicemixed.R
import com.example.voicemixed.soundhelper.Sound

class OnlyPlayingSoundAdapter(
    private val context: Activity,
    private val playingSoundList: ArrayList<PlayingSound>,
    private val volumeChangeListener: SoundVolumeChangeListener
) :
    RecyclerView.Adapter<OnlyPlayingSoundAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSoundName: TextView = itemView.findViewById(R.id.txtSoundName)
        val seekBarVolume: SeekBar = itemView.findViewById(R.id.seekBarVolume)
    }

    interface SoundVolumeChangeListener {
        fun volumeControl(sound: Sound, volume: Float)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_playing_sound, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playingSoundList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val soundList = playingSoundList[position]

        holder.txtSoundName.text = soundList.sound.soundName
        holder.seekBarVolume.progress = (soundList.sound.soundVolume * 100).toInt()
        holder.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100.0f
                soundList.sound.soundVolume = volume
                volumeChangeListener.volumeControl(soundList.sound, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


}