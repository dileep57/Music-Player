package com.mytech.lab.musicplayer.utils

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.mytech.lab.musicplayer.Controls


class CallListner : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                Controls.playPauseControl("play")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Controls.playPauseControl("pause")
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                Controls.playPauseControl("pause")
            }
        }
    }

}