package com.mytech.lab.musicplayer.sub_sub_fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.IOException
import java.util.ArrayList

/**
 * Created by lnx on 13/3/18.
 */
class Playlist_single : android.support.v4.app.Fragment() {


    internal var cntx: Context? = null
    private var playlist_song:ArrayList<Song_base>? = null
    private lateinit var playlist_name:String
    internal var song_adapter:Song_Adapter?=null
    lateinit var helper: DatabaseHelperAdapter

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.cntx = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v : View = inflater.inflate(R.layout.sub_sub_fragment_playlist_expand,container,false)

        playlist_name = arguments!!.getString("name")

        helper = DatabaseHelperAdapter(v.context)

        if(playlist_name.equals("Recent_add_song",ignoreCase = true))
        {
            playlist_song = Constants.get_topsong(context!!, MediaStore.Audio.Media.DATE_ADDED + " DESC")
        }
        else if(playlist_name.equals("favourites",ignoreCase = true))
        {
            playlist_song = helper.getalldata_table("favourites")
        }
        else
        {
            playlist_song = helper.getalldata_playlist(playlist_name)
        }


        val recyclerView:FastScrollRecyclerView = v.findViewById(R.id.recycler_single)
        recyclerView.layoutManager = LinearLayoutManager(v.context)
        recyclerView.setHasFixedSize(true)
        song_adapter = Song_Adapter(playlist_song!!,v.context)

        recyclerView.adapter = song_adapter
        comm(playlist_name)
        return v
    }

    private fun comm(playlist:String) {
        song_adapter?.setCommnicator (object : Song_Adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {


                try {
                    Constants.servicearray("popup_playlist",s.album_name,s.artist,playlist,false,context)

                    var messagearg:String = ""
                    if("popup_playlist".equals(Home.shared.getString("current_album","alb"),ignoreCase = true))
                    {
                        if(Home.shared.getString("playlist","alb").equals(playlist,ignoreCase = true))
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

                    val actual_song_pos = Home.Songname_position.get(s.song_name)!!
                    Constants.mediaAfterprepared(null,cntx,s,actual_song_pos, position,"general",
                            "popup_playlist",playlist)

                        Constants.SONG_NUMBER = position
                        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntx!!)
                        if (!isServiceRunning)
                        {
                            Constants.startService(cntx!!)
                        } else {
                            Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,messagearg))

                        }

//                        Home().cardview?.visibility = View.VISIBLE

                } catch (e: Exception) {
                    Log.e("Error",e.message)
                    Home.filenotsupport(cntx!!)
                }
            }

        })

        song_adapter?.setclick(object : Song_Adapter.Menuclick{

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
        if(!playlist_name.equals("Recent_add_song") && !playlist_name.equals("favourites"))
            popup.menu.findItem(R.id.remove_from_playlist).isVisible = true

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.getItemId()) {

                    R.id.play -> { SongAdapter_Functionality(pop,temp,position,cntx!!).play()}

                    R.id.play_next -> { SongAdapter_Functionality(pop,temp,position,cntx!!).play_next()}

                    R.id.add_to_queue -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).addToQueue() }

                    R.id.remove_from_playlist -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).removeFromPlaylist(temp.song_name,playlist_name, song_adapter,playlist_song) }

                    R.id.delete -> {
                        SongAdapter_Functionality(pop,temp,position,cntx!!).delete( song_adapter, playlist_song) }

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

        public fun notify_change()
        {
            song_adapter?.notifyDataSetChanged()
        }

}