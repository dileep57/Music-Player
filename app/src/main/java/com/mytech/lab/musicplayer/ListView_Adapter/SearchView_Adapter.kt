package com.mytech.lab.musicplayer.ListView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.utils.Song_base

class SearchView_Adapter(context: Context, song_info: ArrayList<String>) : ArrayAdapter<String>(context, R.layout.recycler_listview_searchview, song_info) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val inflater = LayoutInflater.from(context)

        val v = inflater.inflate(R.layout.recycler_listview_searchview, parent, false)

        val stringname:String = getItem(position)

        val pos:Int = Home.Songname_position.get(stringname)!!

        val s: Song_base = Home.all_songs.get(pos)

        val name = v.findViewById<TextView>(R.id.songname)
        name.text = s.song_name

        val artist = v.findViewById<TextView>(R.id.artist)
        artist.text = s.artist


        return v
    }
}