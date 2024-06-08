package com.example.basicalarmclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setAlarmButton: Button
    private lateinit var alarmToggle: Switch
    private lateinit var stopAlarmButton: Button
    private lateinit var selectRingtoneButton: Button
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private var selectedRingtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    companion object {
        private const val RINGTONE_PICKER_REQUEST_CODE = 999
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timePicker = findViewById(R.id.timePicker)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        alarmToggle = findViewById(R.id.alarmToggle)
        stopAlarmButton = findViewById(R.id.stopAlarmButton)
        selectRingtoneButton = findViewById(R.id.selectRingtoneButton)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                }
                startActivity(intent)
            }
        }

        setAlarmButton.setOnClickListener {
            setAlarm()
        }

        alarmToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setAlarm()
            } else {
                cancelAlarm()
            }
        }

        stopAlarmButton.setOnClickListener {
            stopAlarm()
        }

        selectRingtoneButton.setOnClickListener {
            selectRingtone()
        }
    }

    private fun setAlarm() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        calendar.set(Calendar.MINUTE, timePicker.minute)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("RINGTONE_URI", selectedRingtoneUri.toString())
        }
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Exact alarms are not permitted. Please enable it in settings.", Toast.LENGTH_LONG).show()
            return
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm() {
        if (::pendingIntent.isInitialized) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun stopAlarm() {
        sendBroadcast(Intent(this, AlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
        })
    }

    private fun selectRingtone() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri)
        }
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedRingtoneUri = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }
}
