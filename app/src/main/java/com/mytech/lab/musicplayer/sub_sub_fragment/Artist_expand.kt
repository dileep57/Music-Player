package com.mytech.lab.musicplayer.sub_sub_fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home

import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.File

import java.io.IOException
import java.util.ArrayList

/**
 * Created by lnx on 3/3/18.
 */

class Artist_expand : Fragment() {

    private var recyclerView: FastScrollRecyclerView? = null
    private var song_adapter: Song_Adapter?=null
    internal var artist_song_array = ArrayList<Song_base>()
    internal var artist_name: String? = null
    private lateinit var cntx:Context
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        cntx = context!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.sub_sub_fragment_artist_expand, container, false)

        artist_name = arguments!!.getString("artistName")

        recyclerView = v.findViewById(R.id.recycler)
        recyclerView?.layoutManager = LinearLayoutManager(v.context)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.isDrawingCacheEnabled = true
        recyclerView?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        if(Home.artistmap.get(artist_name!!)!=null)
        {
            for (temp in Home.artistmap.get(artist_name!!)!!) {
                if(File(temp.first.url).exists())
                {
                    artist_song_array.add(temp.first)
                }

            }

            if (artist_song_array.size > 0) {
                song_adapter = Song_Adapter(artist_song_array, v.context)
                recyclerView?.adapter = song_adapter
                comm()

            }
        }
        else
        {
            val empty:TextView = v.findViewById(R.id.empty_artist)
            empty.visibility = View.VISIBLE
        }

        return  v

    }


    private fun comm() {
        song_adapter?.setCommnicator (object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {


                try {

                        Constants.servicearray(Constants.SONG_FROM_ARTIST,artist_name = s.artist)
                        var messagearg:String = ""

                        if(Constants.SONG_FROM_ARTIST.equals(Home.shared.getString(Constants.CURRENT_ALBUM,"alb"),ignoreCase = true))
                        {

                            if(Home.shared.getString(Constants.ARTIST_NAME,"alb").equals(s.artist,ignoreCase = true))
                            {
                                messagearg = "false"
                            }
                            else
                            {
                                messagearg = "true"
                            }

                        }
                        else
                        {
                            messagearg = "true"
                        }

                        val actual_song_pos = Home.artistmap[s.artist]!!.get(position).second

                        Constants.mediaAfterprepared(null,context,s,actual_song_pos, position,"general",
                                Constants.SONG_FROM_ARTIST)

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

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,cntx!!).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,cntx!!).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).addToQueue() }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).delete( song_adapter, artist_song_array) }

                    R.id.send -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).send()}

                    R.id.set_ringtone -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).setRingtone() }

                    R.id.add_to_playlistt -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).addToPlaylist(inflater1) }

                    R.id.detail -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).detail(inflater1)}

                    R.id.add_favrioute -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).addToFavrioute() }

                    R.id.search -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).search() }

                }
                return true
            }



        })

        popup.show()
    }


}
