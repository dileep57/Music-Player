@file:JvmName("Constants")
package com.mytech.lab.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import com.mytech.lab.musicplayer.Fragments.Recent_song

import java.util.Random
import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import java.io.FileDescriptor
import android.net.ConnectivityManager
import android.provider.MediaStore
import android.widget.Toast
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Activity.Wel
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import java.io.File





/**
 * Created by lnx on 25/2/18.
 */

class Constants
{

companion object {


    var tint = "hello"
    val coloHexlist = arrayListOf<String>("#0000FF", "#8A2BE2", "#A52A2A", "#5F9EA0", "#D2691E",
            "#6495ED", "#DC143C", "#00008B", "#008B8B", "#A9A9A9", "#A9A9A9", "#006400",
            "#8B008B", "#FF8C00", "#9932CC", "#8B0000", "#483D8B", "#2F4F4F", "#2F4F4F",
            "#9400D3", "#FF1493", "#696969", "#1E90FF", "#B22222", "#228B22", "#FF00FF",
            "#DAA520", "#808080", "#008000", "#ADFF2F", "#FF69B4", "#CD5C5C", "#4B0082",
            "#D3D3D3", "#90EE90", "#FFB6C1", "#FFA07A", "#20B2AA", "#87CEFA", "#778899",
            "#778899", "#00FF00", "#32CD32", "#FF00FF", "#800000", "#0000CD", "#BA55D3",
            "#9370DB", "#3CB371", "#7B68EE", "#00FA9A", "#C71585", "#191970", "#000080",
            "#FFA500", "#FF4500", "#DA70D6", "#DB7093", "#FFC0CB", "#DDA0DD", "#800080",
            "#663399", "#FF0000", "#4169E1", "#2E8B57", "#C0C0C0", "#6A5ACD", "#708090",
            "#00FF7F", "#4682B4", "#008080", "#FF6347", "#EE82EE", "#FFFF00", "#9ACD32","#3C515C")

    fun getMediaPlayer(context: Context): MediaPlayer {

        val mediaplayer = MediaPlayer()

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return mediaplayer
        }

        try {
            val cMediaTimeProvider = Class.forName("android.media.MediaTimeProvider")
            val cSubtitleController = Class.forName("android.media.SubtitleController")
            val iSubtitleControllerAnchor = Class.forName("android.media.SubtitleController\$Anchor")
            val iSubtitleControllerListener = Class.forName("android.media.SubtitleController\$Listener")

            val constructor = cSubtitleController.getConstructor(*arrayOf(Context::class.java, cMediaTimeProvider, iSubtitleControllerListener))

            val subtitleInstance = constructor.newInstance(context, null, null)

            val f = cSubtitleController.getDeclaredField("mHandler")

            f.isAccessible = true
            try {
                f.set(subtitleInstance, Handler())
            } catch (e: IllegalAccessException) {
                return mediaplayer
            } finally {
                f.isAccessible = false
            }

            val setsubtitleanchor = mediaplayer.javaClass.getMethod("setSubtitleAnchor", cSubtitleController, iSubtitleControllerAnchor)

            setsubtitleanchor.invoke(mediaplayer, subtitleInstance, null)

        } catch (e: Exception) {
        }

        return mediaplayer
    }

    fun databasedata(s: Song_base, cntx: Context, song_position: Int, table_name: String) {

        val helper = DatabaseHelperAdapter(cntx)
        if (table_name.equals("RecentSong")) {
            if (helper.checkexists_for_song_in_table(s.song_name, table_name) > 0) {
                var count = helper.checkexists_for_song_in_table(s.song_name, table_name)
                ++count
                helper.updatecount(s.song_name, count)

            } else {
                if (helper.countalldata_in_any_table(table_name) >= 20) {
                    helper.deletelastrow()
                }

                helper.insert_in_any_table(s.song_name, s.artist, s.url, s.albumId.toString(), s.album_name, song_position, s.duration, 1, table_name)

            }
        } else if (table_name == "playlist_name") {

        }

     helper.closedatabase()
    }

    fun calculatetime(minisec: Int?): String {
        val minute = minisec!! / 60 / 1000
        val sec = minisec / 1000 - minisec / 60 / 1000 * 60
        var minstr = Integer.toString(minute)
        var secstr = Integer.toString(sec)

        if (minstr.length == 1) {
            minstr = "0" + minstr
        }
        if (secstr.length == 1) {
            secstr = "0" + secstr
        }

        return minstr + ":" + secstr


    }

