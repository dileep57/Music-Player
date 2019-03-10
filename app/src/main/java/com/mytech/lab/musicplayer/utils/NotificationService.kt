package com.mytech.lab.musicplayer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
import com.mytech.lab.musicplayer.Activity.Home
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.R
import com.mytech.lab.musicplayer.SongService

@Suppress("DEPRECATION")
class NotificationService {

    companion object {

        val CHANNEL_ID: String = "my_musicPlayer"
        var NOTIFICATION_ID = 1111
        lateinit var notification: Notification

        public fun newnotification(applicationContext: Context) {

            val notificationIntent = Intent(applicationContext, Home::class.java)
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(applicationContext, 99, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            SongService.simpleContentView = RemoteViews(applicationContext.packageName, R.layout.custom_notification)
            if (SongService.currentVersionSupportBigNotification) {
                SongService.expandedView = RemoteViews(applicationContext.packageName, R.layout.big_notification)
            }
            updateNotificationData(applicationContext)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channelId = createNotificationChannel("my_service", "NotificationService", applicationContext)
                    val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                    notification = notificationBuilder.setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .setContentIntent(pendingIntent)
                            .setCustomBigContentView(SongService.expandedView)
                            .setContent(SongService.simpleContentView)
                            .setSmallIcon(R.drawable.headphones_tick)
                            .build()

                } else {
                    notification = NotificationCompat.Builder(applicationContext, "notify")
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCustomBigContentView(SongService.expandedView)
                            .setContent(SongService.simpleContentView)
                            .setSmallIcon(R.drawable.headphones_tick)
                            .setChannelId(CHANNEL_ID)
                            .build()
                }


            } else {
                notification = NotificationCompat.Builder(applicationContext, "notify")
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSmallIcon(R.drawable.headphones_tick)
                        .build()
            }

            setListeners(SongService.simpleContentView, applicationContext)
            if (SongService.currentVersionSupportBigNotification)
                setListeners(SongService.expandedView, applicationContext)



            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                notification.contentView = SongService.simpleContentView
                if (SongService.currentVersionSupportBigNotification)
                    notification.bigContentView = SongService.expandedView
            }

        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(channelId: String, channelName: String, applicationContext: Context): String {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val service = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            return channelId
        }


        private fun setListeners(view: RemoteViews, applicationContext: Context) {
            var previous:Intent? = null
            var delete:Intent? = null
            var pauseplay:Intent? = null
            var next:Intent? = null


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                previous = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(SongService.NOTIFY_PREVIOUS);
                delete = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(SongService.NOTIFY_DELETE);
                pauseplay = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(SongService.NOTIFY_PAUSEPLAY);
                next = Intent(applicationContext, PhoneStateReceiver::class.java).setAction(SongService.NOTIFY_NEXT)
            } else {
                previous = Intent(SongService.NOTIFY_PREVIOUS)
                delete = Intent(SongService.NOTIFY_DELETE)
                pauseplay = Intent(SongService.NOTIFY_PAUSEPLAY)
                next = Intent(SongService.NOTIFY_NEXT)
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

        public fun updateNotificationData(applicationContext: Context) {
            try {
                val albumId = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.albumId
                val albumArt = Constants.getAlbumart(applicationContext, albumId)
                if (albumArt != null) {
                    SongService.simpleContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt)
                    SongService.expandedView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt)

                } else {
                    SongService.simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.general_player_small_art)
                    SongService.expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.general_player_small_art)

                }
            } catch (e: Exception) {
                Log.i("error",e.message)
            }

            var songname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.song_name
            var albumname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.album_name
            var artistname: String = Constants.SONGS_LIST.get(Constants.SONG_NUMBER).first.artist
            SongService.simpleContentView.setTextViewText(R.id.textSongName, songname)
            SongService.simpleContentView.setTextViewText(R.id.textAlbumName, albumname)
            SongService.expandedView.setTextViewText(R.id.textSongName, songname)
            SongService.expandedView.setTextViewText(R.id.textAlbumName, albumname)
            SongService.expandedView.setTextViewText(R.id.textArtistname, artistname)
        }


        fun updatenotificationIcon(applicationContext: Context) {
            if (Constants.SONG_PAUSED) {
                SongService.simpleContentView.setImageViewResource(R.id.playpause, R.drawable.play_white)
                SongService.expandedView.setImageViewResource(R.id.playpause, R.drawable.play_white)
            } else {
                SongService.simpleContentView.setImageViewResource(R.id.playpause, R.drawable.pause_white)
                SongService.expandedView.setImageViewResource(R.id.playpause, R.drawable.pause_white)
            }
            startNotify(applicationContext)
        }


        fun startNotify(applicationContext: Context) {

            var mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(NOTIFICATION_ID, notification)
        }

    }

}
