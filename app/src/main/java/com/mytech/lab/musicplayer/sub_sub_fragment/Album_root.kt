package com.mytech.lab.musicplayer.sub_sub_fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Sub_Fragments.Albums

class Album_root : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var v:View = inflater.inflate(R.layout.root_album,container,false)

        val transaction = fragmentManager!!.beginTransaction()

        transaction.replace(R.id.album_root_frame, Albums())

        transaction.commit()

        return v;
    }
}