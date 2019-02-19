package com.mytech.lab.musicplayer

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import android.os.Handler
import android.os.Message
import java.util.*
import android.media.RemoteControlClient
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat.PRIORITY_MAX
import android.util.Log
import android.widget.Toast
import com.mytech.lab.musicplayer.Activity.GeneralPlayer
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Activity.MusicPlayer
import com.mytech.lab.musicplayer.Fragments.Recent_song
import com.mytech.lab.musicplayer.utils.PhoneStateReceiver
import com.mytech.lab.musicplayer.utils.Song_base
import java.util.concurrent.TimeUnit


class SongService : Service(), AudioManager.OnAudioFocusChangeListener {


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    lateinit var audioManager: AudioManager
    lateinit var mEqualizer: Equalizer
    lateinit var mVqualizer: Visualizer
    private var remoteComponentName: ComponentName? = null
    val CHANNEL_ID: String = "my_musicPlayer"

    private var remoteControlClient: RemoteControlClient? = null
    internal var mDummyAlbumArt: Bitmap? = null
    var maxvol: Int = 0
    var currentvol: Int = 0
    var duck_volume: Double = 0.0


    companion object {
        var mp: MediaPlayer? = null
        var NOTIFICATION_ID = 1111
        lateinit var notification: Notification
        val NOTIFY_PREVIOUS = "com.tutorialsface.audioplayer.previous"
        val NOTIFY_DELETE = "com.tutorialsface.audioplayer.delete"
        val NOTIFY_PAUSEPLAY = "com.tutorialsface.audioplayer.pause"
        val NOTIFY_NEXT = "com.tutorialsface.audioplayer.next"

        lateinit var simpleContentView: RemoteViews
        lateinit var expandedView: RemoteViews

        private var currentVersionSupportBigNotification = false
        private var currentVersionSupportLockScreenControls = false


    }


    override fun onCreate() {

        mp = MediaPlayer()


        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        maxvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        currentvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        duck_volume = currentvol.toDouble()

        mp!!.setOnCompletionListener {
            Controls.nextControl(getApplicationContext())
        }

        currentVersionSupportBigNotification = Constants.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = Constants.currentVersionSupportLockScreenControls();

        super.onCreate()
    }

    val handler = Handler(object : Handler.Callback {
        override fun handleMessage(p0: Message?): Boolean {
            if (mp != null) {
                val progress = mp!!.getCurrentPosition() * 100 / mp!!.getDuration()
                val i = arrayOfNulls<Int>(4)
                i[0] = mp!!.currentPosition
                i[1] = mp!!.getDuration()

                try {
                    Constants.PROGRESSBAR_HANDLER!!.sendMessage(Constants.PROGRESSBAR_HANDLER!!.obtainMessage(0, i))
                } catch (e: Exception) {
                }

            }
            return false
        }
    })

    private inner class MainTask : TimerTask() {
        override fun run() {
            if (mp != null && mp!!.isPlaying) {
                handler.sendEmptyMessage(0)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        try {

            if (Constants.SONGS_LIST.size <= 0) {
                for (i in Home.servicearraylist.indices) {
                    Constants.SONGS_LIST.add(Pair(Home.servicearraylist[i].first, Home.servicearraylist[i].second))
                }
            }
            if (currentVersionSupportLockScreenControls) {
                RegisterRemoteClient()
            }

            val s: Song_base = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first
            var songPath: String = s.url
            playSong(songPath, s)
            Constants.SONG_PAUSED = false
            newnotification()
            startForeground()
            collectsongdata(s)
            Home().updateuiwithbutton_home(applicationContext)
            GeneralPlayer().changeuiwithbutton_general(applicationContext)
            MusicPlayer().changeUIwithbutton_musicplayer(applicationContext)
            updatenotification()



            Constants.SONG_CHANGE_HANDLER = Handler(object : Handler.Callback {

                override fun handleMessage(msg: Message?): Boolean {

                    val songPath: String
                    val s: Song_base
                    val messagearg: String = msg!!.obj as String
                    Constants.SONG_PAUSED = false
                    try {
                        if (messagearg.equals("true", ignoreCase = true)) {
                            Constants.SONGS_LIST.clear()
                            for (i in Home.servicearraylist.indices) {
                                Constants.SONGS_LIST.add(Pair(Home.servicearraylist[i].first, Home.servicearraylist[i].second))
                            }
                        }

                        s = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first
                        songPath = s.url
                    } catch (e: Exception) {
                        Constants.SONG_PAUSED = true
                        mp?.pause()
                        Home().changeButton_Home()
                        MusicPlayer().changeButton_musicplayer()
                        GeneralPlayer().changeButton_general()
                        updatenotification()
                        return true
                    }



                    try {
                        playSong(songPath, s)
                        Home().UpdateUI(applicationContext)
                        GeneralPlayer().update_favourite()
                        MusicPlayer().updateUI_Musicplayer()
                        GeneralPlayer().updateUI_GeneralPlayer(applicationContext)
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }

                    collectsongdata(s)
                    Log.i("CLICK", "SongChnage Handler call")
                    newnotification()
                    startNotify()
                    return false

                }

            })


            Constants.PLAY_PAUSE_HANDLER = Handler(object : Handler.Callback {

                override fun handleMessage(msg: Message?): Boolean {

                    var message: String = msg!!.obj as String

                    if (mp == null)
                        return false
                    if (message.equals("play")) {
                        Constants.SELF_CHANGE = false
                        Constants.SONG_PAUSED = false
                        if (currentVersionSupportLockScreenControls) {
                            remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING)
                        }
                        mp!!.start()
                    } else if (message.equals("pause")) {
                        Constants.SELF_CHANGE = true
                        Constants.SONG_PAUSED = true
                        if (currentVersionSupportLockScreenControls) {
                            remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED)
                        }
                        mp!!.pause()
                    }

//                    newnotification()
                    Home().changeButton_Home()
                    MusicPlayer().changeButton_musicplayer()
                    GeneralPlayer().changeButton_general()
                    Log.i("CLICK", "PlayPause Handler call")
                    updatenotification()
                    startNotify()
                    return true

                }

            })

            Constants.SHUFFLE_REPEAT = Handler(object : Handler.Callback {

                override fun handleMessage(msg: Message?): Boolean {

                    Home().changeButton_Home()
                    MusicPlayer().changeButton_musicplayer()
                    GeneralPlayer().changeButton_general()
                    return false;

                }

            })


        } catch (e: Exception) {
            e.printStackTrace();
        }


        return START_NOT_STICKY
    }


