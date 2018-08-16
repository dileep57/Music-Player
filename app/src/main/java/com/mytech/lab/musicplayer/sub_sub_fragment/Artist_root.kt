package com.mytech.lab.musicplayer.sub_sub_fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Sub_Fragments.Artists

/**
 * Created by lnx on 4/3/18.
 */

class Artist_root : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.root_artist, container, false)

        val transaction = fragmentManager!!.beginTransaction()

        transaction.replace(R.id.root_frame, Artists())

        transaction.commit()

        return v
    }
}