    @JvmOverloads
    fun setsharedpreference(s: Song_base, song_type: String, actual_song_pos: Int, sub_song_pos: Int, shuffle: Boolean, repeat: Boolean, current_album: String, playlist_name: String? = null) {
        val edit = Home.shared.edit()

        edit.putString("song_type", song_type)
        edit.putString("song_name", s.song_name)
        edit.putString("artist_name", s.artist)
        edit.putString("album_name", s.album_name)
        edit.putLong("duration",s.duration.toLong())

        edit.putInt("actual_song_position", actual_song_pos)
        edit.putInt("sub_song_position", sub_song_pos)

        edit.putString("playlist_name", playlist_name)
        edit.putString("current_album", current_album)

        edit.putString("url", s.url)
        edit.putLong("albumid", s.albumId!!)
        edit.putBoolean("shuffle", shuffle)
        edit.putBoolean("repeat", repeat)
        edit.apply()
    }


    fun dontrepeatsong(song_position: Int, player: String?): Int
    {
        val rand = Random()
        var p: Int = 0

        if (player != null && !player.equals("Special_player", ignoreCase = false))
        {
            if (Home.song_array_general.size == 1)
            {
                return 0
            } else
            {
                while (true)
                {
                    p = rand.nextInt(Home.song_array_general.size)
                    if (p != song_position)
                    {

                        break
                    }
                }

                return p
            }
        }

        return p
    }

    fun shuffle_song(position:Int):Int
    {
        val rand = Random()
        var p: Int = 0

        if(p==0 && Constants.SONGS_LIST.size==1)
        {
            return 0
        }

        while(true)
        {
            p = rand.nextInt(Constants.SONGS_LIST.size)
            if (p != position)
            {

                break
            }
        }

        return p
    }




