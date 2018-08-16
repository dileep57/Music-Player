package com.mytech.lab.musicplayer.sub_sub_fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Sub_Fragments.Artists
import com.mytech.lab.musicplayer.Sub_Fragments.Playlist

/**
 * Created by lnx on 13/3/18.
 */
class Playlist_root : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.root_playlist, container, false)

        val transaction = fragmentManager!!.beginTransaction()

        transaction.replace(R.id.root_frame_playlist, Playlist())

        transaction.commit()

        return v
    }
}