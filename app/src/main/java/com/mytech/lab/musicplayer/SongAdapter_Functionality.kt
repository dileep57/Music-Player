package com.mytech.lab.musicplayer

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Fragments.Recent_song
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.Sub_Fragments.Playlist
import com.mytech.lab.musicplayer.sub_sub_fragment.Playlist_single
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import java.io.File
import android.support.v4.content.ContextCompat.startActivity


class SongAdapter_Functionality(var pop: LinearLayout, var temp: Song_base, var position: Int,var cntxt:Context) {

    fun play()
    {
        Constants.SONGS_LIST.clear()
        Constants.SONGS_LIST.add(Pair(temp,position))
        Constants.SONG_NUMBER = 0
        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), cntxt)
        if (!isServiceRunning)
        {
            val i = Intent(cntxt, SongService::class.java)
            cntxt.startService(i)
        }
        else
        {
            Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,"false"))
        }

        Home().cardview?.visibility = View.VISIBLE

        Home.shared.edit().putString("current_album","mixed").apply()
    }

    fun play_next()
    {
        if(Constants.SONGS_LIST.size!=0)
        {
            Constants.SONGS_LIST.add(Constants.SONG_NUMBER+1,Pair(temp,position))
            Toast.makeText(cntxt,"Success", Toast.LENGTH_SHORT).show()
        }

        Home.shared.edit().putString("current_album","mixed").apply()
    }

    fun addToQueue()
    {
        if(Constants.SONGS_LIST.size!=0)
        {
            Constants.SONGS_LIST.add(Pair(temp, position))
            Toast.makeText(cntxt, "Successfully Added to Queue", Toast.LENGTH_SHORT).show()
        }
    }

    fun delete(adapter:Song_Adapter?=null,song_array:ArrayList<Song_base>?=null)
    {
        val state = Environment.getExternalStorageState()
        val file = File(temp.url)
        var dialog = AlertDialog.Builder(cntxt).create()
//        Log.i("File to delete",temp.song_name)
//        Log.i("Song_name ",Home.shared.getString("song_name","name"))
        dialog.setMessage("Are you sure you want to delete this song permanently")
        dialog.setTitle("Confirm Delete")
        dialog.setCancelable(true)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", { dialogInterface, i ->

            val deleted = file.delete()
            if (deleted) {
                try
                {
//                    val actual_song_pos = Home.shared.getInt("actual_song_position",-1)
                    if (Home.shared.getString("song_name","name").equals(temp.song_name)) {
                        Controls.nextControl(cntxt)
                    }
                }

                catch (e:Exception){Home.filenotsupport(cntxt)}

                song_array?.removeAt(position)
                Home.all_songs.removeAt(position)
                adapter?.notifyItemRemoved(position)
                if(position==null)
                {
//                    Log.i("DELETE","position null")
                }
                if(song_array==null)
                {
//                    Log.i("DELETE","array null")
                }
                adapter?.notifyItemRangeChanged(position,song_array!!.size)
                if(cntxt!=null)
                {
                    Toast.makeText(cntxt, "Successfully Deleted", Toast.LENGTH_SHORT).show()
//                    Log.i("DELETE","Success")
                }

                Refresh_song().execute(temp.song_name)



            } else
            {
                Toast.makeText(cntxt, "Permission denied Delete song from file manager", Toast.LENGTH_SHORT).show()

            }
            dialog.dismiss()
        })
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"No",{
            dialogInterface, i ->
            dialog.dismiss()

        })
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
        dialog.show()
    }

    fun send()
    {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val screenshotUri = Uri.parse(temp.url)

        sharingIntent.type = "*/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
        cntxt.startActivity(Intent.createChooser(sharingIntent, "Share image using"))
    }

    fun setRingtone()
    {
        val f = File(temp.url)
        if (f.exists()) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, temp.url)
            values.put(MediaStore.MediaColumns.TITLE, "ring")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            values.put(MediaStore.MediaColumns.SIZE, f.length())
            values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name)
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
            values.put(MediaStore.Audio.Media.IS_ALARM, true)
            values.put(MediaStore.Audio.Media.IS_MUSIC, false)

            val uri = MediaStore.Audio.Media.getContentUriForPath(f
                    .absolutePath)
            cntxt.contentResolver.delete(
                    uri,
                    MediaStore.MediaColumns.DATA + "=\""
                            + temp.url + "\"", null)
            val newUri = cntxt.contentResolver.insert(uri, values)

            try {
                RingtoneManager.setActualDefaultRingtoneUri(
                        cntxt, RingtoneManager.TYPE_RINGTONE,
                        newUri)
                Toast.makeText(cntxt, "Success", Toast.LENGTH_SHORT).show()
            } catch (t: Throwable) {
                Toast.makeText(cntxt, "Failure", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }

        }
    }

    fun removeFromPlaylist(songName:String,playListName:String,song_adapter:Song_Adapter?=null,playlist_ar:ArrayList<Song_base>?=null)
    {
        Home().helper?.deletesong_for_playlist(songName,playListName)
        playlist_ar!!.removeAt(position)
        song_adapter?.notifyItemRemoved(position)
        song_adapter?.notifyItemRangeChanged(position,playlist_ar.size)
    }

    fun addToPlaylist(inflater1:LayoutInflater)
    {
        if(Home().helper!!.fetchdistinctplaylist().size==0)
        {
            Toast.makeText(cntxt,"No Playlist",Toast.LENGTH_SHORT).show()
            return
        }

        val detail = AlertDialog.Builder(cntxt)
        val dt = inflater1.inflate(R.layout.dialog_all_playlist, null)
        detail.setView(dt)
        val all_playlist = detail.create()

        val lis: ListView = dt.findViewById(R.id.all_playlist)

        val all_lis:ArrayList<String> = Home().helper!!.fetchdistinctplaylist()

        var all_list_array: ArrayAdapter<String> = ArrayAdapter(cntxt,android.R.layout.simple_expandable_list_item_1,all_lis)

        lis.adapter = all_list_array

        lis.setOnItemClickListener { adapterView, view, i, l ->

            var single_lis_name:String = adapterView.getItemAtPosition(i).toString()


            if(Home().helper!!.checkexistance_of_song_in_playlist(temp.song_name,single_lis_name)>0)
            {
                Toast.makeText(cntxt,"Song already exists",Toast.LENGTH_SHORT).show()
            }
            else
            {
                var check:Long = Home().helper!!.insert_in_any_table(temp.song_name,temp.artist,temp.url,temp.albumId.toString(),temp.album_name,position,temp.duration,0,"Playlist",single_lis_name)

                if(check>0)
                {
                    Playlist().notifychange()
                    Toast.makeText(cntxt, "1 song added to $single_lis_name Playlist", Toast.LENGTH_SHORT).show()
                }
            }

            all_playlist.dismiss()

        }

//                            all_playlist.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
        all_playlist.show()
    }

    fun detail(inflater1:LayoutInflater)
    {
        try {
            val detail = AlertDialog.Builder(cntxt)
            val dt = inflater1.inflate(R.layout.dialog_detail, null)
            detail.setView(dt)
            val sub_detail = detail.create()

            val heading = dt.findViewById<TextView>(R.id.heading)
            val title = dt.findViewById<TextView>(R.id.title_value)
            val artist = dt.findViewById<TextView>(R.id.artist_value)
            val album = dt.findViewById<TextView>(R.id.album_value)
            val composer = dt.findViewById<TextView>(R.id.composer_value)
            val year = dt.findViewById<TextView>(R.id.year_value)
            val location = dt.findViewById<TextView>(R.id.location_value)

            heading.text = temp.song_name
            title.text = temp.song_name
            artist.text = temp.artist
            album.text = temp.album_name
            composer.text = temp.composer
            year.text = temp.year.toString()
            location.text = temp.url
            sub_detail.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation
            sub_detail.show()
        }
        catch (e:Exception){Toast.makeText(cntxt,"Detail dialog not working ",Toast.LENGTH_SHORT).show()}
    }

    fun addToFavrioute()
    {
        if(Home().helper!!.checkexists_for_song_in_table(temp.song_name,"favourites")<=0)
        {
            val position = Home.Songname_position.get(temp.song_name)!!
            var check:Long = Home().helper!!.insert_in_any_table(temp.song_name,temp.artist,temp.url,temp.albumId.toString(),temp.album_name,position,temp.duration,2,"favourites",null)

            if(check>0)
            {
                Playlist_single.notify_change()
                Toast.makeText(cntxt, "1 song added to Favourite Song", Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(cntxt,"Already Added",Toast.LENGTH_SHORT).show()
        }
    }

    fun search()
    {
        if(Constants.isNetworkConnected(cntxt))
        {
            val temp = temp.song_name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://www.google.co.in/search?q= "+temp.joinToString("+"))
            startActivity(cntxt,i,null)
        }
        else
        {
            Toast.makeText(cntxt,"Network not available",Toast.LENGTH_SHORT).show()
        }
    }



    private inner class Refresh_song : AsyncTask<String, Void, Boolean>() {

        private var delete_song_name: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        override fun doInBackground(vararg param: String): Boolean? {
            delete_song_name = param[0]
            return false
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
            val temp_name = Home.shared.getString("song_name", "alb")

            val edit = Home.shared.edit()

            try{ edit.putInt("actual_song_position", Home.Songname_position.get(Home.shared.getString("song_name","alb"))!!).apply()}catch (e:Exception){e.printStackTrace()}

            try{ delete_from_RecentSong(delete_song_name!!) }catch (e:Exception){e.printStackTrace()}

            try{ delete_from_all_playlist(delete_song_name!!) }catch (e:Exception){e.printStackTrace()}

        }
    }
    private fun delete_from_RecentSong(delete_song_name:String)
    {
        if (Home().helper!!.checkexists_for_song_in_table(delete_song_name,"RecentSong") > 0)
        {
            Home().helper?.deletesong_for_table(delete_song_name,"RecentSong")

            val lis = Home().helper!!.getalldata_table("RecentSong")

            for (s in lis) {
                Home().helper?.update_position(s.song_name, Home.Songname_position.get(s.song_name)!!)
            }
            Recent_song.updaterecentsong(cntxt)
        }
//        helper.closedatabase(null)
    }


    private fun delete_from_all_playlist(delete_song_name:String)
    {
        for(playlist_name in Home().helper!!.fetchdistinctplaylist())
        {
            if (Home().helper!!.checkexistance_of_song_in_playlist(delete_song_name,playlist_name) > 0)
            {
                Home().helper!!.deletesong_for_playlist(delete_song_name,playlist_name)

                val lis = Home().helper!!.getalldata_playlist(playlist_name)

                for (s:Song_base in lis)
                {
                    Home().helper!!.update_position(s.song_name, Home.Songname_position.get(s.song_name)!!)
                }

            }

        }

        if (Home().helper!!.checkexists_for_song_in_table(delete_song_name, "favourites") > 0)
        {
            Home().helper!!.deletesong_for_table(delete_song_name, "favourites")
        }
//        helper.closedatabase(null)

        Playlist().notifychange()
    }
}