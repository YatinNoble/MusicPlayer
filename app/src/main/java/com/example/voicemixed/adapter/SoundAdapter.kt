package com.example.voicemixed.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemixed.R
import com.example.voicemixed.soundhelper.Sound

class SoundAdapter(
    private val context: Context,
    private val soundList: ArrayList<Sound>,
    private val soundItemClick: SoundItemClick,
    playSongList: ArrayList<String>
) :
    RecyclerView.Adapter<SoundAdapter.ViewHolder>() {

    private var selectedPositions = ArrayList(playSongList)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSoundName: TextView = itemView.findViewById(R.id.txtSoundName)
        val seekBarVolume: SeekBar = itemView.findViewById(R.id.seekBarVolume)
        val linearContainer: LinearLayout = itemView.findViewById(R.id.linearContainer)
    }

    interface SoundItemClick {
        fun soundClick(sound: Sound, isPlay: Boolean)
        fun volumeControl(sound: Sound)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_sound, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return soundList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtSoundName.text = soundList[position].soundName
        holder.seekBarVolume.progress = (soundList[position].soundVolume * 100).toInt()

        holder.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100.0f
                soundList[position].soundVolume = volume
                soundItemClick.volumeControl(soundList[position])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        holder.txtSoundName.setOnClickListener {
            val isSelected = selectedPositions.contains(soundList[position].soundName)
            if (isSelected) {
                selectedPositions.remove(soundList[position].soundName)
                holder.linearContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )
                soundItemClick.soundClick(soundList[position], false)
            } else {
                selectedPositions.add(soundList[position].soundName)
                holder.linearContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
                soundItemClick.soundClick(soundList[position], true)
            }
        }
    }
}