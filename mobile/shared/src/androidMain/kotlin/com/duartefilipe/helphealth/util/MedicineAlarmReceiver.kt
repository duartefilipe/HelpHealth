package com.duartefilipe.helphealth.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import android.media.AudioAttributes

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Seu medicamento"
        val dose = intent.getStringExtra("DOSE") ?: ""
        val intervalHours = intent.getIntExtra("INTERVAL_HOURS", -1)
        val ringtoneUriStr = intent.getStringExtra("RINGTONE_URI")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "medicine_alarms_${ringtoneUriStr.hashCode()}"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            )
            if (!ringtoneUriStr.isNullOrBlank()) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                channel.setSound(Uri.parse(ringtoneUriStr), audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hora do Remédio: $medicineName")
            .setContentText(if (dose.isNotBlank()) "Tomar: $dose" else "Está na hora de tomar seu medicamento.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (!ringtoneUriStr.isNullOrBlank() && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setSound(Uri.parse(ringtoneUriStr))
        }

        notificationManager.notify(alarmId.toInt(), notificationBuilder.build())

        if (intervalHours > 0) {
            val nextTimeMillis = System.currentTimeMillis() + (intervalHours * 60 * 60 * 1000L)
            scheduleMedicineReminder(
                alarmId = alarmId,
                medicineName = medicineName,
                dose = dose,
                timeMillis = nextTimeMillis,
                intervalHours = intervalHours,
                ringtoneUri = ringtoneUriStr
            )
        }
    }
}
