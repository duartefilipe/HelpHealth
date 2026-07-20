package com.duartefilipe.helphealth.util

actual fun scheduleMedicineReminder(
    alarmId: Long,
    medicineName: String,
    dose: String,
    timeMillis: Long,
    intervalHours: Int?,
    ringtoneUri: String?
) {
    println("Scheduling medicine reminder on Desktop not implemented")
}

actual fun cancelMedicineReminder(alarmId: Long) {
    println("Canceling medicine reminder on Desktop not implemented")
}
