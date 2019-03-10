package com.mytech.lab.musicplayer.Recyclerview_adapter

import java.io.File

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log

import com.mytech.lab.musicplayer.Fragments.Recent_song
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import android.os.Handler
import android.view.*
import android.webkit.WebView
import android.widget.*
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Sub_Fragments.Albums
import com.mytech.lab.musicplayer.Sub_Fragments.Playlist
import com.mytech.lab.musicplayer.sub_sub_fragment.Album_expand
import com.mytech.lab.musicplayer.sub_sub_fragment.Playlist_single
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mytech.lab.musicplayer.Sub_Fragments.Songs
import kotlin.collections.ArrayList

/**
 * Created by lnx on 27/2/18.
 */

class Song_Adapter(internal var song_info: ArrayList<Song_base>, internal var context: Context) : RecyclerView.Adapter<Song_Adapter.MyViewHolder>() {


    internal var inflater: LayoutInflater
    internal var inflater1: LayoutInflater
    internal var dialog: AlertDialog.Builder? = null
    internal lateinit var dg: AlertDialog
    internal lateinit var remove: TextView
    internal lateinit var play: TextView
    internal lateinit var add_playlist:TextView;
    internal var search: TextView? = null
    internal lateinit var send: TextView
    internal lateinit var set_ringtone: TextView
    internal lateinit var detail: TextView
    internal lateinit var helper: DatabaseHelperAdapter

    internal var communicator: Communicator? = null
    internal var menuclick: Menuclick? = null


    init {
        inflater = LayoutInflater.from(context)
        inflater1 = LayoutInflater.from(context)

    }

    interface Communicator {
        fun clickonplaybutton(v: View, s: Song_base, position: Int)
    }

    interface Menuclick {
        fun clickonmenu(v: LinearLayout, s: Song_base, position: Int)
    }

    fun setCommnicator(c: Communicator) {
        this.communicator = c
    }

    fun setclick(m: Menuclick) {
        this.menuclick = m
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Song_Adapter.MyViewHolder {
        val v = inflater.inflate(R.layout.recycler_song_layout, parent, false)
//        equilizerstat = v.findViewById(R.id.equalizer_view)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val song = song_info[position]

        holder.song_name.text = song.song_name
        holder.artist.text = song.artist
        holder.duration.text = Constants.calculatetime(Integer.parseInt(song.duration))
//        val eq:EqualizerView = holder.equiliser
//        equilizerstat.visibility = View.INVISIBLE
        val h = Handler()

        holder.popup.setOnClickListener {view ->
//            playwithpopmenu(holder.popup,song,position)
//            Log.i("LOG"," CLICK")
            if(menuclick !=null )
            {
                menuclick?.clickonmenu(holder.popup, song, position)
            }

        }

        getimageart(song.albumId, song.context, holder.bannerp,R.drawable.music_song_icon_white)





        holder.relativeLayout.setOnClickListener { view ->
            if (communicator != null) {
                Handler().postDelayed({
                        communicator?.clickonplaybutton(view, song, position)
                },250)

            }
        }

        holder.relativeLayout.setOnLongClickListener {
//            dialog_function(position, song)
            true
        }


    }


    override fun getItemCount(): Int {
        return song_info.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var song_name: TextView
        internal var artist: TextView
        internal var duration: TextView
        internal var cardView: CardView
        internal var play: Button? = null
        internal var bannerp: ImageView
        internal var relativeLayout: RelativeLayout
        internal var popup:LinearLayout


        init {
            song_name = itemView.findViewById(R.id.song_name)
            artist = itemView.findViewById(R.id.artist)
            cardView = itemView.findViewById(R.id.cardview)
            bannerp = itemView.findViewById(R.id.small_banner)
            relativeLayout = itemView.findViewById(R.id.cardrelative)
            duration = itemView.findViewById(R.id.duration)
            popup = itemView.findViewById(R.id.popup)

        }


    }


    fun setfilter(query:String):ArrayList<Pair<String,Int>>
    {
        val all_Song:ArrayList<Pair<String,Int>> = ArrayList()
        for(i in song_info.indices)
        {
           all_Song.add(Pair(song_info.get(i).song_name,i))
        }

        return all_Song
    }




    private fun dialog_function(position: Int, temp: Song_base) {
        val dialog = AlertDialog.Builder(context)
        val dp = inflater1.inflate(R.layout.dialog_box_container, null)
        dialog.setView(dp)
        dg = dialog.create()
        dg.show()
        val head = dp.findViewById<TextView>(R.id.heading_container)
        head.text = temp.song_name
        remove = dp.findViewById(R.id.delete_dialog)
        play = dp.findViewById(R.id.play_dialog)
        detail = dp.findViewById(R.id.detail_dialog)
        send = dp.findViewById(R.id.send_dialog)
        set_ringtone = dp.findViewById(R.id.set_ringtone_dialog)
        add_playlist = dp.findViewById(R.id.add_to_playlist)

    }

    companion object {

        @JvmOverloads
        fun getimageart(albumId:Long?, context: Context, image: ImageView,errort:Int) {

            Glide.with(context)
                    .load("content://media/external/audio/albumart/"+albumId.toString())
                    .error(errort)
                    .placeholder(errort)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image)

        }



    }

}