    fun collectsongdata(s: Song_base) {
        val actual_pos = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).second
        Constants.mediaAfterprepared(null, applicationContext, s, actual_pos, Constants.SONG_NUMBER,
                "general", Home.shared.getString("current_album", "alb"), Home.shared.getString("playlist_name", "alb"))
        Constants.databasedata(s, applicationContext, actual_pos, "RecentSong")
        Recent_song.updaterecentsong(applicationContext)
        Recent_song.setvisibility()
    }


    fun newnotification() {

        var songname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.song_name
        var albumname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.album_name
        var artistname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.artist
        val notificationIntent = Intent(applicationContext, Home::class.java)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 99, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        simpleContentView = RemoteViews(applicationContext.packageName, R.layout.custom_notification)
        if (currentVersionSupportBigNotification) {
            expandedView = RemoteViews(applicationContext.packageName, R.layout.big_notification)
        }

        simpleContentView.setTextViewText(R.id.textSongName, songname)
        simpleContentView.setTextViewText(R.id.textAlbumName, albumname)
        expandedView.setTextViewText(R.id.textSongName, songname)
        expandedView.setTextViewText(R.id.textAlbumName, albumname)
        expandedView.setTextViewText(R.id.textArtistname, artistname)
//        expandedView.setTextViewText(R.id.timeelapse,Constants.calculatetime(mp!!.currentPosition))


        try {
            val albumId = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.albumId
            val albumArt = Constants.getAlbumart(applicationContext, albumId)
            if (albumArt != null) {
                simpleContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt)
                expandedView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt)

            } else {
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.general_player_small_art)
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.general_player_small_art)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            //val name:String = "for_oreo"// The user-visible name of the channel.
            //val importance:Int = NotificationManager.IMPORTANCE_HIGH
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channelId = createNotificationChannel("my_service", "My Background Service")
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification = notificationBuilder.setOngoing(true)
                        .setPriority(PRIORITY_MAX)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentIntent(pendingIntent)
                        .setCustomBigContentView(expandedView)
                        .setContent(simpleContentView)
                        .setSmallIcon(R.drawable.headphones_tick)
                        .build()

            } else {
                notification = NotificationCompat.Builder(applicationContext, "notify")
                        .setContentIntent(pendingIntent)
                        .setPriority(99)
                        .setCustomBigContentView(expandedView)
                        .setContent(simpleContentView)
                        .setSmallIcon(R.drawable.headphones_tick)
                        .setChannelId(CHANNEL_ID)
                        .build()
            }


        } else {
            notification = NotificationCompat.Builder(applicationContext, "notify")
                    .setContentIntent(pendingIntent)
                    .setPriority(99)
                    .setSmallIcon(R.drawable.headphones_tick)
                    .build()
        }

        setListeners(simpleContentView)
        if (currentVersionSupportBigNotification)
            setListeners(expandedView)


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

        } else {
            notification.contentView = simpleContentView
            if (currentVersionSupportBigNotification)
                notification.bigContentView = expandedView
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
//        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun startForeground() {
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startNotify() {
//        expandedView.setTextViewText(R.id.timeelapse,Constants.calculatetime(mp!!.currentPosition))
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(NOTIFICATION_ID, notification)


    }


    fun updatenotification() {

        if (Constants.SONG_PAUSED) {
            Log.i("BUTTON PRESS", "button change to play")
            simpleContentView.setImageViewResource(R.id.playpause, R.drawable.play_white)
            expandedView.setImageViewResource(R.id.playpause, R.drawable.play_white)
        } else {
            Log.i("BUTTON PRESS", "button change to pause")
            simpleContentView.setImageViewResource(R.id.playpause, R.drawable.pause_white)
            expandedView.setImageViewResource(R.id.playpause, R.drawable.pause_white)

        }

    }


    fun setListeners(view: RemoteViews) {
        val previous = Intent(NOTIFY_PREVIOUS)
        val delete = Intent(NOTIFY_DELETE)
        var pauseplay = Intent(NOTIFY_PAUSEPLAY)
        val next = Intent(NOTIFY_NEXT)
        Log.i("CLICK", "CHECK LISTner")
        updatenotification()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


        } else {
            val pPrevious = PendingIntent.getBroadcast(applicationContext, 0, previous, PendingIntent.FLAG_UPDATE_CURRENT)
            view.setOnClickPendingIntent(R.id.btnprev, pPrevious)

            val pDelete = PendingIntent.getBroadcast(applicationContext, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT)
            view.setOnClickPendingIntent(R.id.btnDelete, pDelete)

            val pPlayPause = PendingIntent.getBroadcast(applicationContext, 0, pauseplay, PendingIntent.FLAG_UPDATE_CURRENT)
            view.setOnClickPendingIntent(R.id.playpause_head, pPlayPause)

            val pNext = PendingIntent.getBroadcast(applicationContext, 0, next, PendingIntent.FLAG_UPDATE_CURRENT)
            view.setOnClickPendingIntent(R.id.btnNext, pNext)
        }

    }

    override fun onDestroy() {
        if (mp != null) {
            mp!!.stop()
            mp = null
        }
        super.onDestroy()
    }

    @SuppressLint("NewApi")
    fun playSong(songPath: String, s: Song_base) {
        try {
            if (currentVersionSupportLockScreenControls) {
                UpdateMetadata(s, applicationContext)
                remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING)
            }
            mp?.reset()
            mp?.setDataSource(songPath)
            mp?.prepare()
            mp?.start()
            mEqualizer = Equalizer(0, mp!!.audioSessionId)

            mEqualizer.setEnabled(true)
            val timer: Timer = Timer(true)
            timer.scheduleAtFixedRate(MainTask(), 0, 100)
        } catch (e: Exception) {
            Home.filenotsupport()
            Controls.nextControl(applicationContext)
        }
    }

    @SuppressLint("NewApi")
    private fun RegisterRemoteClient() {
        remoteComponentName = ComponentName(applicationContext, PhoneStateReceiver().ComponentName())
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName)
                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
                mediaButtonIntent.component = remoteComponentName
                val mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
                remoteControlClient = RemoteControlClient(mediaPendingIntent)
                audioManager.registerRemoteControlClient(remoteControlClient)
            }
            remoteControlClient?.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY or
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE or
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE or
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP or
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS or
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT)
        } catch (ex: Exception) {
        }

    }

    @SuppressLint("NewApi")
    private fun UpdateMetadata(data: Song_base, cntx: Context) {
        if (remoteControlClient == null)
            return
        val metadataEditor = remoteControlClient?.editMetadata(true)
        metadataEditor?.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.album_name)
        metadataEditor?.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.artist)
        metadataEditor?.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.song_name)
        mDummyAlbumArt = Constants.getAlbumart(applicationContext, data.albumId)
        if (mDummyAlbumArt == null) {
            mDummyAlbumArt = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background)
        }
        metadataEditor?.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt)
        metadataEditor?.apply()
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onAudioFocusChange(p0: Int) {

        if (SongService.mp != null && !Constants.SELF_CHANGE) {
            when (p0) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Constants.SONG_PAUSED = false
                    Controls.playPauseControl("play")
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Controls.playPauseControl("pause")
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    Controls.playPauseControl("pause")
                    Handler().postDelayed({
                        Controls.playPauseControl("play")
                    }, TimeUnit.SECONDS.toMillis(2))
                }

                AudioManager.AUDIOFOCUS_LOSS -> {
                    Controls.playPauseControl("pause")

                }
            }
        }

    }

}

