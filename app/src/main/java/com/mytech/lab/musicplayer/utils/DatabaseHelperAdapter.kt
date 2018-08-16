package com.mytech.lab.musicplayer.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


import java.util.ArrayList

/**
 * Created by lnx on 1/3/18.
 */

class DatabaseHelperAdapter(internal var context: Context) {

    internal var database: DataHelper

    init {
        database = DataHelper(context)
    }

    @JvmOverloads
    fun insert_in_any_table(songname: String, artistname: String, url: String, albumid: String, albumname: String, position: Int, duration: String, count: Int? = null,table_name:String,playlist_name: String?=null): Long {
        val db = database.readableDatabase
        val values = ContentValues()
        values.put(DataHelper.SONG_NAME, songname)
        values.put(DataHelper.ARTIST_NAME, artistname)
        values.put(DataHelper.URL, url)
        values.put(DataHelper.ALBUM_ID, albumid)
        values.put(DataHelper.ALBUM_NAME, albumname)
        values.put(DataHelper.POSITION, position)
        values.put(DataHelper.DURATION, duration)

        if(playlist_name!=null)
        {
            values.put(DataHelper.Playlist_name, playlist_name)
        }
        else if(table_name.equals("RecentSong",ignoreCase = true) || table_name.equals("favourites",ignoreCase = true))
        {
            values.put(DataHelper.COUNT, count)
        }

        return db.insert(table_name, null, values)
    }

