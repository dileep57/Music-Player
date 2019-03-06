package com.mytech.lab.musicplayer.Fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.*
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar

import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.sub_sub_fragment.Artist_root
import com.mytech.lab.musicplayer.sub_sub_fragment.Playlist_root

import java.util.ArrayList
import android.view.*
import com.mytech.lab.musicplayer.Sub_Fragments.*
import com.mytech.lab.musicplayer.sub_sub_fragment.Album_root
import android.support.design.widget.TabLayout
import android.util.Log
import com.mytech.lab.musicplayer.Activity.Home
import com.ogaclejapan.smarttablayout.SmartTabLayout


/**
 * Created by lnx on 26/2/18.
 */

class Music_library : Fragment() {

    private var toolbar: Toolbar? = null
    lateinit private var tabLayout: SmartTabLayout
    lateinit private var viewPager: ViewPager
    private var myContext: Context? = null
    lateinit private var adapter:ViewPagerAdapter

    override fun onAttach(context: Context?) {
        this.myContext = context
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        menu?.clear()
        inflater!!.inflate(R.menu.main_menu,menu)
        if (viewPager.getCurrentItem()==0)
        {
            menu?.findItem(R.id.by_title)?.setVisible(true)
        }
        else if(viewPager.getCurrentItem()==1)
        {
            if(menu!=null)
            {
                menu.findItem(R.id.by_title).setVisible(true)
            }
        }
        else if(viewPager.getCurrentItem()==2)
        {
            if(menu!=null)
            {
                menu.findItem(R.id.sort).setVisible(false)
            }
        }
        else if(viewPager.getCurrentItem()==3)
        {
            if(menu!=null)
            {
                menu.findItem(R.id.sort).setVisible(true)
                menu.findItem(R.id.by_duration).setVisible(true)
                menu.findItem(R.id.by_artist).setVisible(true)
                menu.findItem(R.id.by_title).setVisible(true)
                menu.findItem(R.id.by_album).setVisible(true)
                menu.findItem(R.id.by_duration).setVisible(true)
                menu.findItem(R.id.by_date).setVisible(true)
                menu.findItem(R.id.by_default).setVisible(true)

            }
        }
        else if(viewPager.getCurrentItem()==4)
        {
            if(menu!=null)
            {
                menu.findItem(R.id.by_title).setVisible(true)
                menu.findItem(R.id.sort).setVisible(false)
            }
        }
        else if(viewPager.getCurrentItem()==5)
        {
            if(menu!=null)
            {
                menu.findItem(R.id.sort).setVisible(false)
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_music_library, container, false)


        viewPager = v.findViewById<View>(R.id.viewpager) as ViewPager
        setupViewPager(viewPager)
        tabLayout = v.findViewById<View>(R.id.tabs) as SmartTabLayout
        tabLayout.setSmoothScrollingEnabled(true)
        tabLayout.setViewPager(viewPager)


        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                ActivityCompat.invalidateOptionsMenu(getActivity())

            }

            override fun onPageSelected(position: Int) {
                viewPager.setCurrentItem(position, true)

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        return v
    }


    private fun setupViewPager(viewPager: ViewPager?) {
        adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(Album_root(), "Album")
        adapter.addFragment(Artist_root(), "Artists")
        adapter.addFragment(Playlist_root(), "Playlist")
        adapter.addFragment(Songs(), "Songs")
        adapter.addFragment(Mini_track(), "Mini Track")
        adapter.addFragment(Folder(), "Select Folder")
        viewPager!!.adapter = adapter
    }



    internal inner class ViewPagerAdapter(manager: FragmentManager?) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }

        override fun getItemPosition(`object`: Any): Int {
            return super.getItemPosition(`object`)
        }

        fun onPageSelected(position: Int) {
//            invalidateFragmentMenus(position)
        }


    }

    override fun onResume() {
        super.onResume()
        (activity as Home).setActionBarTitle("Music Library")

    }




}
