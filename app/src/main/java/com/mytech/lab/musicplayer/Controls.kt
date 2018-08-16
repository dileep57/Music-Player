package com.mytech.lab.musicplayer

import android.content.Context


object Controls {

    var LOG_CLASS = "Controls"


    fun playPauseControl(command:String)
    {
        sendMessage(command)
    }


    fun shuffle_repeat()
    {
        try
        {
            Constants.SHUFFLE_REPEAT?.sendMessage(Constants.SHUFFLE_REPEAT?.obtainMessage())
        } catch (e: Exception) { }
    }

    fun nextControl(context: Context) {
        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context)
        if (!isServiceRunning)
            return


        if (Constants.SONGS_LIST.size > 0) {
            if (Constants.SONG_NUMBER < Constants.SONGS_LIST.size - 1) {
                if(Constants.SONG_SHUFFLE)
                {
                    Constants.SONG_NUMBER = Constants.shuffle_song(Constants.SONG_NUMBER)
                }
                else if(Constants.SONG_REPEAT)
                {
                    Constants.SONG_NUMBER
                }
                else
                {
                    Constants.SONG_NUMBER++
                }

                Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,"false"))
            } else {
                Constants.SONG_NUMBER = 0
                Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,"false"))
            }
        }
        Constants.SONG_PAUSED = false


    }

    fun previousControl(context: Context) {
        val isServiceRunning = Constants.isServiceRunning(SongService::class.java.getName(), context)
        if (!isServiceRunning)
            return
        if (Constants.SONGS_LIST.size > 0) {
            if (Constants.SONG_NUMBER > 0) {

                if(Constants.SONG_REPEAT)
                {
                    Constants.SONG_NUMBER
                }
                else
                {
                    Constants.SONG_NUMBER--
                }
                Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,"false"))
            } else {
                Constants.SONG_NUMBER = Constants.SONGS_LIST.size - 1
                Constants.SONG_CHANGE_HANDLER?.sendMessage(Constants.SONG_CHANGE_HANDLER?.obtainMessage(0,"false"))
            }
        }
        Constants.SONG_PAUSED = false


    }

    private fun sendMessage(message: String) {
        try {
            Constants.PLAY_PAUSE_HANDLER?.sendMessage(Constants.PLAY_PAUSE_HANDLER?.obtainMessage(0, message))
        } catch (e: Exception) {
        }



    }
}