    fun getalldata_table(table_name:String): ArrayList<Song_base> {
        val db = database.readableDatabase
        val recent_song = ArrayList<Song_base>()
        val cursor = db.query(table_name, null, null, null, null, null, DataHelper.COUNT + " DESC")
        if (cursor.moveToFirst()) {
            do {

                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val song_name = cursor.getString(cursor.getColumnIndexOrThrow("song_name"))
                val artist_name = cursor.getString(cursor.getColumnIndexOrThrow("artist_name"))
                val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))
                val albumid = cursor.getLong(cursor.getColumnIndexOrThrow("album_id"))
                val albumname = cursor.getString(cursor.getColumnIndexOrThrow("album_name"))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"))

                val s = Song_base(song_name, artist_name, url, albumid, albumname, duration, "Com", 0, context)
                recent_song.add(s)

            } while (cursor.moveToNext())
        }
        return recent_song
    }

    fun countalldata_in_any_table(table_name:String): Int {
        val db = database.readableDatabase
        val str = StringBuffer()
        str.append(" ")
        val col = arrayOf(DataHelper.ID, DataHelper.SONG_NAME, DataHelper.ARTIST_NAME)
        val cursor = db.query(table_name, col, null, null, null, null, null)
        return cursor.count
    }

    fun getposition_in_table(table_name: String): ArrayList<Int> {
        val db = database.readableDatabase
        val pos = ArrayList<Int>()
        val cursor = db.query(table_name, null, null, null, null, null, DataHelper.COUNT + " DESC")
        if (cursor.moveToFirst()) {
            do {

                val id = cursor.getInt(cursor.getColumnIndexOrThrow("position"))
                pos.add(id)
            } while (cursor.moveToNext())
        }
        return pos
    }

    fun getalldata_favourite():ArrayList<Song_base>
    {
        val db = database.readableDatabase
        val recent_song = ArrayList<Song_base>()
        val cursor = db.query("favourite", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {

                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val song_name = cursor.getString(cursor.getColumnIndexOrThrow("song_name"))
                val artist_name = cursor.getString(cursor.getColumnIndexOrThrow("artist_name"))
                val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))
                val albumid = cursor.getLong(cursor.getColumnIndexOrThrow("album_id"))
                val albumname = cursor.getString(cursor.getColumnIndexOrThrow("album_name"))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"))

                val s = Song_base(song_name, artist_name, url, albumid, albumname, duration, "Com", 0, context)
                recent_song.add(s)

            } while (cursor.moveToNext())
        }
        return recent_song
    }




    @JvmOverloads
    fun checkexists_for_song_in_table(song_name: String?,table_name: String,playlist_nme: String?=null): Int {
        var num = 0
        val db = database.readableDatabase
        if(table_name.equals("RecentSong") || table_name.equals("favourites",ignoreCase = true))
        {
            val col = arrayOf(DataHelper.COUNT, DataHelper.SONG_NAME)
            val selectionargs = arrayOf(song_name)
            val cursor = db.query(table_name, col, DataHelper.SONG_NAME + " =? ", selectionargs, null, null, null)
            if (cursor.moveToFirst())
            {
                num = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
            }
        }

        return num
    }

    fun checkexist_for_playlist(playlist_nme: String?=null):Int
    {
        var num = 0
        val db = database.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Playlist WHERE playlist_name = '" + playlist_nme+"'",null)
        return cursor.count

    }

      fun updatecount(song_name: String, num: Int): Int {
        val db = database.readableDatabase
        val values = ContentValues()
        values.put(DataHelper.COUNT, num)
        val selectionargs = arrayOf(song_name)
        return db.update(DataHelper.TABLE_NAME, values, DataHelper.SONG_NAME + " =?", selectionargs)
    }

    fun deletelastrow(): Int {
        val db = database.readableDatabase
        val col = arrayOf(DataHelper.ID)
        var id: Long? = null
        val cursor = db.query(DataHelper.TABLE_NAME, col, null, null, null, null, DataHelper.ID + " LIMIT 1")
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        }
        return db.delete(DataHelper.TABLE_NAME, DataHelper.ID + " = '" + id + "'", null)

    }

    fun deletesong_for_table(name: String,table_name: String): Int {
        var num = 0
        val db = database.readableDatabase
//        val col = arrayOf(DataHelper.ID, DataHelper.SONG_NAME)
//        val selectionargs = arrayOf(name)
//        val cursor = db.query(table_name, col, DataHelper.SONG_NAME + " =? ", selectionargs, null, null, null)
//        if (cursor.moveToFirst())
//        {
//            num = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
//        }
        return db.delete(table_name, DataHelper.SONG_NAME + " = '" + name + "'", null)

    }
    fun deletesong_for_playlist(song_name: String,sub_playlist:String): Int {
        var num = 0
        val db = database.readableDatabase
        val col = arrayOf(DataHelper.ID, DataHelper.SONG_NAME)
        val selectionargs = arrayOf(song_name,sub_playlist)
        if(sub_playlist.equals("all"))
        {
           // val cursor = db.query("Playlist", col, DataHelper.SONG_NAME + " =? ", selectionargs, null, null, null)
            return db.delete("Playlist", DataHelper.SONG_NAME + " = '" + song_name + "'", null)
        }
        else
        {
            return db.delete("Playlist", DataHelper.SONG_NAME + " =? AND " + DataHelper.Playlist_name + " =? ", selectionargs)
        }
//        val cursor = db.query("Playlist", col, DataHelper.SONG_NAME + " =? ", selectionargs, null, null, null)
//        if (cursor.moveToFirst())
//        {
//            num = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
//        }


    }

    fun update_position(name: String, position: Int): Int {
        val db = database.readableDatabase
        val values = ContentValues()
        values.put(DataHelper.POSITION, position)
        val selectionargs = arrayOf(name)
        return db.update(DataHelper.TABLE_NAME, values, DataHelper.SONG_NAME + " =?", selectionargs)
    }

    fun fetchdistinctplaylist() : ArrayList<String>
    {
        val db = database.readableDatabase
        var all_playlist = ArrayList<String>()
        var col = arrayOf(DataHelper.Playlist_name)

        val cursor = db.rawQuery("SELECT DISTINCT playlist_name FROM Playlist;",null)

        if (cursor.moveToFirst())
        {
            do {
                var name:String = cursor.getString(cursor.getColumnIndexOrThrow("playlist_name"))
                all_playlist.add(name)
            } while (cursor.moveToNext())
        }

        return all_playlist
    }

    fun getalldata_playlist(playlist_name: String) : ArrayList<Song_base>
    {
        val db = database.readableDatabase
        val playlist_song = ArrayList<Song_base>()
        val cursor = db.query("Playlist", null, DataHelper.Playlist_name + " = '" + playlist_name + "' AND "+ DataHelper.SONG_NAME + " != 'sample' ", null, null, null, null)
        if (cursor.moveToFirst()) {
            do {

                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val song_name = cursor.getString(cursor.getColumnIndexOrThrow("song_name"))
                val artist_name = cursor.getString(cursor.getColumnIndexOrThrow("artist_name"))
                val url = cursor.getString(cursor.getColumnIndexOrThrow("url"))
                val albumid = cursor.getLong(cursor.getColumnIndexOrThrow("album_id"))
                val albumname = cursor.getString(cursor.getColumnIndexOrThrow("album_name"))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"))


                val s = Song_base(song_name, artist_name, url, albumid, albumname, duration, "Com", 0, context)
                playlist_song.add(s)

            } while (cursor.moveToNext())
        }
        return playlist_song
    }



    fun deleteplaylist(playlist:String):Int
    {
        val db = database.readableDatabase
        return db.delete("Playlist", DataHelper.Playlist_name + " = '" + playlist + "'", null)
    }

    fun checkexistance_of_song_in_playlist(song_name: String,playlist: String):Int
    {
        var num = 0
        val db = database.readableDatabase
        val col = arrayOf(DataHelper.SONG_NAME)
        val selectionargs = arrayOf(song_name)
        val cursor = db.rawQuery("SELECT id FROM Playlist WHERE song_name = '"+song_name+"' AND playlist_name = '"+playlist+"' ",null)
        return cursor.count

    }

    fun closedatabase()
    {
        val db = database.readableDatabase
        db.close()
    }


     internal class DataHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {

        override fun onCreate(db: SQLiteDatabase) {

            try {
                db.execSQL(CREATE_TABLE)
                db.execSQL(CREATE_TABLE1)
                db.execSQL(CREATE_TABLE2)
                //                Toast.makeText(context,"table create",Toast.LENGTH_SHORT).show();
            } catch (e: Exception) {
            }


        }

        override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {

            //            Toast.makeText(context,"ENter in upgrade",Toast.LENGTH_SHORT).show();
            try {
                db.execSQL(DROP_TABLE)
                db.execSQL(DROP_TABLE1)
                db.execSQL(DROP_TABLE2)
                onCreate(db)
                //                Toast.makeText(context,"upgrade success",Toast.LENGTH_SHORT).show();

            } catch (e: Exception) {
            }

        }

        companion object {

            internal val DATABASE_NAME = "Localinfo.db"
            internal val TABLE_NAME = "RecentSong"
            internal val TABLE_NAME1 = "Playlist"
            internal val TABLE_NAME2 = "favourites"
            internal val Playlist_name = "playlist_name"
            internal val ID = "id"
            internal val SONG_NAME = "song_name"
            internal val ARTIST_NAME = "artist_name"
            internal val URL = "url"
            internal val VERSION = 24
            internal val ALBUM_ID = "album_id"
            internal val POSITION = "position"
            internal val ALBUM_NAME = "album_name"
            internal val DURATION = "duration"
            internal val COUNT = "count"
            internal val CREATE_TABLE = "create table IF NOT EXISTS $TABLE_NAME ($ID INTEGER PRIMARY KEY AUTOINCREMENT,$SONG_NAME TEXT,$ARTIST_NAME TEXT,$URL TEXT,$ALBUM_ID TEXT, $ALBUM_NAME TEXT,$POSITION INT,$DURATION TEXT, $COUNT INT);"
            internal val CREATE_TABLE1 = "create table IF NOT EXISTS $TABLE_NAME1 ($ID INTEGER PRIMARY KEY AUTOINCREMENT,$SONG_NAME TEXT,$ARTIST_NAME TEXT,$URL TEXT,$ALBUM_ID TEXT, $ALBUM_NAME TEXT,$POSITION INT,$DURATION TEXT, $Playlist_name TEXT);"
            internal val CREATE_TABLE2 = "create table IF NOT EXISTS $TABLE_NAME2 ($ID INTEGER PRIMARY KEY AUTOINCREMENT,$SONG_NAME TEXT,$ARTIST_NAME TEXT,$URL TEXT,$ALBUM_ID TEXT, $ALBUM_NAME TEXT,$POSITION INT,$DURATION TEXT,$COUNT INT);"
            internal val DROP_TABLE = "drop table IF EXISTS " + TABLE_NAME
            internal val DROP_TABLE1 = "drop table IF EXISTS " + TABLE_NAME1
            internal val DROP_TABLE2 = "drop table IF EXISTS " + TABLE_NAME2
        }

    }


}
