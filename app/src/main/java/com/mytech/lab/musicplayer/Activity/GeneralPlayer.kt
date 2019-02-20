package com.mytech.lab.musicplayer.Activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.*
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.Controls
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.SongService
import com.mytech.lab.musicplayer.sub_sub_fragment.Playlist_single
import com.mytech.lab.musicplayer.utils.Song_base
import de.hdodenhof.circleimageview.CircleImageView


class GeneralPlayer : AppCompatActivity(), View.OnClickListener {


    internal var artist_name_intent: String? = null

    private val mAccel: Float = 0.toFloat()
    private val mAccelCurrent: Float = 0.toFloat()
    private val mAccelLast: Float = 0.toFloat()


    internal var cardvisible:String = "yes"

    internal var sub_song_position = 0
    internal var actual_song_position = 0

    internal var first_time_card: String? = null

    private var playlist_name:String? = "null"
    internal var comefrom: String? = null

    private var handler: Handler? = null
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if(Wel.colorshared.getInt("themename",-1)!=-1)
        {
            setTheme(Wel.colorshared.getInt("themename", R.style.AppFullScreenTheme))
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_player_ui)
//        toolbar = findViewById(R.id.GeneralPlayer_toolbar_layout)
//        toolbar.setTitle("Music Player")
//        setSupportActionBar(toolbar)
        getviews()


        handler = Handler()
        var s: Song_base? = null

        cardvisible = "no"

