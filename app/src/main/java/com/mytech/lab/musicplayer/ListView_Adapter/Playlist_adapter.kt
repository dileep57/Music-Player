package com.mytech.lab.musicplayer.ListView_Adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import android.content.DialogInterface
import android.content.Intent
import android.provider.MediaStore
import android.widget.*
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Sub_Fragments.Playlist
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base


/**
 * Created by lnx on 13/3/18.
 */

class Playlist_adapter(context: Context, playlist_string: ArrayList<String>) : ArrayAdapter<String>(context, R.layout.recycler_listview_playlist_layout, playlist_string) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {


        val inflater = LayoutInflater.from(context)

        var alert: AlertDialog.Builder = AlertDialog.Builder(context);

        var helper: DatabaseHelperAdapter = DatabaseHelperAdapter(context)

        val v = inflater.inflate(R.layout.recycler_listview_playlist_layout, parent, false)

        val nameofplaylist = getItem(position)

        val name = v.findViewById<TextView>(R.id.playlis_name)
        name.text = nameofplaylist.capitalize()

        val track = v.findViewById<TextView>(R.id.total_track)

        track.setText(helper.getalldata_playlist(nameofplaylist).size.toString()+ " Track")

        var pop : LinearLayout = v.findViewById(R.id.popup)
        pop.setOnClickListener {

            val popup = PopupMenu(context, pop)

            popup.inflate(R.menu.popup_playlist)


            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {


                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.getItemId()) {

                        R.id.delete_playlist ->
                        {
                            Toast.makeText(context,"del click",Toast.LENGTH_SHORT).show()
                            val alertbox = AlertDialog.Builder(v.rootView.context)
                            alert.setTitle("Alert!!")
                            alert.setMessage("Are you sure to delete this playlist")

                            alert.setPositiveButton("Yes", object : DialogInterface.OnClickListener {

                                override fun onClick(dialog: DialogInterface, which: Int) {

                                    if(helper.deleteplaylist(nameofplaylist)>0) {
                                        Toast.makeText(context,"Deleted",Toast.LENGTH_SHORT).show()
                                        notifyDataSetChanged()
                                        Playlist.playlist_name_array.remove(nameofplaylist)
                                        Playlist().notifychange()
                                        Home().helper?.deleteplaylist(nameofplaylist)
                                    }
                                    else
                                    {
                                        Toast.makeText(context,"Problem Occur",Toast.LENGTH_SHORT).show()
                                    }
                                    dialog.dismiss()

                                }
                            })
                            alert.setNegativeButton("No", object : DialogInterface.OnClickListener {

                                override fun onClick(dialog: DialogInterface, which: Int) {

                                    dialog.dismiss()
                                }
                            })


                            alert.show()
                        }


                        R.id.play_playlist ->
                        {
                            forPlaySong(nameofplaylist)
                        }

                        R.id.Shuffle_all_playlist ->
                        {
                            if(helper.getalldata_playlist(nameofplaylist).size>0)
                            {
                                var base: Song_base =  helper.getalldata_playlist(nameofplaylist).get(0)
                                Home.shared.edit().putBoolean("shuffle",true).apply()
                                val actual_song_pos = Home.Songname_position.get(base.song_name)!!
                                Constants.mediaAfterprepared(null,context,base,actual_song_pos,0,"general",
                                        "popup_playlist",nameofplaylist)
                            }
                            else
                            {
                                Toast.makeText(context,"Playlist is Empty",Toast.LENGTH_SHORT).show()
                            }

                        }

                        R.id.add_to_queue ->
                        {
                            var base =  helper.getalldata_playlist(nameofplaylist)
                            if(base.size>0)
                            {
                                for(temp in base)
                                {
                                    Constants.SONGS_LIST.add(Pair(temp,Home.Songname_position.get(temp.song_name)!!))
                                }
                                Home.shared.edit().putString("current_album","mixup").apply()
                            }
                        }

                        R.id.play_next ->{

                            var base =  helper.getalldata_playlist(nameofplaylist)
                            if(base.size>0)
                            {
                                for(temp in base)
                                {
                                    Constants.SONGS_LIST.add(Constants.SONG_NUMBER+1,Pair(temp,Home.Songname_position.get(temp.song_name)!!))
                                }
                                Home.shared.edit().putString("current_album","mixup").apply()
                            }
                            else
                            {
                                Toast.makeText(context,"Playlist is Empty",Toast.LENGTH_SHORT).show()
                            }

                        }


                    }
                    return true
                }
            })

            popup.show()
        }


        return v
    }

    private fun forPlaySong(nameofplaylist:String)
    {

        var base =  Home().helper?.getalldata_playlist(nameofplaylist)
        if(base != null && base!!.size>0)
        {
            val actual_song_pos = Home.Songname_position.get(base.get(0).song_name)!!
            Constants.servicearray("popup_playlist",base.get(0).album_name,base.get(0).artist,nameofplaylist,false,context)

            var messagearg:String = ""
            if("popup_playlist".equals(Home.shared.getString("current_album","alb"),ignoreCase = true))
            {
                if(Home.shared.getString("playlist","alb").equals(nameofplaylist,ignoreCase = true))
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

            Constants.mediaAfterprepared(null,context,base.get(0),actual_song_pos,0,"general",
                    "popup_playlist",nameofplaylist)
            Constants.SONG_NUMBER = 0

            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context!!)
            if (!isServiceRunning)
            {
                val i = Intent(context, SongService::class.java)
                context!!.startService(i)


            } else {

                Constants.SONG_CHANGE_HANDLER!!.sendMessage(Constants.SONG_CHANGE_HANDLER!!.obtainMessage(0,messagearg));

            }

            Home().cardview?.visibility = View.VISIBLE

        }
        else
        {
            Toast.makeText(context,"Playlist is Empty",Toast.LENGTH_SHORT).show()
        }


    }
}
