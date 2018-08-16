package com.mytech.lab.musicplayer.sub_sub_fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.io.IOException
import android.R.attr.bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.widget.PopupMenu
import android.view.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.SongAdapter_Functionality
import com.mytech.lab.musicplayer.SongService
import com.mytech.lab.musicplayer.utils.Song_base
import java.io.File


class Album_expand :Fragment() {


    var album_name:String? = null
    var song_Adapter: Song_Adapter? = null
    var recyclerView:RecyclerView? = null
    lateinit var cntx:Context
    var album_back_image:LinearLayout? = null
    var album_song_array:ArrayList<Song_base> = ArrayList<Song_base>()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        cntx = context!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v:View = inflater.inflate(R.layout.sub_sub_fragment_album_expand,container,false);

        album_name = arguments?.getString("album_name")
        album_back_image = v.findViewById(R.id.album_image_back)

        recyclerView = v.findViewById(R.id.recycler_album_expand)
        recyclerView?.layoutManager = LinearLayoutManager(v.context)
        recyclerView?.setHasFixedSize(true)

        Log.i("Album name is ",album_name)
        if(Home.albummap.get(album_name)!=null)
        {
            for (temp in Home.albummap.get(album_name)!!)
            {
                if(File(temp.first.url).exists()) {
                    album_song_array.add(temp.first)
                }
            }

            if (album_song_array.size > 0) {
                song_Adapter = Song_Adapter(album_song_array, cntx)
                recyclerView?.adapter = song_Adapter
                recyclerView?.isDrawingCacheEnabled = true
                recyclerView?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
                comm()
            }
        }
        else
        {
            val empty:TextView = v.findViewById(R.id.empty_album)
            empty.visibility = View.VISIBLE
        }

        return v
    }

    private fun comm() {


        song_Adapter?.setCommnicator (object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {

                try {


                    Constants.servicearray("album",album_name = s.album_name)
                    var messagearg:String = ""

                    if("album".equals(Home.shared.getString("current_album","alb"),ignoreCase = true))
                    {
                        if(Home.shared.getString("album_name","alb").equals(s.album_name,ignoreCase = true))
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

                    val actual_song_pos = Home.albummap[s.album_name]!!.get(position).second
//
                    Constants.mediaAfterprepared(null,context,s,actual_song_pos, position,"general",
                            "album")

                    Constants.SONG_NUMBER = position

                    val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx)
                    if (!isServiceRunning) {
                        val i = Intent(cntx, SongService::class.java)
                        cntx.startService(i)
                    } else {

                        Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,messagearg));

                    }

                    Home.cardview.visibility = View.VISIBLE
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        })

        song_Adapter?.setclick(object : Song_Adapter.Menuclick{

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
                when (item.getItemId()) {

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,cntx!!).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,cntx!!).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).addToQueue() }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).delete( song_Adapter, album_song_array) }

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