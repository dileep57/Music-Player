package com.mytech.lab.musicplayer.Sub_Fragments

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.*
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home

import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.sub_sub_fragment.Playlist_single
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.File

import java.io.IOException
import kotlin.collections.ArrayList


/**
 * Created by lnx on 26/2/18.
 */

class Songs : Fragment() {


    internal var handler = Handler()
    internal lateinit var recyclerView: FastScrollRecyclerView
    var mediaPlayer: MediaPlayer? = null


    lateinit var cntx: Context

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.cntx = context!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
         super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val edit = Home.shared.edit()
        edit.putString("current_album","mixed").apply()

        when(item!!.itemId)
        {
            R.id.by_default ->
            {
                Home.fetchallsong(cntx,MediaStore.Audio.Media.TITLE)
                adapter?.notifyDataSetChanged()
            }

            R.id.by_title ->
            {
                Home.fetchallsong(cntx,MediaStore.Audio.Media.TITLE)
                adapter?.notifyDataSetChanged()
            }

            R.id.by_album ->
            {
                Home.fetchallsong(cntx,MediaStore.Audio.Media.ALBUM)
                adapter?.notifyDataSetChanged()
            }

            R.id.by_artist ->
            {
                Home.fetchallsong(cntx,MediaStore.Audio.Media.ARTIST)
                adapter?.notifyDataSetChanged()
            }

            R.id.by_date ->
            {
                Home.fetchallsong(cntx,MediaStore.Audio.Media.DATE_ADDED)
                adapter?.notifyDataSetChanged()
            }

            R.id.by_duration ->
            {
                Home.fetchallsong(cntx,MediaStore.Audio.Media.DURATION)
                adapter?.notifyDataSetChanged()
            }

        }

        return super.onOptionsItemSelected(item)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.sub_fragment_song, container, false)

        recyclerView = v.findViewById(R.id.sub_fragment_song_recycler)
        recyclerView.layoutManager = LinearLayoutManager(v.context)
        val ob = Backgroundtask()
        ob.execute()
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    private fun commm(context: Context?) {
        adapter?.setCommnicator (object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {

                try {
                        Constants.servicearray(Constants.SONG_FROM_ONLY_SONG)
                        var messagearg:String = ""
                        if(Constants.SONG_FROM_ONLY_SONG.equals(Home.shared.getString(Constants.CURRENT_ALBUM,"alb"),ignoreCase = true))
                        {
                            messagearg = "false"
                        }
                        else
                        {
                            messagearg = "true"
                        }

                        Constants.mediaAfterprepared(mediaPlayer,context,s,position, position,
                                "general", Constants.SONG_FROM_ONLY_SONG)

                    Constants.SONG_NUMBER = position
                    val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)

                        if (!isServiceRunning)
                        {
                            val i = Intent(cntx, SongService::class.java)
                            //cntx.startService(i)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                cntx.startForegroundService(i);
                            } else {
                                cntx.startService(i);
                            }

                        } else {

                            val msg = Message.obtain()
                            msg.obj = messagearg
                            Constants.SONG_CHANGE_HANDLER?.sendMessage(msg)

                        }

                    Home().cardview?.visibility = View.VISIBLE

                }
                 catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })

        adapter?.setclick(object : Song_Adapter.Menuclick{

            override fun clickonmenu(v: LinearLayout, s: Song_base, position: Int) {
                 playwithpopmenu(v,s,position)
            }
        })
    }

    inner class Backgroundtask : AsyncTask<Void, Void, Boolean>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            try {
                //                loadsong(cntxt);
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
            adapter = Song_Adapter(Home.all_songs, cntx)
            recyclerView.adapter = adapter
            commm(cntx)

        }
    }

    private fun checkpermission(context: Context?) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
            } else {

            }
        } else {
            //            loadsong(context);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                    loadsong(cntxt);
            } else {
                Toast.makeText(cntx, "Permission Denied", Toast.LENGTH_SHORT).show()
                checkpermission(cntx)
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }


    }

    companion object {
        var adapter: Song_Adapter? = null
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }



    fun playwithpopmenu(pop: LinearLayout, temp: Song_base, position: Int)
    {

        val popup = PopupMenu(cntx, pop)
        popup.inflate(R.menu.pop_menu_song)

        var inflater1:LayoutInflater? = null
        inflater1 = LayoutInflater.from(cntx)

        if(Build.VERSION.SDK_INT>=23)
        {
            popup.gravity = Gravity.END
        }

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.getItemId()) {

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,cntx).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,cntx).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).addToQueue() }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).delete( adapter, Home.all_songs) }

                    R.id.send -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).send()}

                    R.id.set_ringtone -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).setRingtone() }

                    R.id.add_to_playlistt -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).addToPlaylist(inflater1) }

                    R.id.detail -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).detail(inflater1)}

                    R.id.add_favrioute -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).addToFavrioute() }

                    R.id.search -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).search() }

                }
                return true
            }



        })

        popup.show()
    }



}
