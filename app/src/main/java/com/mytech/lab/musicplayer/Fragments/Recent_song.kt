package com.mytech.lab.musicplayer.Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home

import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.IOException
import java.util.ArrayList

/**
 * Created by lnx on 1/3/18.
 */

class Recent_song : Fragment() {
//    internal var context: Context? = null
    private var helper: DatabaseHelperAdapter? = null
    private var recent_activity:TextView? = null
    private var upper_card:CardView? = null
    internal var recent: FastScrollRecyclerView? = null
    internal var song_adapter: Song_Adapter? = null



    override fun onAttach(context: Context?) {
        super.onAttach(context)
//        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {


        if(menu!=null)
        {
            menu.findItem(R.id.sort).setVisible(false)
            menu.findItem(R.id.by_duration).setVisible(false)
            menu.findItem(R.id.by_artist).setVisible(false)
            menu.findItem(R.id.by_title).setVisible(false)
            menu.findItem(R.id.by_date).setVisible(false)
            menu.findItem(R.id.by_album).setVisible(true)
        }

        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_recent_songs, container, false)

        recent = v.findViewById(R.id.recentsong_recycler)
        upper_card = v.findViewById(R.id.recent_upper_card)
        recent?.layoutManager = LinearLayoutManager(v.context)
        recent?.setHasFixedSize(true)
        recent?.setItemViewCacheSize(20)
        recent?.isDrawingCacheEnabled = true
        recent_activity = v.findViewById(R.id.no_recent_activity)
        updaterecentsong(context)


        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    fun commm(context: Context?) {
        song_adapter?.setCommnicator(object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {

                try {
                        Constants.servicearray(Constants.SONG_FROM_RECENT_SONG,album_name = null,artist_name = null,playlist_name = null,firstopen = false,cntx = context)

                        var messagearg:String = ""

                        if(Constants.SONG_FROM_RECENT_SONG.equals(Home.shared.getString(Constants.CURRENT_ALBUM,"alb"),ignoreCase = true)) {
                            messagearg = "false"

                        }
                        else {
                            messagearg = "true"
                        }

                        val actual_song_pos = Home.Songname_position.get(s.song_name)!!
                        Constants.mediaAfterprepared(null,context,s,actual_song_pos, position,"general",
                                Constants.SONG_FROM_RECENT_SONG)
                        Constants.SONG_NUMBER = position

                        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context!!)
                        if (!isServiceRunning) {

                            val i = Intent(context, SongService::class.java)
                            context.startService(i)
                        } else {
                            Constants.SONG_CHANGE_HANDLER!!.sendMessage(Constants.SONG_CHANGE_HANDLER!!.obtainMessage(0,messagearg));
                        }
//                        Home().cardview?.visibility = View.VISIBLE

                    }

                 catch (e: IOException) {
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
        setvisibility()
        if (recent_song.size != 0) {
            commm(getContext())
        }
        (activity as Home).setActionBarTitle("Recent Songs")

    }




    private fun checkpermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
            } else {

            }
        } else {

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {


        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission access", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                checkpermission()
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    companion object {
        internal lateinit var recent_song: ArrayList<Song_base>
    }

       public fun updaterecentsong(cntx: Context?) {
            val helper = DatabaseHelperAdapter(cntx!!)
            recent_song = ArrayList()
            recent_song.clear()
            recent_song = helper.getalldata_table("RecentSong")
            if (recent_song.size != 0) {
                song_adapter = Song_Adapter(recent_song, cntx)
                recent?.adapter = song_adapter
            }

        }

       public fun setvisibility()
        {
            if(recent_song.size>0)
            {
                recent_activity?.visibility = View.INVISIBLE
                upper_card?.visibility = View.VISIBLE
            }
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
                        SongAdapter_Functionality(pop,temp,position,context!!).delete( song_adapter, recent_song) }

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
