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
import android.media.MediaMetadataRetriever
import android.os.Build
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

        val NOTIFY_PREVIOUS = "com.mytech.lab.musicplayer.previous"
        val NOTIFY_DELETE = "com.mytech.lab.musicplayer.delete"
        val NOTIFY_PAUSEPLAY = "com.mytech.lab.musicplayer.pause"
        val NOTIFY_NEXT = "com.mytech.lab.musicplayer.next"

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
            updatenotificationIcon()
            Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0))


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
                        return false
                    }



                    try {
                        Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0, null))
                        playSong(songPath, s)
                    } catch (e: Exception) {
                        Log.e("ERROR", e.message)
                    }
                    collectsongdata(s)
                    updateNotificationData()
                    updatenotificationIcon()
                    startNotify()
                    return true

                }

            })


            Constants.PLAY_PAUSE_HANDLER = Handler(object : Handler.Callback {

                override fun handleMessage(msg: Message?): Boolean {

                    var message: String = msg!!.obj as String

                    if (mp == null)
                        return false

                    if (message.equals(Constants.PLAY)) {
                        Constants.SELF_CHANGE = false
                        Constants.SONG_PAUSED = false
                        if (currentVersionSupportLockScreenControls) {
                            remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING)
                        }
                        mp!!.start()

                    } else if (message.equals(Constants.PAUSE)) {
                        Constants.SELF_CHANGE = true
                        Constants.SONG_PAUSED = true
                        if (currentVersionSupportLockScreenControls) {
                            remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED)
                        }
                        mp!!.pause()
                    }
                    Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0, null))
                    updatenotificationIcon()
                    startNotify()
                    return true

                }

            })

            Constants.SHUFFLE_REPEAT = Handler(object : Handler.Callback {

                override fun handleMessage(msg: Message?): Boolean {
                    Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0, null))
                    return true

                }
            })


        } catch (e: Exception) {
            Controls.createToast(this, e.message!!,Toast.LENGTH_SHORT)
        }

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

        return START_NOT_STICKY
    }


    fun collectsongdata(s: Song_base) {
        val lambda1=Thread{
            val actual_pos = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).second
            Constants.mediaAfterprepared(null, applicationContext, s, actual_pos, Constants.SONG_NUMBER,
                    "general", Home.shared.getString("current_album", "alb"), Home.shared.getString("playlist_name", "alb"))
            Constants.databasedata(s, applicationContext, actual_pos, "RecentSong")
            Recent_song().updaterecentsong(applicationContext)
            Recent_song().setvisibility()
        }
        lambda1.start()

    }


    fun newnotification() {


        val notificationIntent = Intent(applicationContext, Home::class.java)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 99, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        simpleContentView = RemoteViews(applicationContext.packageName, R.layout.custom_notification)
        if (currentVersionSupportBigNotification) {
            expandedView = RemoteViews(applicationContext.packageName, R.layout.big_notification)
        }
        updateNotificationData()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channelId = createNotificationChannel("my_service", "NotificationService")
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
                        .setPriority(PRIORITY_MAX)
                        .setCustomBigContentView(expandedView)
                        .setContent(simpleContentView)
                        .setSmallIcon(R.drawable.headphones_tick)
                        .setChannelId(CHANNEL_ID)
                        .build()
            }


        } else {
            notification = NotificationCompat.Builder(applicationContext, "notify")
                    .setContentIntent(pendingIntent)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.drawable.headphones_tick)
                    .build()
        }

        setListeners(simpleContentView)
        if (currentVersionSupportBigNotification)
            setListeners(expandedView)



        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            notification.contentView = simpleContentView
            if (currentVersionSupportBigNotification)
                notification.bigContentView = expandedView
        }

    }


    private fun updateNotificationData(){
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
            Log.i("error",e.message)
        }

        var songname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.song_name
        var albumname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.album_name
        var artistname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.artist
        simpleContentView.setTextViewText(R.id.textSongName, songname)
        simpleContentView.setTextViewText(R.id.textAlbumName, albumname)
        expandedView.setTextViewText(R.id.textSongName, songname)
        expandedView.setTextViewText(R.id.textAlbumName, albumname)
        expandedView.setTextViewText(R.id.textArtistname, artistname)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun startForeground() {
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startNotify() {

            var mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(NOTIFICATION_ID, notification)
    }


    fun updatenotificationIcon() {
        if (Constants.SONG_PAUSED) {
            simpleContentView.setImageViewResource(R.id.playpause, R.drawable.play_white)
            expandedView.setImageViewResource(R.id.playpause, R.drawable.play_white)
//            Log.i("pause", "ok")
        } else {
            simpleContentView.setImageViewResource(R.id.playpause, R.drawable.pause_white)
            expandedView.setImageViewResource(R.id.playpause, R.drawable.pause_white)
//            Log.i("play", "ok")
        }
        startNotify()
    }

    fun setListeners(view: RemoteViews) {
        var previous:Intent? = null
        var delete:Intent? = null
        var pauseplay:Intent? = null
        var next:Intent? = null


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            previous = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(NOTIFY_PREVIOUS);
            delete = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(NOTIFY_DELETE);
            pauseplay = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(NOTIFY_PAUSEPLAY);
            next = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(NOTIFY_NEXT)
        } else {
            previous = Intent(NOTIFY_PREVIOUS)
            delete = Intent(NOTIFY_DELETE)
            pauseplay = Intent(NOTIFY_PAUSEPLAY)
            next = Intent(NOTIFY_NEXT)
        }

        val pPrevious = PendingIntent.getBroadcast(applicationContext, 0, previous, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.btnprev, pPrevious)

        val pDelete = PendingIntent.getBroadcast(applicationContext, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete)

        val pPlayPause = PendingIntent.getBroadcast(applicationContext, 0, pauseplay, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.playpause_head, pPlayPause)

        val pNext = PendingIntent.getBroadcast(applicationContext, 0, next, PendingIntent.FLAG_UPDATE_CURRENT)
        view.setOnClickPendingIntent(R.id.btnNext, pNext)

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
            Home.filenotsupport(applicationContext)
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
                    Controls.playPauseControl(Constants.PLAY)
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Controls.playPauseControl(Constants.PAUSE)
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    Controls.playPauseControl(Constants.PAUSE)
                    Handler().postDelayed({
                        Controls.playPauseControl(Constants.PLAY)
                    }, TimeUnit.SECONDS.toMillis(2))
                }

                AudioManager.AUDIOFOCUS_LOSS -> {
                    Controls.playPauseControl(Constants.PAUSE)

                }
            }
        }

    }

}

