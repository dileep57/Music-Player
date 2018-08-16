package com.mytech.lab.musicplayer.Fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mytech.lab.musicplayer.R


/**
 * Created by lnx on 4/3/18.
 */

class Music_lib_2 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.music_lib_2, container, false)
        val transaction = fragmentManager!!
                .beginTransaction()

            transaction.replace(R.id.music_lib_2, Music_library())
            transaction.commit()



        return v
    }
}
