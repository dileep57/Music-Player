package com.mytech.lab.musicplayer.Recyclerview_adapter

import android.content.Context
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.utils.Song_base
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import java.util.ArrayList

import android.graphics.Bitmap





/**
 * Created by lnx on 27/2/18.
 */

class Album_adapter(internal var st: ArrayList<Song_base>, internal var context: Context) : RecyclerView.Adapter<Album_adapter.MyViewHolder>(){
    internal var inflater: LayoutInflater

    internal var communicator: Communicator? = null
//    internal val opensans:Typeface;

    init {
        inflater = LayoutInflater.from(context)
//        opensans = Typeface.createFromAsset(context.assets, "fonts/Amiko-Regular.ttf")
    }


    interface Communicator {
        fun clickonplaybutton(v: View, s: Song_base, position: Int)
    }

    fun setCommnicator(c: Communicator) {
        this.communicator = c
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = inflater.inflate(R.layout.recycler_album_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val s = st[position]
        holder.album_name.text = s.album_name
        holder.artist_title.text = s.artist

        getimageart(s.albumId, context, holder.banner)
        val h = Handler()

        holder.relativeLayout.setOnClickListener { view ->

                communicator?.clickonplaybutton(view, s, position)

        }


    }

    override fun getItemCount(): Int {
        return st.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var album_name: TextView
        internal var artist_title: TextView
        internal var banner: ImageView
        internal var relativeLayout: RelativeLayout

        init {
            album_name = itemView.findViewById(R.id.title)
            artist_title = itemView.findViewById(R.id.artist_title)
            banner = itemView.findViewById(R.id.thumbnail)
            relativeLayout = itemView.findViewById(R.id.relative_layout)
        }
    }


    private fun getimageart(albumId: Long?, context: Context, banner: ImageView) {


        var main_url : String = "content://media/external/audio/albumart/"+albumId.toString()

            Glide.with(context)
                    .load(main_url)
                    .error(R.drawable.music_song_icon_white)
                    .placeholder(R.drawable.music_song_icon_white)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(banner)
    }

}
