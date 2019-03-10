package com.mytech.lab.musicplayer.Sub_Fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.LinearLayout
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home

import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.File

import java.io.IOException
import kotlin.collections.ArrayList

/**
 * Created by lnx on 4/3/18.
 */

class Mini_track : Fragment() {

    internal lateinit var recyclerView: com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
    internal  var song_adapter: Song_Adapter? = null
    internal var context: Context? = null
    lateinit var temperary_array:ArrayList<Song_base>

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId)
        {

            R.id.by_title ->
            {


            }


        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.sub_fragment_mini_track, container, false)

        recyclerView = v.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(v.context)

        temperary_array = ArrayList<Song_base>()
        for (s in Home.mini_track) {
            if(File(s.first.url).exists()) {
                temperary_array.add(s.first)
            }
        }
        song_adapter = Song_Adapter(temperary_array, v.context)
        recyclerView.adapter = song_adapter
        comm()
        return v
    }

    private fun comm() {
        song_adapter?.setCommnicator( object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {

                try {
//
                    Constants.servicearray(Constants.SONG_FROM_MINI_TRACK)

                    var messagearg:String = ""
                    if(Constants.SONG_FROM_MINI_TRACK.equals(Home.shared.getString(Constants.CURRENT_ALBUM,"alb"),ignoreCase = true)) {
                        messagearg = "false"

                    }
                    else {
                        messagearg = "true"

                    }

                    val actual_song_position = Home.mini_track[position].second
                    Constants.setsharedpreference(s,
                            "general",
                            actual_song_position, position,
                            Home.shared.getBoolean(Constants.SHUFFLE,false),
                            Home.shared.getBoolean(Constants.REPEAT,false),
                            Constants.SONG_FROM_MINI_TRACK
                    )

                    Constants.SONG_NUMBER = position
                    val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context!!)
                    if (!isServiceRunning) {
                        val i = Intent(context, SongService::class.java)
                        context!!.startService(i)
                    } else {

                        Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,messagearg));
                    }

                    Home().cardview?.visibility = View.VISIBLE


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        })

        song_adapter?.setclick(object : Song_Adapter.Menuclick{

            override fun clickonmenu(v: LinearLayout, s: Song_base, position: Int) {
                playwithpopmenu(v,s,position)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        song_adapter?.notifyDataSetChanged()
    }

    fun playwithpopmenu(pop: LinearLayout, temp: Song_base, position: Int)
    {

        val popup = PopupMenu(context!!, pop)
        popup.inflate(R.menu.pop_menu_song)

        var inflater1:LayoutInflater? = null
        inflater1 = LayoutInflater.from(context)

        if(Build.VERSION.SDK_INT>=23)
        {
            popup.gravity = Gravity.END
        }

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.getItemId()){

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,context!!).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,context!!).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).addToQueue() }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).delete( song_adapter, temperary_array) }

                    R.id.send -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).send()}

                    R.id.set_ringtone -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).setRingtone() }

                    R.id.add_to_playlistt -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).addToPlaylist(inflater1) }

                    R.id.detail -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).detail(inflater1)}

                    R.id.add_favrioute -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).addToFavrioute() }

                    R.id.search -> {
                        SongAdapter_Functionality(pop,temp,position,context!!).search() }

                }
                return true
            }



        })

        popup.show()
    }


}
