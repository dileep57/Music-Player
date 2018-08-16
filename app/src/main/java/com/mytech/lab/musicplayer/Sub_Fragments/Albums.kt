package com.mytech.lab.musicplayer.Sub_Fragments

import android.content.Context
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.*
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.Recyclerview_adapter.Album_adapter
import com.mytech.lab.musicplayer.sub_sub_fragment.Album_expand
import com.mytech.lab.musicplayer.utils.Song_base
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView


/**
 * Created by lnx on 26/2/18.
 */

class Albums : Fragment() {

    internal lateinit var recyclerView: FastScrollRecyclerView
    internal  var cntxt: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.cntxt = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.sub_fragment_album, container, false)

        recyclerView = v.findViewById(R.id.sub_fragment_album_recycler)
        cntxt = v.context
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.gridspace)


//        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true, 0))
//        recyclerView.setHasFixedSize(true)
        LoadAlbumAdapter().execute()
        return v

    }



    private inner class LoadAlbumAdapter : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg voids: Void): Boolean? {
            try {

            } catch (e: Exception) {

            }

            return true
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)

                recyclerView.layoutManager = GridLayoutManager(context, 2)
                album_adapter = Album_adapter(Home.album_array, cntxt!!)
                recyclerView.adapter = album_adapter


            if (Home.album_array.size > 0) {
                commm()
            }


        }
    }

    inner class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean, private val headerNum: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            val position = parent.getChildAdapterPosition(view) - headerNum // item position

            if (position >= 0) {
                val column = position % spanCount // item column

                if (includeEdge) {
                    outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

                    if (position < spanCount) { // top edge
                        outRect.top = spacing
                    }
                    outRect.bottom = spacing // item bottom
                } else {
                    outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                    outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                    if (position >= spanCount) {
                        outRect.top = spacing // item top
                    }
                }
            } else {
                outRect.left = 0
                outRect.right = 0
                outRect.top = 0
                outRect.bottom = 0
            }
        }
    }


    private fun dpToPx(dp: Int): Int {
        val r = resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
    }

    private fun commm() {
        album_adapter?.setCommnicator(object : Album_adapter.Communicator {
            override fun clickonplaybutton(v: View, s: Song_base, position: Int) {
                val bundle = Bundle()
                bundle.putString("album_name", s.album_name)
                val frag = Album_expand()
                frag.arguments = bundle
                val manager = activity!!.supportFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.album_root_frame, frag)
                transaction.addToBackStack("album")
                transaction.commit()



            }
        });
    }

    companion object {
        internal var album_adapter: Album_adapter? = null
        var album_number: Int = 0
        private var cntx : Context? = null

    }

    override fun onResume() {
        super.onResume()
        album_adapter?.notifyItemRangeChanged(0,Home.album_array.size)

    }


}