    fun setcolortheme(num: Int) {
        when (num) {
            0 ->  Wel.colorshared.edit().putInt("themename",R.style.Blue).apply()
            1 ->  Wel.colorshared.edit().putInt("themename",R.style.BlueViolet).apply()
            2 ->  Wel.colorshared.edit().putInt("themename",R.style.Brown).apply()
            3 ->  Wel.colorshared.edit().putInt("themename",R.style.CadetBlue).apply()
            4 ->  Wel.colorshared.edit().putInt("themename",R.style.Chocolate).apply()
            5 ->  Wel.colorshared.edit().putInt("themename",R.style.CornflowerBlue).apply()
            6 ->  Wel.colorshared.edit().putInt("themename",R.style.Crimson).apply()
            7 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkBlue).apply()
            8 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkCyan).apply()
            9 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkGray).apply()
            10 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkGrey).apply()
            11 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkGreen).apply()
            12 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkMagenta).apply()
            13 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkOrange).apply()
            14 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkOrchid).apply()
            15 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkRed).apply()
            16 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkSlateBlue).apply()
            17 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkSlateGray).apply()
            18 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkSlateGrey).apply()
            19 ->  Wel.colorshared.edit().putInt("themename",R.style.DarkViolet).apply()
            20 ->  Wel.colorshared.edit().putInt("themename",R.style.DeepPink).apply()
            21 ->  Wel.colorshared.edit().putInt("themename",R.style.DimGray).apply()
            22 ->  Wel.colorshared.edit().putInt("themename",R.style.DodgerBlue).apply()
            23 ->  Wel.colorshared.edit().putInt("themename",R.style.FireBrick).apply()
            24 ->  Wel.colorshared.edit().putInt("themename",R.style.ForestGreen).apply()
            25 ->  Wel.colorshared.edit().putInt("themename",R.style.Fuchsia).apply()
            26 ->  Wel.colorshared.edit().putInt("themename",R.style.GoldenRod).apply()
            27 ->  Wel.colorshared.edit().putInt("themename",R.style.Gray).apply()
            28 ->  Wel.colorshared.edit().putInt("themename",R.style.Green).apply()
            29 ->  Wel.colorshared.edit().putInt("themename",R.style.GreenYellow).apply()
            30 ->  Wel.colorshared.edit().putInt("themename",R.style.HotPink).apply()
            31 ->  Wel.colorshared.edit().putInt("themename",R.style.IndianRed ).apply()
            32 ->  Wel.colorshared.edit().putInt("themename",R.style.Indigo  ).apply()
            33 ->  Wel.colorshared.edit().putInt("themename",R.style.LightGrey).apply()
            34 ->  Wel.colorshared.edit().putInt("themename",R.style.LightGreen).apply()
            35 ->  Wel.colorshared.edit().putInt("themename",R.style.LightPink).apply()
            36 ->  Wel.colorshared.edit().putInt("themename",R.style.LightSalmon).apply()
            37 ->  Wel.colorshared.edit().putInt("themename",R.style.LightSeaGreen).apply()
            38 ->  Wel.colorshared.edit().putInt("themename",R.style.LightSkyBlue).apply()
            39 ->  Wel.colorshared.edit().putInt("themename",R.style.LightSlateGray).apply()
            40 ->  Wel.colorshared.edit().putInt("themename",R.style.LightSlateGrey).apply()
            41 ->  Wel.colorshared.edit().putInt("themename",R.style.Lime).apply()
            42 ->  Wel.colorshared.edit().putInt("themename",R.style.LimeGreen).apply()
            43 ->  Wel.colorshared.edit().putInt("themename",R.style.Magenta).apply()
            44 ->  Wel.colorshared.edit().putInt("themename",R.style.Maroon).apply()
            45 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumBlue).apply()
            46 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumOrchid).apply()
            47 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumPurple).apply()
            48 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumSeaGreen).apply()
            49 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumSlateBlue).apply()
            50 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumSpringGreen).apply()
            51 ->  Wel.colorshared.edit().putInt("themename",R.style.MediumVioletRed).apply()
            52 ->  Wel.colorshared.edit().putInt("themename",R.style.MidnightBlue).apply()
            53 ->  Wel.colorshared.edit().putInt("themename",R.style.Navy).apply()
            54 ->  Wel.colorshared.edit().putInt("themename",R.style.Orange).apply()
            55 ->  Wel.colorshared.edit().putInt("themename",R.style.OrangeRed).apply()
            56 ->  Wel.colorshared.edit().putInt("themename",R.style.Orchid).apply()
            57 ->  Wel.colorshared.edit().putInt("themename",R.style.PaleVioletRed).apply()
            58 ->  Wel.colorshared.edit().putInt("themename",R.style.Pink).apply()
            59 ->  Wel.colorshared.edit().putInt("themename",R.style.Plum).apply()
            60 ->  Wel.colorshared.edit().putInt("themename",R.style.Purple).apply()
            61 ->  Wel.colorshared.edit().putInt("themename",R.style.RebeccaPurple).apply()
            62 ->  Wel.colorshared.edit().putInt("themename",R.style.Red).apply()
            63 ->  Wel.colorshared.edit().putInt("themename",R.style.RoyalBlue).apply()
            64 ->  Wel.colorshared.edit().putInt("themename",R.style.SeaGreen).apply()
            65 ->  Wel.colorshared.edit().putInt("themename",R.style.Silver).apply()
            66 ->  Wel.colorshared.edit().putInt("themename",R.style.SlateBlue).apply()
            67 ->  Wel.colorshared.edit().putInt("themename",R.style.SlateGray).apply()
            68 ->  Wel.colorshared.edit().putInt("themename",R.style.SpringGreen).apply()
            69 ->  Wel.colorshared.edit().putInt("themename",R.style.SteelBlue).apply()
            70 ->  Wel.colorshared.edit().putInt("themename",R.style.Teal).apply()
            71 ->  Wel.colorshared.edit().putInt("themename",R.style.Tomato).apply()
            72 ->  Wel.colorshared.edit().putInt("themename",R.style.Violet).apply()
            73 ->  Wel.colorshared.edit().putInt("themename",R.style.Yellow).apply()
            74 ->  Wel.colorshared.edit().putInt("themename",R.style.YellowGreen).apply()
            75 ->  Wel.colorshared.edit().putInt("themename",R.style.AppFullScreenTheme).apply()
        }
    }

    fun mediaAfterprepared(media:MediaPlayer?=null, context: Context?, s: Song_base,
                           actual_song_pos:Int, local_position:Int, song_type: String,
                           current_album:String, playlist: String="None")
    {
        Constants.setsharedpreference(s,
                song_type,
                actual_song_pos, local_position,
                Home.shared.getBoolean("shuffle",false),
                Home.shared.getBoolean("repeat",false),
                current_album,playlist
        )

        Constants.databasedata(s, context!!, actual_song_pos,"RecentSong")
        Recent_song.updaterecentsong(context)
    }

    fun getDefaultAlbumArt(context: Context): Bitmap? {
        var bm: Bitmap? = null
        val options = BitmapFactory.Options()
        try {
            bm = BitmapFactory.decodeResource(context.resources, R.drawable.music2, options)
        } catch (ee: Error) {
        } catch (e: Exception) {
        }

        return bm
    }

    fun getAlbumart(context: Context, album_id: Long?): Bitmap? {
        var bm: Bitmap? = null
        val options = BitmapFactory.Options()
        try {
            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
            val uri = ContentUris.withAppendedId(sArtworkUri, album_id!!)
            var pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null)
            {
                var fd: FileDescriptor? = pfd.fileDescriptor
                bm = BitmapFactory.decodeFileDescriptor(fd, null, options)
                pfd = null
                fd = null
            }
        } catch (ee: Exception) { }

        return bm
    }

    @JvmOverloads
    fun servicearray(section:String,album_name:String?=null,artist_name:String?=null,playlist_name: String?=null,firstopen:Boolean=false,cntx:Context?=null)
    {

        if(section.equals(Home.shared.getString("current_album","alb"),ignoreCase = true) && !firstopen)
        {
            if(section.equals("artist",ignoreCase = true))
            {
                if(Home.shared.getString("artist_name","alb").equals(artist_name,ignoreCase = true))
                {
                    return
                }
            }
            else if(section.equals("album",ignoreCase = true))
            {
                if(Home.shared.getString("album_name","alb").equals(album_name,ignoreCase = true))
                {
                    return
                }
            }
            else if(section.equals("popup_playlist",ignoreCase = true))
            {
                if(Home.shared.getString("playlist","alb").equals(playlist_name,ignoreCase = true))
                {
                    return
                }
            }
            else
            {
                Log.i("TAGGI ","EXECUTE")
                Log.i("TAGGI ","YES")
                return
            }

        }

        Home.servicearraylist.clear()
        if(section.equals("only_song",ignoreCase = true))
        {

            for(inc in Home.all_songs.indices)
            {
                Home.servicearraylist.add(Pair(Home.all_songs.get(inc),inc))
            }

        }
        else if(section.equals("mini_track",ignoreCase = true))
        {
            for(temp in Home.mini_track)
            {
                Home.servicearraylist.add(Pair(temp.first,temp.second))
            }
        }
        else if(section.equals("popup_playlist",ignoreCase = true))
        {
            if(playlist_name.equals("Recent_add_song",ignoreCase = true))
            {
                for(temp in get_topsong(cntx!!,MediaStore.Audio.Media.DATE_ADDED + " DESC"))
                {
                    try{Home.servicearraylist.add(Pair(temp, Home.Songname_position.get(temp.song_name)!!))}
                    catch (e:Exception){Home.filenotsupport()}
                }
            }
            else if(playlist_name.equals("favourites",ignoreCase = true))
            {
                for(temp in Home.helper.getalldata_table(playlist_name!!))
                {
                    Home.servicearraylist.add(Pair(temp, Home.Songname_position.get(temp.song_name)!!))
                }
            }
            else
            {
                for(temp in Home.helper.getalldata_playlist(playlist_name!!))
                {
                    Home.servicearraylist.add(Pair(temp, Home.Songname_position.get(temp.song_name)!!))
                }
            }

        }
        else if(section.equals("album",ignoreCase = true))
        {
            for(temp in Home.albummap.get(album_name)!!)
            {
                Home.servicearraylist.add(Pair(temp.first,temp.second))
            }
        }
        else if(section.equals("artist",ignoreCase = true))
        {
            for(temp in Home.artistmap.get(artist_name)!!)
            {
                Home.servicearraylist.add(Pair(temp.first,temp.second))
            }
        }
        else if(section.equals("recent",ignoreCase = true))
        {
            val helper = DatabaseHelperAdapter(cntx!!)
            for(temp in helper.getalldata_table("RecentSong"))
            {
                Home.servicearraylist.add(Pair(temp, Home.Songname_position.get(temp.song_name)!!))
            }
        }
        else if(section.equals("folder",ignoreCase = true))
        {

            val song_name = Home.shared.getString("song_name",null)
            val artist_name = Home.shared.getString("artist_name",null)
            val url = Home.shared.getString("url",null)
            val albumId = Home.shared.getLong("albumid",0)
            val act_pos = Home.shared.getInt("actual_song_position",0)
            val album_name = Home.shared.getString("album_name",null)
            val duration = Home.shared.getLong("duration",0)
            val composer = Home.shared.getString("Composer","Composer")

            val s: Song_base = Song_base(song_name, artist_name, url, albumId, album_name, duration.toString(), composer, 0, cntx!!)

            if(Home.Songname_position.get(Home.shared.getString("song_name",null))!=null)
                    {
                        Home.servicearraylist.add(Pair(s,act_pos))
                    }
        }

    }


    fun isServiceRunning(serviceName: String, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName == service.service.className) {
                return true
            }
        }
        return false
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
    }

    fun currentVersionSupportBigNotification(): Boolean {
        val sdkVersion = android.os.Build.VERSION.SDK_INT
        return if (sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            true
        } else false
    }

    fun currentVersionSupportLockScreenControls(): Boolean {
        val sdkVersion = android.os.Build.VERSION.SDK_INT
        return if (sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            true
        } else false
    }

    fun get_topsong(context: Context,sorting_by:String):ArrayList<Song_base> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"

        val topsong = ArrayList<Song_base>()
        val cursor = context.contentResolver.query(uri, null, selection, null, sorting_by)
        var inc = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val song_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val album_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    var composer: String? = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
                    val year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR))

                    val s: Song_base
                    if (composer == null) {
                        composer = "NULL"
                    }

                    val f = File(url)
                    if (f.exists()) {
                        s = Song_base(song_name, artist, url, albumId, album_name, duration, composer, year, context)
                        topsong.add(s)
                    }

                    ++inc
                    if(inc==30)
                        break

                } while (cursor.moveToNext())
            }
            cursor.close()


        }
        return topsong
    }

    fun change_shuffle(context: Context?)
    {
        if (Constants.SONG_SHUFFLE == true) {
            Toast.makeText(context,"Shuffle Off", Toast.LENGTH_SHORT).show()
            Constants.SONG_SHUFFLE = false }

        else {  Constants.SONG_SHUFFLE = true
            Toast.makeText(context,"Shuffle On", Toast.LENGTH_SHORT).show()
            if (Constants.SONG_REPEAT == true) { Constants.SONG_REPEAT = false }
        }
        Controls.shuffle_repeat()
        Home.shared.edit().putBoolean("shuffle", Constants.SONG_SHUFFLE).apply()
        Home.shared.edit().putBoolean("repeat", Constants.SONG_REPEAT).apply()
    }

    fun change_repeat(context: Context?)
    {
        if (Constants.SONG_REPEAT == true)
        {
            Toast.makeText(context,"Repeat Off", Toast.LENGTH_SHORT).show()
            Constants.SONG_REPEAT = false
        }

        else
        {
            Constants.SONG_REPEAT = true
            Toast.makeText(context,"Repeat On", Toast.LENGTH_SHORT).show()
            if (Constants.SONG_SHUFFLE == true) { Constants.SONG_SHUFFLE = false }
        }
        Controls.shuffle_repeat()
        Home.shared.edit().putBoolean("shuffle", Constants.SONG_SHUFFLE).apply()
        Home.shared.edit().putBoolean("repeat", Constants.SONG_REPEAT).apply()
    }

    fun playandpause(context: Context?)
    {
        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context!!)
        if (!isServiceRunning)
        {
            val current = Home.shared.getString("current_album","alb")
            val playlist_name = Home.shared.getString("playlist_name","popup_playlist")
            val album_name = Home.shared.getString("album_name","alb")
            val artist_name = Home.shared.getString("artist_name","alb")
            val sub_song = Home.shared.getInt("sub_song_position",0)
            Constants.servicearray(current,album_name,artist_name,playlist_name,true,context)
            Constants.SONG_NUMBER = sub_song

            val i = Intent(context, SongService::class.java)
            context.startService(i)
        }
        else
        {
            if(Constants.SONG_PAUSED)
            {
                Controls.playPauseControl("play")
            }
            else
            {
                Controls.playPauseControl("pause")
            }
        }
    }

    var SONG_PAUSED = true

    var SONG_SHUFFLE = false

    var SONG_REPEAT = false

    var SONG_NUMBER = 0

    var SONG_CHANGED = false

    var SONG_CHANGE_HANDLER: Handler? = null

    var SHUFFLE_REPEAT: Handler? = null

    var CHECK_SONG_ARRAY_HANDLER: Handler? = null

    var PLAY_PAUSE_HANDLER: Handler? = null

    var PROGRESSBAR_HANDLER: Handler? = null

    var SELF_CHANGE:Boolean = false

    var SONGS_LIST = ArrayList<Pair<Song_base,Int>>()

    @JvmStatic
    val current_directory_array = ArrayList<String>()

    var favourite = false



}

}


