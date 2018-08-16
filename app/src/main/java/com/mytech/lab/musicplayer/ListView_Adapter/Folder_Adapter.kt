package com.mytech.lab.musicplayer.ListView_Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mytech.lab.musicplayer.R


class Folder_Adapter(context:Context,folder_info:ArrayList<String>) : ArrayAdapter<String>(context, R.layout.recycler_folderview,folder_info)
{

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val inflater = LayoutInflater.from(context)

        val v = inflater.inflate(R.layout.recycler_folderview, parent, false)

        val stringname:String = getItem(position)


        val foldername = v.findViewById<TextView>(R.id.foldername)
        foldername.text = stringname

        return v
    }


}