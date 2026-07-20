package com.duartefilipe.helphealth.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

actual fun scheduleMedicineReminder(medicineName: String, intervalHours: Int) {
    val context = ContextProvider.context
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
        putExtra("MEDICINE_NAME", medicineName)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        medicineName.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAtMillis = System.currentTimeMillis() + (intervalHours * 60 * 60 * 1000L)
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
    )
}
