package com.mytech.lab.musicplayer.Activity

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.utils.PlayerAbstractClass
import com.mytech.lab.musicplayer.utils.Song_base

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import java.io.IOException

class MusicPlayer : PlayerAbstractClass(), View.OnClickListener {

    private lateinit var toolbar: Toolbar

    private var query: String? = null
    private var playandpause: LinearLayout?=null
    private var prev: LinearLayout?=null
    private var next: LinearLayout?=null

    var oncurrentactivity : Boolean = false


    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var db: SQLiteDatabase? = null

    private var song_position: Int = 0
    private var playeropenfirsttime: String? = "no"

    private lateinit var recyclerView: FastScrollRecyclerView
    private var adapter: Song_Adapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musicplayer)
        getview()

        toolbar = findViewById(R.id.toolbar_player2)


        setSupportActionBar(toolbar)
        shuffle_status = Home.shared.getBoolean("shuffle", false)
        repeat_status = Home.shared.getBoolean("repeat", false)

        handler = Handler()
        oncurrentactivity = true

        Thread {
            adapter = Song_Adapter(Home.all_songs, applicationContext)
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.adapter = adapter
            commmunicationAdapter()
        }.start()

        initiliseUIHandler()


       updateSeekbar()
//
//        Constants.PLAYER_UI = Handler(object : Handler.Callback {
//                override fun handleMessage(msg: Message?): Boolean {
//                    Log.i("MusicPlayer ","call")
//                    try {
//                        val s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER)
//
//                        artist_name?.text = s.first.artist
//                        song_name?.text = s.first.song_name
//
//                        loadimage(200, s.first.albumId)
//                    }
//                    catch (e:Exception){}
//
//
//
//                    return true
//                }
//            })


    }

    override fun updateButtonUI() {
        try {
            if (Constants.SONG_PAUSED) { playandpause_image?.setImageResource(R.drawable.album_play) }

            else { playandpause_image?.setImageResource(R.drawable.album_pause) }


            if(Constants.SONG_SHUFFLE ==true) {
                shuffle_image?.setImageResource(R.drawable.ic_shuffle_click_24dp)}

            else {
                shuffle_image?.setImageResource(R.drawable.ic_shuffle_black_24dp)}

            if(Constants.SONG_REPEAT ==true) { repeat_image?.setImageResource(R.drawable.ic_repeat_click_24dp)}

            else {
                repeat_image?.setImageResource(R.drawable.ic_repeat_one_black_24dp)}

        }catch (e:Exception){Log.e("Error",e.message)}
    }

    private fun commmunicationAdapter() {

        adapter?.setCommnicator(object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {

                try {
                    Constants.servicearray("only_song")

                    var messagearg:String = ""
                    if("only_song".equals(Home.shared.getString("current_album","alb"),ignoreCase = true))
                    {
                        messagearg = "false"
                    }
                    else
                    {
                        messagearg = "true"
                    }

                    Constants.mediaAfterprepared(null, applicationContext, s, position, position,
                            "general", "only_song")

                    Constants.SONG_NUMBER = position
                    val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)

                    if (!isServiceRunning)
                    {
                        val i = Intent(applicationContext, SongService::class.java)
                        // cntx.startService(i)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            applicationContext.startForegroundService(i);
                        } else {
                            applicationContext.startService(i);
                        }



                    } else {

                        val msg = Message.obtain()
                        msg.obj = messagearg
                        Constants.SONG_CHANGE_HANDLER?.sendMessage(msg)

                    }

                }
                catch (e: IOException) { }

            }
        })

        adapter?.setclick(object : Song_Adapter.Menuclick{

            override fun clickonmenu(v: LinearLayout, s: Song_base, position: Int) {
                playwithpopmenu(v,s,position)
            }
        })
    }

    fun playwithpopmenu(pop: LinearLayout, temp: Song_base, position: Int)
    {
        val popup = PopupMenu(applicationContext, pop)
        popup.inflate(R.menu.pop_menu_song)

        var inflater1:LayoutInflater? = null
        inflater1 = LayoutInflater.from(applicationContext)

        if(Build.VERSION.SDK_INT>=23)
        {
            popup.gravity = Gravity.END
        }

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.getItemId()){

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,applicationContext).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,applicationContext).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,applicationContext).addToQueue() }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,applicationContext).delete( adapter, Home.all_songs) }

                    R.id.send -> {
                        SongAdapter_Functionality(pop,temp,position,applicationContext).send()}

                    R.id.set_ringtone -> {
                        SongAdapter_Functionality(pop,temp,position, applicationContext).setRingtone() }

                    R.id.add_to_playlistt -> {
                        SongAdapter_Functionality(pop,temp,position, applicationContext).addToPlaylist(inflater1) }

                    R.id.detail -> {
                        SongAdapter_Functionality(pop,temp,position, applicationContext).detail(inflater1)}

                    R.id.add_favrioute -> {
                        SongAdapter_Functionality(pop,temp,position, applicationContext).addToFavrioute() }

                    R.id.search -> {
                        SongAdapter_Functionality(pop,temp,position, applicationContext).search() }

                }
                return true
            }
        })

        popup.show()
    }

    private fun getview()
    {
        forward = findViewById(R.id.forward)
        forward!!.progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        song_name = findViewById(R.id.song_name)
        song_name!!.text = "Song Name"
        artist_name = findViewById(R.id.artist)
        artist_name!!.text = "Artist"
        starttime = findViewById(R.id.starttime)
        starttime!!.text = "start"
        endtime = findViewById(R.id.totaltime)
        endtime!!.text = "End"
        banner = findViewById(R.id.gallery)
        playandpause = findViewById(R.id.playandpause)
        playandpause!!.setOnClickListener(this)
        playandpause_image = findViewById(R.id.playandpause_image)
        prev = findViewById(R.id.prev)
        prev!!.setOnClickListener(this)
        next = findViewById(R.id.next)
        next!!.setOnClickListener(this)
        repeat = findViewById(R.id.repeat)
        repeat!!.setOnClickListener(this)
        repeat_image = findViewById(R.id.repeat_image)
        shuffle = findViewById(R.id.shuffle)
        shuffle!!.setOnClickListener(this)
        shuffle_image = findViewById(R.id.shuffle_image)

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
    }






    override fun onClick(v: View) {

        if(Home.shared.getString("current_album",null)==null)
        {
            return
        }
        when (v.id) {
            R.id.playandpause -> {
                Constants.playandpause(applicationContext)
            }

            R.id.next -> {
                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
                if (!isServiceRunning)
                {
                    val i = Intent(applicationContext, SongService::class.java)
                    applicationContext.startService(i)

                }
                Controls.nextControl(applicationContext)

            }

            R.id.prev -> {
                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
                if (!isServiceRunning)
                {
                    val i = Intent(applicationContext, SongService::class.java)
                    applicationContext.startService(i)
                }
                Controls.previousControl(applicationContext)

            }

            R.id.shuffle -> {
                Constants.change_shuffle(applicationContext)
            }

            R.id.repeat -> {
                Constants.change_repeat(applicationContext)
            }
        }


    }

    private fun buttonclick(res:Boolean)
    {
        playandpause?.isClickable = res
        prev?.isClickable = res
        next?.isClickable = res
    }


    override fun onResume() {
        super.onResume()
        inilitiseUIOnResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        oncurrentactivity = false
    }


}
