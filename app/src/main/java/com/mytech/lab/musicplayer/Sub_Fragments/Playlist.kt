package com.mytech.lab.musicplayer.Sub_Fragments

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.ListView_Adapter.Playlist_adapter
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.SongService
import com.mytech.lab.musicplayer.sub_sub_fragment.Playlist_single
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base


/**
 * Created by lnx on 26/2/18.
 */

class Playlist : Fragment() {

    lateinit var helper : DatabaseHelperAdapter;
    internal var context: Context? = null
    lateinit var recent_add_song:LinearLayout
    lateinit var favuorites:LinearLayout
    lateinit var fab:FloatingActionButton
    var fav_side_popup:LinearLayout? = null
    var recent_side_popup:LinearLayout? = null


    lateinit var mylist:ListView
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.context = context
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var v : View =  inflater.inflate(R.layout.sub_fragment_playlist, container, false)

        getview(v)

        playlist_name_array = helper.fetchdistinctplaylist()
        if(playlist_name_array.contains("favourites"))
        {
            playlist_name_array.remove("favourites")
        }

        fab.setOnClickListener {
            addplaylist()
        }

        var tsk:Downloadtsk = Downloadtsk()
        tsk.execute()

        recent_add_song.setOnClickListener {
                open_frag("Recent_add_song")
        }

