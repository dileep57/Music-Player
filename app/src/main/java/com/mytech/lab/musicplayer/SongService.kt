package com.mytech.lab.musicplayer

import android.app.*
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.os.IBinder
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
import android.util.Log
import android.widget.Toast
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Fragments.Recent_song
import com.mytech.lab.musicplayer.utils.NotificationService
import com.mytech.lab.musicplayer.utils.PhoneStateReceiver
import com.mytech.lab.musicplayer.utils.Song_base
import org.apache.commons.collections4.CollectionUtils
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class SongService : Service(){


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    lateinit var audioManager: AudioManager
    lateinit var mEqualizer: Equalizer
    lateinit var mVqualizer: Visualizer
    private var remoteComponentName: ComponentName? = null
    private var mOnAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private var remoteControlClient: RemoteControlClient? = null
    internal var mDummyAlbumArt: Bitmap? = null

    var maxvol: Int = 0
    var currentvol: Int = 0
    var duck_volume: Double = 0.0


    companion object {
        var mPlayer: MediaPlayer? = null

        val NOTIFY_PREVIOUS = "com.mytech.lab.musicplayer.previous"
        val NOTIFY_DELETE = "com.mytech.lab.musicplayer.delete"
        val NOTIFY_PAUSEPLAY = "com.mytech.lab.musicplayer.pause"
        val NOTIFY_NEXT = "com.mytech.lab.musicplayer.next"

        lateinit var simpleContentView: RemoteViews
        lateinit var expandedView: RemoteViews

        var currentVersionSupportBigNotification = false
        var currentVersionSupportLockScreenControls = false

    }


    override fun onCreate() {

        mPlayer = MediaPlayer()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        maxvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        currentvol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        duck_volume = currentvol.toDouble()

        mPlayer!!.setOnCompletionListener {
            Controls.nextControl(getApplicationContext())
        }

        currentVersionSupportBigNotification = Constants.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = Constants.currentVersionSupportLockScreenControls();
        super.onCreate()

        try {

            if (CollectionUtils.isEmpty(Constants.SONGS_LIST)) {
                for (i in Home.servicearraylist.indices) {
                    Constants.SONGS_LIST.add(Pair(Home.servicearraylist[i].first, Home.servicearraylist[i].second))
                }
            }
            if (currentVersionSupportLockScreenControls) {
                registerRemoteClient()
            }

            val s: Song_base = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first
            var songPath: String = s.url
            playSong(songPath, s, applicationContext)
            Constants.SONG_PAUSED = false
            NotificationService.newnotification(applicationContext)
            startNotificationService(applicationContext)
            collectsongdata(s, applicationContext)
            NotificationService.updatenotificationIcon(applicationContext)
            Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0))

            songChangeHandler(applicationContext)

            playPauseHandler(applicationContext)

            shuffleRepeatHandler(applicationContext)

            initiliseAudioFocusChangeListner()


        } catch (e: Exception) {
            Controls.createToast(this, e.message!!,Toast.LENGTH_SHORT)
        }

    }

    public  fun songChangeHandler(applicationContext: Context) {

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
                    SongService.mPlayer?.pause()
                    return false
                }

                try {
                    Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0, null))
                    playSong(songPath, s, applicationContext)
                } catch (e: Exception) {
                    Log.e("ERROR", e.message)
                }
                collectsongdata(s, applicationContext)
                NotificationService.updateNotificationData(applicationContext)
                NotificationService.updatenotificationIcon(applicationContext)
                NotificationService.startNotify(applicationContext)
                return true

            }

        })
    }

    fun playSong(songPath: String, s: Song_base, applicationContext: Context) {
        try {
            if (SongService.currentVersionSupportLockScreenControls) {
                updateMetadata(s, applicationContext)
                remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING)
            }
            SongService.mPlayer?.reset()
            SongService.mPlayer?.setDataSource(songPath)
            SongService.mPlayer?.prepare()
            SongService.mPlayer?.start()
            mEqualizer = Equalizer(0, SongService.mPlayer!!.audioSessionId)

            mEqualizer.setEnabled(true)
            val timer: Timer = Timer(true)
            timer.scheduleAtFixedRate(MainTask(), 0, 100)
        } catch (e: Exception) {
            Home.filenotsupport(applicationContext)
            Controls.nextControl(applicationContext)
        }
    }

    fun registerRemoteClient() {
        remoteComponentName = ComponentName(applicationContext, PhoneStateReceiver().ComponentName())
        try {
            if (remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName)
                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
                mediaButtonIntent.component = remoteComponentName
                val mediaPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0)
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

    fun startNotificationService(applicationContext: Context) {
        startForeground(NotificationService.NOTIFICATION_ID, NotificationService.notification)
    }

    fun updateMetadata(data: Song_base, cntx: Context) {
        if (remoteControlClient == null)
            return
        val metadataEditor = remoteControlClient?.editMetadata(true)
        metadataEditor?.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.album_name)
        metadataEditor?.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.artist)
        metadataEditor?.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.song_name)
        mDummyAlbumArt = Constants.getAlbumart(cntx, data.albumId)
        if (mDummyAlbumArt == null) {
            mDummyAlbumArt = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background)
        }
        metadataEditor?.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt)
        metadataEditor?.apply()
        audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    public fun collectsongdata(s: Song_base, applicationContext:Context) {
        val lambda1=Thread{
            val actual_pos = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).second
            Constants.mediaAfterprepared(null, applicationContext, s, actual_pos, Constants.SONG_NUMBER,
                    "general", Home.shared.getString(Constants.CURRENT_ALBUM, "alb"), Home.shared.getString(Constants.PLAYLIST_NAME, "alb"))
            Constants.databasedata(s, applicationContext, actual_pos, "RecentSong")
            Recent_song().updaterecentsong(applicationContext)
            Recent_song().setvisibility()
        }
        lambda1.start()

    }

    public fun playPauseHandler(applicationContext: Context) {

        Constants.PLAY_PAUSE_HANDLER = Handler(object : Handler.Callback {

            override fun handleMessage(msg: Message?): Boolean {

                var message: String = msg!!.obj as String

                if (SongService.mPlayer == null)
                    return false

                if (message.equals(Constants.PLAY)) {
                    Constants.SELF_CHANGE = false
                    Constants.SONG_PAUSED = false
                    if (SongService.currentVersionSupportLockScreenControls) {
                        remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING)
                    }
                    SongService.mPlayer!!.start()

                } else if (message.equals(Constants.PAUSE)) {
                    Constants.SELF_CHANGE = true
                    Constants.SONG_PAUSED = true
                    if (SongService.currentVersionSupportLockScreenControls) {
                        remoteControlClient?.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED)
                    }
                    SongService.mPlayer!!.pause()
                }
                Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0, null))
                NotificationService.updatenotificationIcon(applicationContext)
                NotificationService.startNotify(applicationContext)
                return true

            }

        })

    }
    fun shuffleRepeatHandler(applicationContext: Context) {

        Constants.SHUFFLE_REPEAT = Handler(object : Handler.Callback {

            override fun handleMessage(msg: Message?): Boolean {
                Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0, null))
                return true

            }
        })

    }


    fun initiliseAudioFocusChangeListner(){

        mOnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            if (SongService.mPlayer?.isPlaying != null && !Constants.SELF_CHANGE) {

                when (focusChange) {
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


    val handler = Handler(object : Handler.Callback {
        override fun handleMessage(p0: Message?): Boolean {
            if (mPlayer != null) {
                val progress = mPlayer!!.getCurrentPosition() * 100 / mPlayer!!.getDuration()
                val i = arrayOfNulls<Int>(4)
                i[0] = mPlayer!!.currentPosition
                i[1] = mPlayer!!.getDuration()

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
            if (mPlayer != null && mPlayer!!.isPlaying) {
                handler.sendEmptyMessage(0)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }


    override fun onDestroy() {
        if (mPlayer != null) {
            mPlayer!!.stop()
            mPlayer = null
        }
        super.onDestroy()
    }



}



//    private fun requestAudioFocus(): Boolean {
//        if (!mAudioFocusGranted) {
//            val am = mContext
//                    .getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            // Request audio focus for play back
//            val result = am.requestAudioFocus(mOnAudioFocusChangeListener,
//                    // Use the music stream.
//                    AudioManager.STREAM_MUSIC,
//                    // Request permanent focus.
//                    AudioManager.AUDIOFOCUS_GAIN)
//
//            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                mAudioFocusGranted = true
//            } else {
//                Log.e("TAG", ">>>>>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<")
//            }
//        }
//        return mAudioFocusGranted
//    }
//
//    private fun abandonAudioFocus() {
//        val am = mContext
//                .getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        val result = am.abandonAudioFocus(mOnAudioFocusChangeListener)
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            mAudioFocusGranted = false
//        } else {
//            // FAILED
//            Log.e("TAG",
//                    ">>>>>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<")
//        }
//        mOnAudioFocusChangeListener = null
//    }



//}

