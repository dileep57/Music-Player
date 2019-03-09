package com.mytech.lab.musicplayer.Sub_Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.*
import java.io.File
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.Constants.Companion.current_directory_array
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Recyclerview_adapter.Folder_Adapter
import com.mytech.lab.musicplayer.Recyclerview_adapter.MusicFile_Adapter
import com.mytech.lab.musicplayer.SongService
import com.mytech.lab.musicplayer.UniversalFilter
import com.mytech.lab.musicplayer.utils.Song_base
import java.io.FilenameFilter


class Folder : Fragment() {

    lateinit var cntx:Context
    val PERMISSIONS_REQUEST_CODE = 0
    val FILE_PICKER_REQUEST_CODE = 1
    val folder_array = ArrayList<String>()
    val song_array = ArrayList<String>()
    lateinit var recyclerview_folder:RecyclerView
    lateinit var recyclerview_song:RecyclerView
    lateinit var folder_adapter: Folder_Adapter
    lateinit var song_adapter: MusicFile_Adapter

    lateinit var back:LinearLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        cntx = context!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v:View = inflater.inflate(R.layout.sub_fragment_folder, container, false)
        recyclerview_folder = v.findViewById(R.id.folderview)
        recyclerview_folder.layoutManager = LinearLayoutManager(v.context)
        back = v.findViewById(R.id.folder_back)

        back.setOnClickListener {
            if(current_directory_array.size!=1)
            {
                folder_back()
                recursive_in_folder(current_directory_array.joinToString("/"))
            }
            else
            {
                Toast.makeText(v.context,"On Root Directory",Toast.LENGTH_SHORT).show()
            }

        }




        folder_adapter = Folder_Adapter(folder_array,cntx)
        recyclerview_folder.adapter = folder_adapter
//
        Backgroundtask().execute()

        checkpermission()

        return v
    }

    inner class Backgroundtask : AsyncTask<Void, Void, Boolean>() {


        override fun doInBackground(vararg voids: Void): Boolean? {
            return true
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)


        }
    }

    private fun recursive_in_folder(filename:String)
    {
        folder_array.clear()
        val file = File(filename)
        if (file.isDirectory() === false) {
            Toast.makeText(cntx, "Not a directory", Toast.LENGTH_SHORT).show()
            return
        }
        val files = file.listFiles()
        var i = 0
        if(files==null)
            return
        for (f in files) {
            if ((f.isFile() || f.isDirectory()) && (!f.isHidden )) {
                try {
//                    Log.i("FileName ",f.name);
                    if(f.isFile())
                    {
//                        Log.i("file ",f.name);
                        if(UniversalFilter.checkfilecode(f))
                             folder_array.add(f.name)
                    }
                    else
                    {
//                        Log.i("folder ",f.name);
                        if (f.isDirectory() == true && f.listFiles()!=null && f.listFiles().size!=0)
                        {
                                folder_array.add(i,f.name)
                                ++i
                        }


                    }

                } catch (e: Exception) { }

            }
        }
        folder_adapter.notifyDataSetChanged()
//

    }

    private fun folder_back()
    {
        current_directory_array.removeAt(current_directory_array.size-1)
    }



    private fun comm()
    {
        folder_adapter.clickfolder { v, foldername, position ->

            val nameoffolder = foldername
            val filepath:String = current_directory_array.joinToString("/") + "/"+nameoffolder.toString()
            val file = File(filepath)
            if(file.isDirectory)
            {
                if (file.isDirectory() == true && file.listFiles()!=null && file.listFiles().size!=0)
                {
                    current_directory_array.add(nameoffolder.toString())
                    recursive_in_folder(current_directory_array.joinToString("/"))
                }
            }
            else if(file.isFile)
            {
                val pathname:String = current_directory_array.joinToString("/") + "/"+foldername

                val file = File(pathname)
                val name = file.name
                val s: Song_base = Home.all_songs.get(Home.Songname_position.get(name)!!)
                Constants.SONGS_LIST.clear()
                Constants.SONGS_LIST.add(Pair(s, Home.Songname_position.get(name)!!))
                Constants.SONG_NUMBER = 0
//                Log.i("FileName ",file.name)
                Constants.mediaAfterprepared(null,context,s,position, position,
                        "general", Constants.SONG_FROM_FOLDER)

                val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context!!)
                if (!isServiceRunning)
                {
                    val i = Intent(context, SongService::class.java)
                    context!!.startService(i)
                }
                else
                {
                    Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,"false"))
                }

                Home().cardview?.visibility = View.VISIBLE
            }


        }

    }

    override fun onResume() {
        super.onResume()
        comm()
    }

    private fun getFileFromStorage()
    {
        if(current_directory_array.size==1)
        {
            if(Build.VERSION.SDK_INT<=23)
            {
                recursive_in_folder("/storage")
            }
            else
            {
                recursive_in_folder(Environment.getExternalStorageDirectory().getAbsolutePath())
            }

        }
        else
        {
            recursive_in_folder(current_directory_array.joinToString("/"))
        }
    }


    private fun checkpermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
            }
            else
            {
                getFileFromStorage()

            }
        }
        else {
            getFileFromStorage()

        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {


        when (requestCode) {
            123 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFileFromStorage()

            }
            else
            {
                Toast.makeText(context, "Permission Denied, You should allow permission", Toast.LENGTH_SHORT).show()

            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }





}