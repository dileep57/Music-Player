package com.mytech.lab.musicplayer.Activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.SongService
import com.mytech.lab.musicplayer.utils.Song_base
import com.mytech.lab.musicplayer.R
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import java.io.IOException


class Song_library_Act : AppCompatActivity() {

    internal var handler = Handler()
    internal lateinit var recyclerView: FastScrollRecyclerView
    internal lateinit var adapter: Song_Adapter

    internal lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        if(Wel.colorshared.getInt(Constants.THEMENAME,-1)!=-1)
        {

            setTheme(Wel.colorshared.getInt(Constants.THEMENAME, R.style.AppFullScreenTheme))
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_library)


        toolbar = findViewById(R.id.toolbar_player2)
        toolbar.title = "Music Library"
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setItemViewCacheSize(20);
        recyclerView.addItemDecoration(DividerItemDecoration(this, 0))
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.isDrawingCacheEnabled = true

        if (savedInstanceState == null) {
            val tsk = Backgroundtask()
            tsk.execute()

        }

        var tsk = Backgroundtask();
        tsk.execute();


    }

    inner class Backgroundtask : AsyncTask<Void, Void, Boolean>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)

            adapter = Song_Adapter(Home.all_songs, this@Song_library_Act)
            recyclerView.adapter = adapter
            val itemDecoration = DividerItemDecoration(this@Song_library_Act, DividerItemDecoration.VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)
            commm(this@Song_library_Act)

        }
    }

    private fun commm(context: Context) {


        adapter.setCommnicator(object : Song_Adapter.Communicator {
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


                    Constants.mediaAfterprepared(null, context, s, position, position,
                            "general", Constants.SONG_FROM_ONLY_SONG)

                    Constants.SONG_NUMBER = position
                    val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)

                    if (!isServiceRunning) {
                      Constants.startService(applicationContext)

                    } else {
                        Constants.SONG_CHANGE_HANDLER!!.sendMessage(Constants.SONG_CHANGE_HANDLER!!.obtainMessage(0,messagearg));
                    }

                }
                catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        });



    }




    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

}
