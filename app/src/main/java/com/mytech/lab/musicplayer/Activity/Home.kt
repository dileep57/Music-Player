package com.mytech.lab.musicplayer.Activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.CardView
import android.support.v7.widget.SearchView
import android.support.v7.widget.ShareActionProvider
import android.support.v7.widget.Toolbar
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.mytech.lab.musicplayer.*
import com.mytech.lab.musicplayer.Fragments.Music_lib_2
import com.mytech.lab.musicplayer.Fragments.Recent_song
import com.mytech.lab.musicplayer.Fragments.Settings
import com.mytech.lab.musicplayer.ListView_Adapter.SearchView_Adapter
import com.mytech.lab.musicplayer.Recyclerview_adapter.Song_Adapter
import com.mytech.lab.musicplayer.utils.AccelerometerManager
import com.mytech.lab.musicplayer.utils.DatabaseHelperAdapter
import com.mytech.lab.musicplayer.utils.Song_base
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.utils.PhoneStateReceiver
import petrov.kristiyan.colorpicker.ColorPicker
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class Home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AccelerometerManager.OnShakeListener {



    internal var cardchange: Boolean? = false
    private var doubleBackToExitPressedOnce = false
    private var phoneStateReceiver:PhoneStateReceiver?= null
    private val mShareActionProvider: ShareActionProvider? = null
    internal var editor: SharedPreferences.Editor? = null
    var libraryFragmentnumber:String = "temp"
    var ratingPreferences:SharedPreferences? = null
    var remindmelater:Boolean = false

    lateinit internal var playorpausecard:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        if(Wel.colorshared.getInt("themename",-1)!=-1)
        {

            setTheme(Wel.colorshared.getInt("themename", R.style.AppFullScreenTheme))
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        toolbar = findViewById(R.id.toolbar_layout)
        drawer = findViewById(R.id.drawer)
        toolbar.title = "Music Library"
        setSupportActionBar(toolbar)
        contx = applicationContext


        shakePreferences = getSharedPreferences("shake",Context.MODE_PRIVATE)
        ratingPreferences = getSharedPreferences("rating",Context.MODE_PRIVATE)


        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        cardview = findViewById(R.id.playercard)

        nav = findViewById(R.id.naigation)
        nav.itemIconTintList = null
        nav.setNavigationItemSelectedListener(this)

        card_song_name = findViewById(R.id.song_name)
        card_songart = findViewById(R.id.songart)
        card_artist_name = findViewById(R.id.artist_name)
        card_songicon = findViewById(R.id.songicon)
        helper = DatabaseHelperAdapter(this)
        playorpausecard = findViewById(R.id.playorpause)

        playorpausecard.setOnClickListener {
            Constants.playandpause(applicationContext)
        }

        shared = getSharedPreferences("current_song", Context.MODE_PRIVATE)



        createfragment(Music_lib_2(), "music_library")

        backstackChangeListner()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val dataArr:ArrayList<String> = ArrayList()
        for(i in all_songs.indices)
        {
            dataArr.add(all_songs.get(i).song_name)
        }
        val newsAdapter: SearchView_Adapter = SearchView_Adapter(applicationContext, dataArr)

        var item:MenuItem = menu.findItem(R.id.search)
        menu.findItem(R.id.sort).setVisible(false)
        val search:android.support.v7.widget.SearchView = item.actionView as SearchView

        val searchAutoComplete = search.findViewById(android.support.v7.appcompat.R.id.search_src_text) as SearchView.SearchAutoComplete

        searchAutoComplete.setTextColor(Color.WHITE)
        searchAutoComplete.setAdapter(newsAdapter)

        searchAutoComplete.setOnItemClickListener { adapterView, view, i, l ->

            val queryString= adapterView.getItemAtPosition(i) as String

            val act_pos = Songname_position.get(queryString)!!
            val temp: Song_base = all_songs.get(act_pos)

            Constants.SONGS_LIST.clear()
            Constants.SONGS_LIST.add(kotlin.Pair(temp, act_pos))
            Constants.SONG_NUMBER = 0
            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
            if (!isServiceRunning)
            {
                startService(Intent(applicationContext, SongService::class.java))
            }
            else
            {
                Constants.SONG_CHANGE_HANDLER!!.sendMessage(Constants.SONG_CHANGE_HANDLER!!.obtainMessage(0,"false"))
            }

            cardview.visibility = View.VISIBLE
            hidesoftkeyboard()

        }

        return true
    }

    fun hidesoftkeyboard()
    {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {

//            R.id.playlist -> addplaylist()

            R.id.send_app -> sendapp()

            R.id.shuffle_all -> shuffle_all()

            R.id.settings -> createfragment(Settings(), "settings")

        }
        return false
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {


            R.id.home -> createfragment(Recent_song(), "recent_song")

            R.id.music_library -> {


                createfragment(Music_lib_2(), "music_library")
            }

            R.id.equilizer ->
            {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

                if (intent.resolveActivity(packageManager) != null)
                {
                    startActivityForResult(intent, 1234)
                }
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }

            R.id.theme ->
            {
                selecttheme()
            }

            R.id.player2 ->
            {
                val q = Intent(this, MusicPlayer::class.java)
                startActivity(q)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

            }

            R.id.settings ->
            {
                createfragment(Settings(), "settings")
            }

            R.id.invite ->{
                close_drawer()
                sendapp()
            }
        }

        return true
    }

    fun createfragment(fragment: Fragment, str: String) {

        val manager = supportFragmentManager
        var topfragName:String = ""
        val transaction = manager.beginTransaction()


        if(str.equals("music_library"))
        {
            transaction.replace(R.id.replaceable_layout, fragment, str)
        }
        else
        {
            transaction.add(R.id.replaceable_layout, fragment, str)
        }


        if (str != "music_library") {

            if(manager.getBackStackEntryCount()>0)
            {

                if(getFragmentTag().equals(str,ignoreCase = true)) {
                    close_drawer()
                    return
                }
            }
            transaction.addToBackStack(str)

        }
        else
        {

            if(manager.getBackStackEntryCount()>0)
            {

                if(getFragmentTag().equals(str,ignoreCase = true)) {
                    close_drawer()
                    return
                }
            }
            transaction.addToBackStack(str)
        }

        close_drawer()


        Handler().postDelayed({
            transaction.commit()
        },250)

    }

    private fun close_drawer()
    {
        val drawer = findViewById<View>(R.id.drawer) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
    }

    fun selecttheme()
    {
        var colorpicker:ColorPicker = ColorPicker(this)

        colorpicker.setColors(Constants.coloHexlist)
        colorpicker.setColorButtonSize(46,46)
        colorpicker.setRoundColorButton(true)
        colorpicker.setOnFastChooseColorListener(object :ColorPicker.OnFastChooseColorListener {

            override fun setOnFastChooseColorListener(position:Int, color:Int) {
                Log.i("color", Constants.coloHexlist[position])
                Constants.setcolortheme(position)

                val intent:Intent = Intent(applicationContext, Home::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.face_out)
                colorpicker.dismissDialog()
                finish()

            }

            override fun onCancel(){
            }
        })
        colorpicker.show()


    }


    fun clickoncardplayer(v: View) {
        val i:Intent = Intent(applicationContext,GeneralPlayer::class.java)
        startActivity(i)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

    }


    override fun onBackPressed() {


        if (supportFragmentManager.backStackEntryCount > 0) {

            if(getFragmentTag() == "music_library")
            {
                activity_finish()
            }
            else
            {
                supportFragmentManager.popBackStack()
            }

            if (shared.getString("song_name", null) != null) {
                cardview.visibility = View.VISIBLE
            }
        }




        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

        GeneralPlayer().cardvisible = "yes"

        if(Constants.SONGS_LIST.size>0)
        {
            UpdateUI(applicationContext)
            changeButton_Home()
        }


    }

    private fun setToolbarTitle()
    {


    }

    private fun getFragmentTag() :String
    {
        val manager = supportFragmentManager
        Log.i("GETTAG",manager.getBackStackEntryCount().toString())
        val index = manager.getBackStackEntryCount() - 1
        val backentry:FragmentManager.BackStackEntry = manager.getBackStackEntryAt(index)
        return backentry.getName()
    }

    private fun backstackChangeListner()
    {
        supportFragmentManager!!.addOnBackStackChangedListener {

            if(getFragmentTag() == "settings")
            {
                toolbar.setTitle("Settings")
            }
            if(getFragmentTag() == "recent_song")
            {
                toolbar.setTitle("Recent Songs")
            }
            if(getFragmentTag() == "music_library")
            {
                toolbar.setTitle("Music Library")
            }
        }
    }

    private fun activity_finish()
    {
        if (doubleBackToExitPressedOnce) {
            finish()
            return
        }
        if((ratingPreferences?.getString("status","not given").equals("not given") ||
                ratingPreferences?.getString("status","not given").equals("later")) && !remindmelater)
        {
            ratingDialog()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }



    private fun sendapp() {

        var applicationInfo : ApplicationInfo = getApplicationInfo()

        var filePath:String = applicationInfo.sourceDir;

        var intent:Intent = Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT,"MusicPlayer");
        intent.putExtra(Intent.EXTRA_TEXT, "Share link : "+"https://play.google.com/store/apps/details?id=com.mytech.lab.musicplayer")
        startActivity(Intent.createChooser(intent, "Share app"))
    }

    private fun shuffle_all() {

        try {

            Constants.servicearray("only_song")

            var messagearg:String
            if("only_song".equals(shared.getString("current_album","alb"),ignoreCase = true))
            {
                messagearg = "false"
            }
            else
            {
                messagearg = "true"
            }

            Constants.SONG_SHUFFLE = true

            Constants.mediaAfterprepared(null, applicationContext, all_songs.get(0), 0, 0,
                    "general", "only_song")

            Constants.SONG_NUMBER = 0
            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)

            if (!isServiceRunning)
            {
                val i = Intent(applicationContext, SongService::class.java)
                startService(i)

            } else {

                Constants.SONG_CHANGE_HANDLER!!.sendMessage(Constants.SONG_CHANGE_HANDLER!!.obtainMessage(0,messagearg))

            }

            cardview.visibility = View.VISIBLE

        }
        catch (e: IOException) {
            e.printStackTrace()
        }



    }

    override fun onResume() {
        super.onResume()
        try{
            Constants.SONG_SHUFFLE = Home.shared.getBoolean("shuffle",false)
            Constants.SONG_REPEAT  = Home.shared.getBoolean("repeat",false)
            val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), applicationContext)
            if (isServiceRunning) {
                UpdateUI(applicationContext)
                cardview.visibility = View.VISIBLE

            }
            else
            {
                cardview.visibility = View.INVISIBLE
                val current = shared.getString("current_album","alb")
                if(!current.equals("alb",ignoreCase = true))
                {
                    val song_name = shared.getString("song_name","alb")
                    val artist_name = shared.getString("artist_name","alb")

                    card_song_name.text = song_name
                    card_artist_name.text = artist_name
                    Song_Adapter.getimageart(shared.getLong("albumid", 0), applicationContext, card_songart, R.drawable.music_song_icon_crimson)
                    cardview.visibility = View.VISIBLE
                    changeButton_Home()

                }


            }
        }
        catch (e:Exception){}

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

        if (AccelerometerManager.isSupported(this))
        {
            AccelerometerManager.startListening(this)
        }


    }






    override fun onShake() {

        if(shakePreferences.getBoolean("shake_stats",false) && shakePreferences.getString("action_text","ABC")!="No Action")
        {


            var res:String = shakePreferences.getString("action_text","No Action")

            if(res.equals("Next Song"))
            {
                Controls.nextControl(applicationContext)
            }
            else if(res.equals("Previous Song"))
            {
                Controls.previousControl(applicationContext)
            }
            else if(res.equals("Pause Song"))
            {
                Controls.playPauseControl("pause")
                changeButton_Home()
                GeneralPlayer().changeButton_general()
            }

        }
    }


    companion object {

        lateinit var toggle: ActionBarDrawerToggle

        @JvmStatic
        lateinit var drawer: DrawerLayout

        var localimage = intArrayOf(R.drawable.music1, R.drawable.music2)

        var i:Int = 0

        internal lateinit var shared: SharedPreferences

        internal lateinit var shakePreferences: SharedPreferences

        internal lateinit var card_songart: ImageView

        internal lateinit var card_songicon: ImageView

        internal lateinit var card_song_name: TextView

        internal lateinit var card_artist_name: TextView

        lateinit var cardview: CardView

        lateinit var helper: DatabaseHelperAdapter

        lateinit var nav: NavigationView

        lateinit var toolbar: Toolbar

        var all_songs = ArrayList<Song_base>()

        lateinit var albummap: MutableMap<String, ArrayList<Pair<Song_base, Int>>>

        lateinit var album_array: ArrayList<Song_base>

        var album_song_number = ArrayList<Song_base>()

        lateinit var artistmap: MutableMap<String, ArrayList<Pair<Song_base, Int>>>

//        lateinit var genresmap: MutableMap<String,ArrayList<Pair<Song_base,Int>>>

        lateinit var artist_to_album: MutableMap<String, Int>

        lateinit var artist_array: ArrayList<Song_base>

        lateinit var mini_track: ArrayList<Pair<Song_base, Int>>

        var song_array_general = ArrayList<Pair<Song_base, Int>>() // for general player

        lateinit var Songname_position: MutableMap<String, Int>

        var write_permission_status: Boolean? = false

        var servicearraylist:ArrayList<kotlin.Pair<Song_base,Int>> = ArrayList()

        var sorting_method:String = "title"

        var contx:Context?=null



        fun fetchallsong(context: Context,sorting:String?=null):ArrayList<Song_base> {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
            var sorting_by:String = ""

            if(sorting==null)
                sorting_by = MediaStore.Audio.Media.DISPLAY_NAME
            else
                sorting_by = sorting
            Log.i("String ",sorting_by.toString())
            all_songs.clear()
            val cursor = context.contentResolver.query(uri, null, selection, null, sorting_by)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        val song_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                        val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                        val url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                        val album_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                        var composer: String? = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER))
                        val year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR))

                        val s: Song_base
                        if (composer == null) {
                            composer = "NULL"
                        }
                        val f = File(url)
                        if (f.exists()) {
                            s = Song_base(song_name, artist, url, albumId, album_name, duration, composer, year, context)
                            all_songs.add(s)
                        }

                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
            songame2Position()
            return all_songs
        }

        fun songame2Position()
        {
            Songname_position = HashMap()
            i = 0
            for (s in all_songs) {
                Songname_position.put(s.song_name, i)
                ++i
            }

            val edit = Home.shared.edit()
            try{ edit.putInt("actual_song_position", Home.Songname_position.get(Home.shared.getString("song_name","alb"))!!).apply()}catch (e:Exception){e.printStackTrace()}

        }

        @JvmStatic
        fun getalbums() {


            albummap = TreeMap()
            album_array = ArrayList()
            albummap.clear()
            album_array.clear()
            i = 0
            for (s in all_songs) {
                val albname = s.album_name
                if (albummap[albname] == null) {
                    albummap.put(albname, ArrayList())
                }

                albummap[albname]!!.add(Pair(s, i))
                ++i
            }

            for ((_, value) in albummap) {

                if (value.size > 0) {
                    album_array.add(value[0].first)
                }

            }

        }


        @JvmStatic
        fun getartist() {
            artistmap = TreeMap()
            artist_array = ArrayList()
            artist_to_album = TreeMap()
            artistmap.clear()
            artist_array.clear()
            i = 0
            for (s in all_songs) {
                val artist_name = s.artist
                if (artistmap[artist_name] == null) {
                    artistmap.put(artist_name, ArrayList())
                }
                artistmap[artist_name]!!.add(Pair(s, i))

                ++i
            }

            for ((key, value) in artistmap) {

                if (value.size > 0) {
                    artist_array.add(value[0].first)
                }
                i = 0
                val st = HashSet<String>()
                for (pr in value) {
                    st.add(pr.first.album_name)
                }
                artist_to_album.put(key, st.size)
                st.clear()

            }


        }

        @JvmStatic
        fun mini_track() {
            mini_track = ArrayList()
            mini_track.clear()
            i = 0
            for (s in all_songs) {


                if (java.lang.Long.parseLong(s.duration) < 60000) {
                    mini_track.add(Pair(s, i))

                }
                ++i
            }

        }
        @JvmStatic
        fun filenotsupport()
        {
            Toast.makeText(contx,"File not Support, Refresh Player",Toast.LENGTH_SHORT).show()
        }


        fun cardvisible()
        {
            Home.cardview.visibility = View.VISIBLE
        }

    }

    fun updateuiwithbutton_home(cntx:Context)
    {
        UpdateUI(cntx)
        changeButton_Home()
    }


    fun UpdateUI(cntx: Context?)
    {
        val s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first

        Song_Adapter.getimageart(s.albumId, cntx!!, card_songart, R.drawable.music_song_icon_crimson)

        card_song_name.text = s.song_name

        card_artist_name.text = s.artist

        if(Constants.SONG_PAUSED){
            card_songicon.setImageResource(R.drawable.play_icon_black)
        }else{
            card_songicon.setImageResource(R.drawable.pause_icon_black)
        }

    }


    fun changeButton_Home()
    {
        if(Constants.SONG_PAUSED)
        {
            card_songicon.setImageResource(R.drawable.play_icon_black)
        }
        else
        {
            card_songicon.setImageResource(R.drawable.pause_icon_black)
        }
    }




    override fun onPause() {
        super.onPause()


    }

    public override fun onStop() {
        super.onStop()

    }

    fun setActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (AccelerometerManager.isListening)
        {
            AccelerometerManager.stopListening()
        }
