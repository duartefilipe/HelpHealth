package com.duartefilipe.helphealth.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Seu medicamento"
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "medicine_alarms",
                "Lembretes de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, "medicine_alarms")
            // .setSmallIcon(R.drawable.ic_launcher_foreground) // We don't have R in shared usually, but we need an icon
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hora do Remédio!")
            .setContentText("Está na hora de tomar: $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(medicineName.hashCode(), notification)
    }
}
