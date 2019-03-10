package com.mytech.lab.musicplayer.Sub_Fragments

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Recyclerview_adapter.Artist_adapter
import com.mytech.lab.musicplayer.sub_sub_fragment.Artist_expand
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

/**
 * Created by lnx on 26/2/18.
 */

class Artists : Fragment() {

    internal lateinit var recyclerView: FastScrollRecyclerView
    internal var artist_adapter: Artist_adapter? = null
    internal var context: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item!!.itemId)
        {
            R.id.by_default ->
            {

            }

            R.id.by_artist ->
            {
                 artist_adapter?.notifyDataSetChanged()
            }



        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.sub_fragment_artist, container, false)
        recyclerView = v.findViewById(R.id.recycler)
        recyclerView.layoutManager = LinearLayoutManager(v.context)
        Background().execute()
        return v

    }

    fun commm(context: Context?) {
        artist_adapter?.setCommnicator { v, s, position ->
            val bundle = Bundle()
            bundle.putString(Constants.ARTIST_NAME, s.artist)
            val frag = Artist_expand()
            frag.arguments = bundle
            val manager = activity!!.supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.root_frame, frag)
            transaction.addToBackStack(Constants.SONG_FROM_ARTIST)
            transaction.commit()
        }
    }

   inner class Background : AsyncTask<Void,Void,Boolean>()
    {
        override fun doInBackground(vararg p0: Void?): Boolean {
            artist_adapter = Artist_adapter(Home.artist_array, context)
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            recyclerView.adapter = artist_adapter
            commm(context)
        }

    }


    override fun onResume() {
        super.onResume()
            artist_adapter?.notifyDataSetChanged()

    }

}

