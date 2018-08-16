package com.mytech.lab.musicplayer.Recyclerview_adapter

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView

import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import java.util.ArrayList

/**
 * Created by lnx on 1/3/18.
 */

class Album_song_adapter(internal var song_info: ArrayList<Song_base>, internal var context: Context) : RecyclerView.Adapter<Album_song_adapter.MyViewHolder>(), FastScrollRecyclerView.SectionedAdapter {
    internal var inflater: LayoutInflater

    internal var communicator: Communicator? = null

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getSectionName(position: Int): String {
        return song_info[position].song_name.substring(0, 1)
    }


    interface Communicator {
        fun clickonplaybutton(v: View, s: Song_base, position: Int)
    }

    fun setCommnicator(c: Communicator) {
        this.communicator = c
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = inflater.inflate(R.layout.recycler_album_player_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val song = song_info[position]
        holder.song_name.text = song.song_name
        holder.artist.text = song.artist
        holder.duration.text = Constants.calculatetime(Integer.parseInt(song.duration))
        holder.count.text = Integer.toString(position + 1)
        holder.relativeLayout.setOnClickListener { view ->

                communicator?.clickonplaybutton(view, song, position)

        }

    }

    override fun getItemCount(): Int {
        return song_info.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var song_name: TextView
        var artist: TextView
        var duration: TextView
        var count: TextView
        var cardView: CardView
        var play: Button? = null
        var relativeLayout: RelativeLayout

        init {
            song_name = itemView.findViewById(R.id.song_name)
            artist = itemView.findViewById(R.id.artist)
            cardView = itemView.findViewById(R.id.cardview)
            count = itemView.findViewById(R.id.count_number)
            relativeLayout = itemView.findViewById(R.id.cardrelative)
            duration = itemView.findViewById(R.id.duration)
        }
    }
}
