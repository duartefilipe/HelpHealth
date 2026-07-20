package com.duartefilipe.helphealth.util

expect fun scheduleMedicineReminder(
    alarmId: Long,
    medicineName: String,
    dose: String,
    timeMillis: Long,
    intervalHours: Int?,
    ringtoneUri: String?
)

expect fun cancelMedicineReminder(alarmId: Long)
