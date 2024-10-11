package com.example.voicemixed.audiorecording

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemixed.R

class AudioFileAdapter(
    val context: Context,
    private val soundList: ArrayList<AudioData>,
    private val soundItem: SoundItem
) :
    RecyclerView.Adapter<AudioFileAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSoundName: TextView = itemView.findViewById(R.id.txtSoundName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.audio_file_adpater_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtSoundName.text = "${position + 1}"

        holder.itemView.setOnClickListener {
            soundItem.soundClick(soundList[position])
        }
    }

    override fun getItemCount(): Int {
        return soundList.size
    }

    interface SoundItem {
        fun soundClick(audioData: AudioData)
    }
}