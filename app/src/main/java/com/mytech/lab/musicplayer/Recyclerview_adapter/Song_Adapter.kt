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

    //    Longclick longclick;


//    override fun getSectionText(position: Int): String {
//        return song_info[position].song_name.substring(0, 1)
//    }


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
            Log.i("LOG"," CLICK")
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

//    fun playwithpopmenu(pop: LinearLayout, temp: Song_base, position: Int)
//    {
//
//        val popup = PopupMenu(context, pop)
//        Log.i("ONCLICK"," das")
//        popup.inflate(R.menu.pop_menu_song)
//
//        var inflater1:LayoutInflater? = null
//        inflater1 = LayoutInflater.from(context)
//
//        if(Build.VERSION.SDK_INT>=23)
//        {
//            popup.gravity = Gravity.END
//        }
//
//
//        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
//
//            override fun onMenuItemClick(item: MenuItem): Boolean {
//                when (item.getItemId()) {
//
//                    R.id.play ->
//                    {
//                        Constants.SONGS_LIST.clear()
//                        Constants.SONGS_LIST.add(Pair(temp,position))
//                        Constants.SONG_NUMBER = 0
//                        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context)
//                        if (!isServiceRunning)
//                        {
//                            val i = Intent(context, SongService::class.java)
//                            context.startService(i)
//                        }
//                        else
//                        {
//                            Constants.SONG_CHANGE_HANDLER!!.sendMessage(Constants.SONG_CHANGE_HANDLER!!.obtainMessage(0,"false"))
//                        }
//
//                        Home.cardview.visibility = View.VISIBLE
//
//                    }
//
//                    R.id.play_next ->
//                    {
//                        if(Constants.SONGS_LIST.size!=0)
//                        {
//                            Constants.SONGS_LIST.add(Constants.SONG_NUMBER+1,Pair(temp,position))
//                            Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show()
//                        }
//
//                    }
//
//                    R.id.add_to_queue ->
//                    {
//                        if(Constants.SONGS_LIST.size!=0)
//                        {
//                            Constants.SONGS_LIST.add(Pair(temp, position))
//                            Toast.makeText(context, "Successfully Added to Queue", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    R.id.delete ->
//                    {
//                        val state = Environment.getExternalStorageState()
//
//
//                        val file = File(temp.url)
//                        var dialog = AlertDialog.Builder(context).create()
//
//                        dialog.setMessage("Are you sure you want to delete this song permanently")
//                        dialog.setTitle("Confirm Delete")
//                        dialog.setCancelable(true)
//                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialogInterface, i ->
//
//
//                            val deleted = file.delete()
//                            if (deleted) {
//                                try
//                                {
//                                    val actual_song_pos = Home.shared.getInt("actual_song_position",-1)
//                                    if (Home.all_songs.get(actual_song_pos).song_name.equals(temp.song_name)) {
//                                        Controls.nextControl(context)
//                                    }
//                                }
//                                catch (e:Exception){}
//
//                                song_info.removeAt(position)
//                                Home.all_songs.removeAt(position)
//                                Songs.adapter?.notifyItemRangeChanged(position,song_info.size)
////                                recyclerView.notifyItemRemoved(position)
////                                notifyItemRangeChanged(position, all_songs?.size)
//                                Toast.makeText(context, "Successfully Deleted", Toast.LENGTH_SHORT).show()
//
//
//
//                            } else
//                            {
//                                Toast.makeText(context, "Permission denied Delete song from file manager", Toast.LENGTH_SHORT).show()
//
//                            }
//                            dialog.dismiss()
//                        })
//                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No",{
//                            dialogInterface, i ->
//                            dialog.dismiss()
//
//                        })
//                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
//                        dialog.show()
//                    }
//
//                    R.id.send ->
//                    {
//                        val sharingIntent = Intent(Intent.ACTION_SEND)
//                        val screenshotUri = Uri.parse(temp.url)
//
//                        sharingIntent.type = "*/*"
//                        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
//                        context.startActivity(Intent.createChooser(sharingIntent, "Share image using"))
//
//
//                    }
//
//                    R.id.set_ringtone ->
//                    {
//                        val f = File(temp.url)
//                        if (f.exists()) {
//                            val values = ContentValues()
//                            values.put(MediaStore.MediaColumns.DATA, temp.url)
//                            values.put(MediaStore.MediaColumns.TITLE, "ring")
//                            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
//                            values.put(MediaStore.MediaColumns.SIZE, f.length())
//                            values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name)
//                            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
//                            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
//                            values.put(MediaStore.Audio.Media.IS_ALARM, true)
//                            values.put(MediaStore.Audio.Media.IS_MUSIC, false)
//
//                            val uri = MediaStore.Audio.Media.getContentUriForPath(f
//                                    .absolutePath)
//                            context.contentResolver.delete(
//                                    uri,
//                                    MediaStore.MediaColumns.DATA + "=\""
//                                            + temp.url + "\"", null)
//                            val newUri = context.contentResolver.insert(uri, values)
//
//                            try {
//                                RingtoneManager.setActualDefaultRingtoneUri(
//                                        context, RingtoneManager.TYPE_RINGTONE,
//                                        newUri)
//                                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
//                            } catch (t: Throwable) {
//                                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
//                                t.printStackTrace()
//                            }
//
//                        }
//
//
//                    }
//
//                    R.id.add_to_playlistt ->
//                    {
//
//                        helper = DatabaseHelperAdapter(context)
//                        val detail = AlertDialog.Builder(context)
//                        val dt = inflater1.inflate(R.layout.dialog_all_playlist, null)
//                        detail.setView(dt)
//                        val all_playlist = detail.create()
//
//                        val lis: ListView = dt.findViewById(R.id.all_playlist)
//
//                        val all_lis:ArrayList<String> = helper.fetchdistinctplaylist()
//
//                        var all_list_array:ArrayAdapter<String> = ArrayAdapter(context,android.R.layout.simple_expandable_list_item_1,all_lis)
//
//                        lis.adapter = all_list_array
//
//                        lis.setOnItemClickListener { adapterView, view, i, l ->
//
//                            var single_lis_name:String = adapterView.getItemAtPosition(i).toString()
//
//
//                            if(helper.checkexistance_of_song_in_playlist(temp.song_name,single_lis_name)>0)
//                            {
//                                Toast.makeText(context,"Song already exists",Toast.LENGTH_SHORT).show()
//                            }
//                            else
//                            {
//                                var check:Long = helper.insert_in_any_table(temp.song_name,temp.artist,temp.url,temp.albumId.toString(),temp.album_name,position,temp.duration,0,"Playlist",single_lis_name)
//
//                                if(check>0)
//                                {
//                                    Playlist().notifychange()
//                                    Toast.makeText(context, "1 song added to $single_lis_name Playlist", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//
//                            all_playlist.dismiss()
//
//                        }
//
////                            all_playlist.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
//                        all_playlist.show()
//                    }
//
//                    R.id.detail ->
//                    {
//                        try {
//                            val detail = AlertDialog.Builder(context)
//                            val dt = inflater1.inflate(R.layout.dialog_detail, null)
//                            detail.setView(dt)
//                            val sub_detail = detail.create()
//
//                            val heading = dt.findViewById<TextView>(R.id.heading)
//                            val title = dt.findViewById<TextView>(R.id.title_value)
//                            val artist = dt.findViewById<TextView>(R.id.artist_value)
//                            val album = dt.findViewById<TextView>(R.id.album_value)
//                            val composer = dt.findViewById<TextView>(R.id.composer_value)
//                            val year = dt.findViewById<TextView>(R.id.year_value)
//                            val location = dt.findViewById<TextView>(R.id.location_value)
//
//                            heading.text = temp.song_name
//                            title.text = temp.song_name
//                            artist.text = temp.artist
//                            album.text = temp.album_name
//                            composer.text = temp.composer
//                            year.text = temp.year.toString()
//                            location.text = temp.url
//                            sub_detail.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
//                            sub_detail.show()
//                        }
//                        catch (e:Exception){Toast.makeText(context,"Detail dialog not working ",Toast.LENGTH_SHORT).show()}
//                    }
//
//                    R.id.add_favrioute ->
//                    {
//
//                        if(Home.helper.checkexists_for_song_in_table(temp.song_name,"favourites")<=0)
//                        {
//                            val position = Home.Songname_position.get(temp.song_name)!!
//                            var check:Long = Home.helper.insert_in_any_table(temp.song_name,temp.artist,temp.url,temp.albumId.toString(),temp.album_name,position,temp.duration,2,"favourites",null)
//
//                            if(check>0)
//                            {
//                                Playlist_single.notify_change()
//                                Toast.makeText(context, "1 song added to Favourite Song", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                        else
//                        {
//                            Toast.makeText(context,"Already Added",Toast.LENGTH_SHORT).show()
//                        }
//
//                    }
//
//                    R.id.search ->
//                    {
//
//                        if(Constants.isNetworkConnected(context))
//                        {
//                            var theWebPage:WebView =  WebView(context)
//                            theWebPage.getSettings().setJavaScriptEnabled(true)
//                            val temp = temp.song_name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                            theWebPage.loadUrl("https://www.google.co.in/search?q= "+temp.joinToString("+"))
//                        }
//                        else
//                        {
//                            Toast.makeText(context,"Network not available",Toast.LENGTH_SHORT).show()
//                        }
//
//                    }
//
//
//                }
//                return true
//            }
//
//
//
//        })
//
//        popup.show()
//    }


}
