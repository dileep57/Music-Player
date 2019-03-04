package com.mytech.lab.musicplayer.Activity

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.*
import android.support.v7.app.AppCompatActivity
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
import com.mytech.lab.musicplayer.utils.Song_base

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import java.io.IOException

class MusicPlayer : AppCompatActivity(), View.OnClickListener {

    internal lateinit var toolbar: Toolbar

    internal var query: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musicplayer)

        toolbar = findViewById(R.id.toolbar_player2)


        setSupportActionBar(toolbar)
        shuffle_status = Home.shared.getBoolean("shuffle", false)
        repeat_status = Home.shared.getBoolean("repeat", false)

        getview()
        handler = Handler()
        oncurrentactivity = true

        cntx = this


        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH


        if (savedInstanceState == null) {
            val tsk = Backtask()
            tsk.execute()
        }

        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)
        if (isServiceRunning) {
            forward.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    if(b) {
                        SongService.mp!!.seekTo(i)
                    }


                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    SongService.mp!!.pause()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    SongService.mp!!.start()

                }
            })
        }


    }


    private fun commm() {

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

                    Constants.mediaAfterprepared(null, cntx, s, position, position,
                            "general", "only_song")

                    Constants.SONG_NUMBER = position
                    val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)

                    if (!isServiceRunning)
                    {
                        val i = Intent(cntx, SongService::class.java)
                       // cntx.startService(i)

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

                    Home.cardview.visibility = View.VISIBLE
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
                when (item.getItemId()){

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,cntx).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,cntx).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).addToQueue() }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).delete( adapter, Home.all_songs) }

                    R.id.send -> {
                        SongAdapter_Functionality(pop,temp,position,cntx).send()}

                    R.id.set_ringtone -> {
                        SongAdapter_Functionality(pop,temp,position, cntx).setRingtone() }

                    R.id.add_to_playlistt -> {
                        SongAdapter_Functionality(pop,temp,position, cntx).addToPlaylist(inflater1) }

                    R.id.detail -> {
                        SongAdapter_Functionality(pop,temp,position, cntx).detail(inflater1)}

                    R.id.add_favrioute -> {
                        SongAdapter_Functionality(pop,temp,position, cntx).addToFavrioute() }

                    R.id.search -> {
                        SongAdapter_Functionality(pop,temp,position, cntx).search() }

                }
                return true
            }
        })

        popup.show()
    }

    private fun getview()
    {
        forward = findViewById(R.id.forward)
        forward.progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        song_name = findViewById(R.id.song_name)
        song_name.text = "Song Name"
        artist_title = findViewById(R.id.artist)
        artist_title.text = "Artist"
        starttime = findViewById(R.id.starttime)
        starttime.text = "start"
        totaltime = findViewById(R.id.totaltime)
        totaltime.text = "End"
        gallery = findViewById(R.id.gallery)
        playandpause = findViewById(R.id.playandpause)
        playandpause.setOnClickListener(this)
        prev = findViewById(R.id.prev)
        prev.setOnClickListener(this)
        nxt = findViewById(R.id.next)
        nxt.setOnClickListener(this)
        repeat = findViewById(R.id.repeat)
        repeat.setOnClickListener(this)
        shuffle = findViewById(R.id.shuffle)
        shuffle.setOnClickListener(this)
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
                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)
                if (!isServiceRunning)
                {
                    val i = Intent(cntx, SongService::class.java)
                    cntx.startService(i)

                }
                Controls.nextControl(cntx)

            }

            R.id.prev -> {
                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)
                if (!isServiceRunning)
                {
                    val i = Intent(cntx, SongService::class.java)
                    cntx.startService(i)
                }
                Controls.previousControl(cntx)

            }

            R.id.shuffle -> {
                Constants.change_shuffle(cntx)
            }

            R.id.repeat -> {
                Constants.change_repeat(cntx)
            }
        }


    }

     private object inner class Backtask : AsyncTask<Void, Void, Boolean>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            adapter = Song_Adapter(Home.all_songs, cntx)
            recyclerView.layoutManager = LinearLayoutManager(cntx)
            recyclerView.adapter = adapter
            return true
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
            MusicPlayer().commm()
        }
    }


    companion object {

        var oncurrentactivity : Boolean = false
        internal lateinit var forward: SeekBar
        internal lateinit var song_name: TextView
        internal lateinit var artist_title: TextView
        internal lateinit var starttime: TextView
        internal lateinit var totaltime: TextView
        internal var playstatus: Boolean? = false
        internal var shuffle_status: Boolean? = false
        internal var repeat_status: Boolean? = false
        internal lateinit var playandpause: ImageView
        internal lateinit var prev: ImageView
        internal lateinit var nxt: ImageView
        internal lateinit var repeat: ImageView
        internal lateinit var shuffle: ImageView
        internal lateinit var runnable: Runnable
        internal lateinit var handler: Handler
        internal var db: SQLiteDatabase? = null

        internal var song_position: Int = 0
        internal var playeropenfirsttime: String? = "no"

        internal lateinit var cntx: Context
        internal lateinit var gallery: ImageView

        internal lateinit var recyclerView: FastScrollRecyclerView
        internal var adapter: Song_Adapter?=null

    }

    fun loadimage(delay:Long,albumId:Long?)
    {
        Handler().postDelayed({
            val albumArt = Constants.getAlbumart(cntx, albumId)
            if (albumArt != null) {
                gallery.setImageBitmap(albumArt)

            } else {
                gallery.setImageResource(R.drawable.default_general_player_albumart)

            }
        },delay)
    }


    private fun buttonclick(res:Boolean)
    {
        playandpause.isClickable = res
        prev.isClickable = res
        nxt.isClickable = res
    }

    fun updateUI_Musicplayer()
    {
        try {
            val s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER)

            artist_title.text = s.first.artist
            song_name.text = s.first.song_name

            loadimage(200, s.first.albumId)
        }
        catch (e:Exception){}


    }

    fun changeButton_musicplayer()
    {
        runOnUiThread {
            try {
                if (Constants.SONG_PAUSED) { playandpause.setImageResource(R.drawable.album_play) }

                else { playandpause.setImageResource(R.drawable.album_pause) }


                if(Constants.SONG_SHUFFLE ==true) {
                    shuffle.setImageResource(R.drawable.ic_shuffle_click_24dp)}

                else {
                    shuffle.setImageResource(R.drawable.ic_shuffle_black_24dp)}

                if(Constants.SONG_REPEAT ==true) { repeat.setImageResource(R.drawable.ic_repeat_click_24dp)}

                else {
                    repeat.setImageResource(R.drawable.ic_repeat_one_black_24dp)}

            }catch (e:Exception){Log.e("Error",e.message)}
        }
    }

    fun changeUIwithbutton_musicplayer(cntx:Context)
    {
        updateUI_Musicplayer()
        changeButton_musicplayer()
    }





    override fun onResume() {
        super.onResume()
        try {
            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)
            if (isServiceRunning) {
                Handler().postDelayed({
                    updateUI_Musicplayer()
                },100)


            }
            else
            {

                val current = Home.shared.getString("current_album","alb")

                if(!current.equals("alb",ignoreCase = true))
                {

                    val songname = Home.shared.getString("song_name","alb")
                    val artistname = Home.shared.getString("artist_name","alb")
                    val sub_song = Home.shared.getInt("sub_song_position",0)
                    val album_name = Home.shared.getString("album_name","alb")
                    val playlistname = Home.shared.getString("popup_playlist","popup_playlist")

                    song_name.text = songname
                    artist_title.text = artistname
                    changeUIwithbutton_musicplayer(cntx)
                    Constants.SONG_NUMBER = sub_song
                    Constants.servicearray(current, album_name, artistname, playlistname)

                }
            }
            changeButton_musicplayer()
            Handler().postDelayed({
                Constants.PROGRESSBAR_HANDLER = Handler(object : Handler.Callback {
                    override fun handleMessage(msg: Message?): Boolean {
                        val i = msg?.obj as Array<Int>
                        starttime.setText(Constants.calculatetime(i[0]))
                        totaltime.setText(Constants.calculatetime(i[1]))
                        forward.setProgress(i[0])
                        forward.max = i[1]

                        return true
                    }
                })
            },300)

        } catch (e: Exception) { }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        oncurrentactivity = false
    }


}
