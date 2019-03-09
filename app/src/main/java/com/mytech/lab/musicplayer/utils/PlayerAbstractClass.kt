package com.mytech.lab.musicplayer.utils

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.SongService
import de.hdodenhof.circleimageview.CircleImageView
import android.R.attr.digits
import android.graphics.drawable.LayerDrawable
import android.R.attr.bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.R.attr.bitmap







abstract class PlayerAbstractClass() : AppCompatActivity() {


    protected  var song_name: TextView?=null
    protected var artist_name: TextView?=null
    protected  var starttime: TextView?=null
    protected  var endtime: TextView?=null
    protected  var forward: SeekBar? = null

    protected var shuffle_image: ImageView? = null
    protected var playandpause_image: ImageView? = null
    protected var repeat_image: ImageView?=null
    protected var banner: ImageView? = null
    protected var songImage: CircleImageView?= null

    protected var playstatus = true
    protected var shuffle_status:Boolean = false
    protected var repeat_status:Boolean = false

    protected  var shuffle: LinearLayout?=null

    protected var repeat: LinearLayout?=null

    protected var card_playPauseIcon: ImageView?= null

    public var cardview: CardView?= null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    protected fun initiliseUIHandler(){

        Constants.PLAYER_UI = Handler(object : Handler.Callback {
            override fun handleMessage(msg: Message?): Boolean {
//                Log.i("General", "Common UI Handler")
                updateButtonUI()
                updatePlayerUI()
                return true
            }
        })
    }


    protected fun  updatePlayerUI(){
        try {
            val s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER)

            val delay:Long = 0
            song_name?.text = s.first.song_name
            artist_name?.text = s.first.artist
            Constants.SONG_SHUFFLE = Home.shared.getBoolean("shuffle",false)
            Constants.SONG_REPEAT  = Home.shared.getBoolean("repeat",false)
            loadimage(delay, s.first.albumId!!,applicationContext)
        }
        catch (e:Exception){
            Log.e("Error",e.message)}
    }


    protected abstract fun updateButtonUI()


    protected fun sendMessageToUIHandler(){
        Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0))
    }


    protected fun inilitiseUIOnResume(){

        try {
            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
            if (!isServiceRunning) {
                val current = Home.shared.getString(Constants.CURRENT_ALBUM,"alb")
                if(!current.equals("alb",ignoreCase = true))
                {
                    val songname = Home.shared.getString(Constants.SONG_NAME,"alb")
                    val artistname = Home.shared.getString(Constants.ARTIST_NAME,"alb")
                    val sub_song = Home.shared.getInt(Constants.SUB_SUB_POSITION,0)
                    val album_name = Home.shared.getString(Constants.ALBUM_NAME,"alb")
                    val playlistname = Home.shared.getString(Constants.PLAYLIST_NAME,"empty")

                    song_name?.text = songname
                    artist_name?.text = artistname
                    Constants.SONG_NUMBER = sub_song
                    Constants.servicearray(current,album_name,artistname,playlistname,true, cntx = applicationContext)

                }
            }
            sendMessageToUIHandler()
            Handler().postDelayed({
                Constants.PROGRESSBAR_HANDLER = Handler(object : Handler.Callback {
                    override fun handleMessage(msg: Message?): Boolean {
                        val i = msg?.obj as Array<Int>
                        starttime?.setText(Constants.calculatetime(i[0]))
                        endtime?.setText(Constants.calculatetime(i[1]))
                        forward?.setProgress(i[0])
                        forward?.max = i[1]
                        return true
                    }
                })
            },50)

        } catch (e: Exception) {Log.i("Error",e.message) }
    }


   private fun loadimage(delay:Long,albumId:Long,cntx: Context)
    {
//        Handler().postDelayed({

            val albumArt = Constants.getAlbumart(cntx, albumId)
            if (albumArt != null) {
                banner?.setImageBitmap(albumArt)
                songImage?.setImageBitmap(albumArt)
//                val oldArtDrawable = resources.getDrawable(R.drawable.shape) as LayerDrawable
//                val newArtDrawable = BitmapDrawable(resources, albumArt)
//                oldArtDrawable.setDrawableByLayerId(R.id.musicPlayerBackGround, newArtDrawable)
            } else {
                banner?.setImageResource(R.drawable.default_general_player_albumart)
                songImage?.setImageResource(R.drawable.default_general_player_albumart)

            }
//        },0)
    }

    protected fun updateSeekbar(){
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
}