        first_time_card = "first"


        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
        if (isServiceRunning) {
            forward?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    if (b) {
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

    fun getviews()
    {
        song_name = findViewById(R.id.song_name)
        songImage = findViewById(R.id.songImage)
        artist_name = findViewById(R.id.artist_name)
        banner = findViewById(R.id.center_banner)
        banner?.isClickable = false
        playandpause = findViewById(R.id.playandpause)
        start = findViewById(R.id.start)
        end = findViewById(R.id.end)
        forward = findViewById(R.id.seekbar)
        shuffle_image = findViewById(R.id.shuffle_image)
        repeat_image = findViewById(R.id.repeat_image)
        playandpause_image = findViewById(R.id.playandpause_image)
        shuffle = findViewById(R.id.shuffle)
        fav_head = findViewById(R.id.fav_id)
        fav_Image = findViewById(R.id.fav_image)

        shuffle = findViewById(R.id.shuffle)
        shuffle?.setOnClickListener (this)

        prev = findViewById(R.id.prev)
        prev?.setOnClickListener(this)

        playandpause!!.setOnClickListener(this)

        next = findViewById(R.id.next)
        next.setOnClickListener(this)

        repeat = findViewById(R.id.repeat)
        repeat.setOnClickListener(this)

        fav_head?.setOnClickListener(this)


    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.playandpause -> {
                Constants.playandpause(applicationContext)}

            R.id.next -> {
                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
                if (!isServiceRunning)
                {
                    val i = Intent(applicationContext, SongService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(i);
                    } else {
                        startService(i);
                    }

                }
                Controls.nextControl(applicationContext)

            }

            R.id.prev -> {
                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
                if (!isServiceRunning)
                {
                    val i = Intent(applicationContext, SongService::class.java)
                    startService(i)
                }
                Controls.previousControl(applicationContext)

            }

            R.id.shuffle -> { Constants.change_shuffle(applicationContext)}

            R.id.repeat -> { Constants.change_repeat(applicationContext)}

            R.id.fav_id ->{

                try
                {
                    val s: Song_base = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first
                    val position = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).second
                    if (Home.helper.checkexists_for_song_in_table(s.song_name, "favourites") <= 0) {
                        val check: Long = Home.helper.insert_in_any_table(s.song_name, s.artist, s.url, s.albumId.toString(), s.album_name, position, s.duration, 2, "favourites", null)

                        if (check > 0)
                        {
                            Playlist_single.notify_change()
                            Toast.makeText(applicationContext, "1 song added to Favourite Song", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                    {
                        Home.helper.deletesong_for_table(s.song_name, "favourites")
                        Toast.makeText(applicationContext, "Remove From Favourite", Toast.LENGTH_SHORT).show()
                    }

                    update_favourite()
                }
                catch (e:Exception){}

            }

        }

    }

    override fun onResume() {
        super.onResume()
        try {
            update_favourite()
            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
            if (isServiceRunning) {
                Handler().postDelayed({
                    updateUI_GeneralPlayer(applicationContext)
                },500)


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

                    song_name?.text = songname
                    artist_name?.text = artistname
                    changeuiwithbutton_general(applicationContext)
                    Constants.SONG_NUMBER = sub_song
                    Constants.servicearray(current,album_name,artistname,playlistname)

                }
            }
            changeButton_general()
            Handler().postDelayed({
                Constants.PROGRESSBAR_HANDLER = Handler(object : Handler.Callback {
                    override fun handleMessage(msg: Message?): Boolean {
                        val i = msg?.obj as Array<Int>
                        start?.setText(Constants.calculatetime(i[0]))
                        end?.setText(Constants.calculatetime(i[1]))
                        forward?.setProgress(i[0])
                        forward?.max = i[1]
                        return true
                    }
                })
            },300)

        } catch (e: Exception) { }
    }

        fun updateUI_GeneralPlayer(cntx:Context)
        {
            try {
                val s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER)

                val delay:Long = 0
                first_time_card = "abc"
                artist_name?.text = s.first.artist
                song_name?.text = s.first.song_name
                loadimage(delay, s.first.albumId!!,cntx)
            }
            catch (e:Exception){}


        }
        internal fun changeButton_general()
        {
            try {
                if (Constants.SONG_PAUSED)
                    playandpause_image?.setImageResource(R.drawable.ic_play)

                else {
                    playandpause_image?.setImageResource(R.drawable.ic_pause)}


                if(Constants.SONG_SHUFFLE==true) {
                    shuffle_image?.setImageResource(R.drawable.ic_shuffle_orange)}

                else {shuffle_image?.setImageResource(R.drawable.ic_shuffle_black)}

                if(Constants.SONG_REPEAT==true) { repeat_image?.setImageResource(R.drawable.ic_repeat_orange)}

                else {repeat_image?.setImageResource(R.drawable.ic_repeat_black)}

            }catch (e:Exception){e.printStackTrace()}
        }

        fun update_favourite()
        {

            val song_name:String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.song_name
            if(Home.helper.checkexists_for_song_in_table(song_name,"favourites")>0)
            {
                fav_Image?.setImageResource(R.drawable.fav_click)
            }
            else
            {
                fav_Image?.setImageResource(R.drawable.fav_unclick)

            }
        }

        fun changeuiwithbutton_general(cntx:Context)
        {
            updateUI_GeneralPlayer(cntx)
            changeButton_general()
        }



        fun loadimage(delay:Long,albumId:Long,cntx:Context)
        {
            Handler().postDelayed({

                val albumArt = Constants.getAlbumart(cntx, albumId)
                if (albumArt != null) {
                    banner?.setImageBitmap(albumArt)
                    songImage?.setImageBitmap(albumArt)

                } else {
                    banner?.setImageResource(R.drawable.default_general_player_albumart)
                    songImage?.setImageResource(R.drawable.default_general_player_albumart)
                }
            },delay)
        }

    companion object {
        private lateinit var toolbar:Toolbar
        private var playstatus = true
        private var shuffle_status:Boolean = false
        private var repeat_status:Boolean = false
        private  var forward: SeekBar? = null
        private var song_adapter: Song_Adapter? = null
        private  var song_name: TextView?=null
        private  var artist_name: TextView?=null
        private  var start: TextView?=null
        private  var end: TextView?=null
        private  var shuffle: LinearLayout?=null
        private  var prev: LinearLayout?=null
        private var playandpause: LinearLayout? = null
        private lateinit var next: LinearLayout
        private lateinit var repeat: LinearLayout
        private var shuffle_image: ImageView? = null
        private var playandpause_image: ImageView? = null
        private var repeat_image: ImageView?=null
        private var banner: ImageView? = null
        private var fav_head: LinearLayout?=null
        private var fav_Image: ImageView? = null
        private var songImage : CircleImageView?= null;
    }

}
