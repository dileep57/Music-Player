package com.mytech.lab.musicplayer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.mytech.lab.musicplayer.Constants
import com.mytech.lab.musicplayer.Controls
import com.mytech.lab.musicplayer.SongService
import java.util.concurrent.TimeUnit


/**
 * Created by lnx on 26/3/18.
 */
public class PhoneStateReceiver : BroadcastReceiver(){

    var mgr:TelephonyManager? = null
    override fun onReceive(context: Context?, intent: Intent?) {

        try {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            mgr = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            if (intent!!.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                when (state) {
                    0 -> {
                        Controls.playPauseControl(Constants.PAUSE)
                        Log.i("TAG", "Headset is unplugged")
                    }
                    1 -> Log.i("TAG","HEADSET PLUGIN")
                    else -> Log.d("TAG", "I have no idea what the headset state is")
                }

            }
                if (intent!!.action == Intent.ACTION_MEDIA_BUTTON)
                {
                    val keyEvent = intent.extras!!.get(Intent.EXTRA_KEY_EVENT) as KeyEvent
                    if (keyEvent.action != KeyEvent.ACTION_DOWN)
                        return

                    when (keyEvent.keyCode)
                    {
                        KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> if (!Constants.SONG_PAUSED) {
//                            Toast.makeText(context,Constants.HEADPHONE,Toast.LENGTH_SHORT).show()
                            Controls.playPauseControl(Constants.PAUSE)

                        } else {
                            Controls.playPauseControl(Constants.PLAY)
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            Controls.previousControl(context)
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            Controls.nextControl(context)
                        }

                    }
                }

                if (intent.action == SongService.NOTIFY_PAUSEPLAY)
                {

                    var status: String = if(Constants.SONG_PAUSED){
                        Constants.PLAY
                    } else {
                        Constants.PAUSE
                    }

                    Controls.playPauseControl(status)

                }
                else if (intent.action == SongService.NOTIFY_NEXT) {
                    Controls.nextControl(context)

                }
                else if (intent.action == SongService.NOTIFY_DELETE) {

                    val i = Intent(context, SongService::class.java)
                    context.stopService(i)
                    Constants.SONG_PAUSED = true
                    Constants.PLAYER_UI?.sendMessage(Constants.PLAYER_UI?.obtainMessage(0))

                }
                else if (intent.action == SongService.NOTIFY_PREVIOUS) {
                    Controls.previousControl(context)
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun componentName(): String {
        return this.javaClass.name
    }



}