package com.example.basicalarmclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var ringtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "STOP_ALARM" -> stopRingtone()
            else -> {
                val ringtoneUriString = intent.getStringExtra("RINGTONE_URI")
                val ringtoneUri = Uri.parse(ringtoneUriString)
                startRingtone(context, ringtoneUri)
            }
        }
    }

    private fun startRingtone(context: Context, ringtoneUri: Uri) {
        stopRingtone() // Stop any existing ringtone first
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }
}
