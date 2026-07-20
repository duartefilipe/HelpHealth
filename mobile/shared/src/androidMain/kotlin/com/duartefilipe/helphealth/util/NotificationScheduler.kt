package com.duartefilipe.helphealth.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

actual fun scheduleMedicineReminder(
    alarmId: Long,
    medicineName: String,
    dose: String,
    timeMillis: Long,
    intervalHours: Int?,
    ringtoneUri: String?
) {
    val context = ContextProvider.context
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
        putExtra("ALARM_ID", alarmId)
        putExtra("MEDICINE_NAME", medicineName)
        putExtra("DOSE", dose)
        putExtra("INTERVAL_HOURS", intervalHours ?: -1)
        putExtra("RINGTONE_URI", ringtoneUri)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val finalTimeMillis = if (timeMillis == 0L && intervalHours != null && intervalHours > 0) {
        System.currentTimeMillis() + (intervalHours * 60 * 60 * 1000L)
    } else if (timeMillis == 0L) {
        System.currentTimeMillis() + 5000L // fallback para 5s se não houver tempo válido
    } else {
        timeMillis
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        finalTimeMillis,
        pendingIntent
    )
}

actual fun cancelMedicineReminder(alarmId: Long) {
    val context = ContextProvider.context
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MedicineAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}