//        unregisterphonestate()

    }

    private fun ratingDialog()
    {
        val detail = AlertDialog.Builder(this)
        val dt = layoutInflater.inflate(R.layout.dialog_rating,null)
        detail.setView(dt)
        val sub_detail = detail.create()
        sub_detail.show()

        val no:LinearLayout = dt.findViewById(R.id.no)
        val now:LinearLayout = dt.findViewById(R.id.now)
        val later:LinearLayout = dt.findViewById(R.id.later)


        no.setOnClickListener {
            ratingPreferences?.edit()!!.putString("status","no").apply()
            sub_detail.dismiss()
        }

        now.setOnClickListener {
            ratingPreferences?.edit()!!.putString("status","now").apply()
            val i = Intent(Intent.ACTION_VIEW)
            sub_detail.dismiss()
            i.data = Uri.parse("https://play.google.com/store/apps/details?id=com.mytech.lab.musicplayer")
            startActivity(i)
        }

        later.setOnClickListener {
            ratingPreferences?.edit()!!.putString("status","later").apply()
            remindmelater = true
            sub_detail.dismiss()
        }


    }

//    private fun registerphonestate()
//    {
//        phoneStateReceiver = PhoneStateReceiver()
//        val intent1:IntentFilter = IntentFilter("android.intent.action.PHONE_STATE")
//        val intent2:IntentFilter = IntentFilter("android.intent.action.ACTION_HEADSET_PLUG")
//        val intent3:IntentFilter = IntentFilter("android.intent.action.NEW_OUTGOING_CALL")
//        val intent4:IntentFilter = IntentFilter("com.tutorialsface.audioplayer.delete")
//        val intent5:IntentFilter = IntentFilter("com.tutorialsface.audioplayer.pause")
//        val intent6:IntentFilter = IntentFilter("com.tutorialsface.audioplayer.next")
//        val intent7:IntentFilter = IntentFilter("com.tutorialsface.audioplayer.play")
//        val intent8:IntentFilter = IntentFilter("com.tutorialsface.audioplayer.previous")
//        val intent9:IntentFilter = IntentFilter("android.intent.action.MEDIA_BUTTON")
//        registerReceiver(phoneStateReceiver,intent1)
//        registerReceiver(phoneStateReceiver,intent2)
//        registerReceiver(phoneStateReceiver,intent3)
//        registerReceiver(phoneStateReceiver,intent4)
//        registerReceiver(phoneStateReceiver,intent5)
//        registerReceiver(phoneStateReceiver,intent6)
//        registerReceiver(phoneStateReceiver,intent7)
//        registerReceiver(phoneStateReceiver,intent8)
//        registerReceiver(phoneStateReceiver,intent9)
//    }
//
//    private fun unregisterphonestate()
//    {
//        unregisterReceiver(phoneStateReceiver)
//    }

}