        recent_side_popup?.setOnClickListener {

                val popup = PopupMenu(context, recent_side_popup)

                popup.inflate(R.menu.popup_playlist)

                popup.menu.findItem(R.id.delete_playlist).isVisible = false

                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {


                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.getItemId()) {

                            R.id.play_playlist ->
                            {
                                forrecent_and_favourite_song("Recent_add_song")
                            }

                            R.id.Shuffle_all_playlist ->
                            {
                                Home.shared.edit().putBoolean("shuffle",true).apply()
                                forrecent_and_favourite_song("Recent_add_song")
                            }

                            R.id.add_to_queue ->
                            {
                                var base =  Constants.get_topsong(context!!, MediaStore.Audio.Media.DATE_ADDED + " DESC")
                                if(base.size>0)
                                {
                                    for(temp in base)
                                    {
                                        Constants.SONGS_LIST.add(Pair(temp,Home.Songname_position.get(temp.song_name)!!))
                                    }
                                }
                            }

                            R.id.play_next ->{

                                var base =  Constants.get_topsong(context!!, MediaStore.Audio.Media.DATE_ADDED+" DESC")
                                if(base.size>0)
                                {
                                    for(temp in base)
                                    {
                                        Constants.SONGS_LIST.add(Constants.SONG_NUMBER+1,Pair(temp,Home.Songname_position.get(temp.song_name)!!))
                                    }
                                }
                                Home.shared.edit().putString("current_album","mixup").apply()
                            }

                        }

                        return true
                    }
                })

                popup.show()
        }



        favuorites.setOnClickListener {
             open_frag("favourites")
        }

        fav_side_popup?.setOnClickListener {

            val popup = PopupMenu(context, fav_side_popup)

            popup.inflate(R.menu.popup_playlist)
            popup.menu.findItem(R.id.delete_playlist).isVisible = false

            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {


                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.getItemId()) {

                        R.id.play_playlist ->
                        {
                            forrecent_and_favourite_song("favourites")
                        }

                        R.id.Shuffle_all_playlist ->
                        {

                            if(helper.getalldata_table("favourites").size>0)
                            {
                                Home.shared.edit().putBoolean("shuffle",true).apply()
                                forrecent_and_favourite_song("favourites")
                            }
                            else
                            {
                                Toast.makeText(context,"Playlist is Empty",Toast.LENGTH_SHORT).show()
                            }


                        }

                        R.id.add_to_queue ->
                        {
                            var base =  helper.getalldata_table("favourites")
                            if(base.size>0)
                            {
                                for(temp in base)
                                {
                                    Constants.SONGS_LIST.add(Pair(temp,Home.Songname_position.get(temp.song_name)!!))
                                }
                            }
                            else
                            {
                                Toast.makeText(context,"Playlist is Empty",Toast.LENGTH_SHORT).show()
                            }
                        }

                        R.id.play_next ->{

                            var base =  helper.getalldata_table("favourites")
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


        mylist.setOnItemClickListener { adapterView, view, i, l ->
            val bundle = Bundle()
            bundle.putString("name", playlist_name_array.get(i).toString())
            val frag = Playlist_single()
            frag.arguments = bundle
            val manager = activity!!.supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.root_frame_playlist, frag)
            transaction.addToBackStack("playlist")
            transaction.commit()

        }

        return v;

    }

    private fun forrecent_and_favourite_song(table_name:String)
    {
        var base:ArrayList<Song_base>? = null
        if(table_name.equals("Recent_add_song"))
        {
             base =  Constants.get_topsong(context!!, MediaStore.Audio.Media.DATE_ADDED + " DESC")
        }
        else
        {
             base =  helper.getalldata_table(table_name)
        }

        if(base.size>0)
        {
            val actual_song_pos = Home.Songname_position.get(base.get(0).song_name)!!
            Constants.servicearray("popup_playlist",base.get(0).album_name,base.get(0).artist,table_name,false,context)

            var messagearg:String = ""
            if("popup_playlist".equals(Home.shared.getString("current_album","alb"),ignoreCase = true))
            {
                if(Home.shared.getString("playlist","alb").equals(table_name,ignoreCase = true))
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
                    "popup_playlist",table_name)
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




    private fun open_frag(nameof_frag:String)
    {
        val bundle = Bundle()
        bundle.putString("name", nameof_frag)
        val frag = Playlist_single()
        frag.arguments = bundle
        val manager = activity!!.supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.root_frame_playlist, frag)
        transaction.addToBackStack("playlist_expand")
        transaction.commit()

    }


    fun addplaylist()
    {
        lateinit var dg: AlertDialog
        var isvalid : Boolean
        val dialog = AlertDialog.Builder(context!!)
        val dp = layoutInflater.inflate(R.layout.dialog_playlist, null)
        dialog.setView(dp)
        dg = dialog.create()
        dg.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
        dg.show()

        var create : TextView = dp.findViewById(R.id.create)

        var cancel : TextView = dp.findViewById(R.id.cancel)

        var name : EditText = dp.findViewById(R.id.playlist_name)

        var textinput : TextInputLayout = dp.findViewById(R.id.username_textinputlayout);

        create.setOnClickListener {
            if(name.getText().toString().isEmpty()) {
                textinput.setError("PlayList Name Is Mandatory")
                isvalid = false;

            } else {
                textinput.setErrorEnabled(false);
                isvalid = true;
            }

            if(isvalid)
            {

                var temp: Long = 0
                if(name.text.toString().toLowerCase()=="favourites")
                {
                    playlist_already_exists()
                    return@setOnClickListener
                }
                if(helper.checkexist_for_playlist(playlist_nme = name.text.toString().toLowerCase())==0)
                {
                    temp = helper.insert_in_any_table("sample","sample","sample","sample","sample",-1,"0",0,"Playlist",name.text.toString().toLowerCase())
                    Playlist.playlist_name_array.add(name.text.toString())
                    Playlist().notifychange()

                } else {
                   playlist_already_exists()
                }

                if(temp>0)
                {
                    Toast.makeText(context, "Playlist Created", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(context, "Problem Occur ", Toast.LENGTH_SHORT).show()
                }

                dg.dismiss()
            }


        }

        cancel.setOnClickListener {
            dg.dismiss()
        }

        Playlist().notifychange()

    }

    private fun playlist_already_exists()
    {
        Toast.makeText(context, "Playlist already exists", Toast.LENGTH_SHORT).show()
    }

    inner class Downloadtsk : AsyncTask<Void, Void, Boolean>()
    {

        override fun doInBackground(vararg p0: Void?): Boolean {
            adapter = Playlist_adapter(context!!, playlist_name_array)
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            mylist.adapter = adapter

        }

    }

    companion object {

        var adapter:ArrayAdapter<String>? = null
        lateinit var playlist_name_array:ArrayList<String>

    }

    fun notifychange() {
        adapter?.notifyDataSetChanged()
        Playlist_single().notify_change()
    }

    private fun getview(v:View)
    {
        mylist = v.findViewById(R.id.lis)
        fav_side_popup = v.findViewById(R.id.fav_side_popup)
        recent_side_popup = v.findViewById(R.id.recent_side_popup)
        helper = DatabaseHelperAdapter(v.context)

        recent_add_song = v.findViewById(R.id.recent_add_song_playlist)

        favuorites = v.findViewById(R.id.favourites)
        fab = v.findViewById(R.id.fab_icon)
    }

    override fun onResume() {
        super.onResume()
        notifychange()

    }





}


