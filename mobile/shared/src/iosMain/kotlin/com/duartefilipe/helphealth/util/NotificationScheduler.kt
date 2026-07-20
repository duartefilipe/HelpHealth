package com.duartefilipe.helphealth.util

actual fun scheduleMedicineReminder(
    alarmId: Long,
    medicineName: String,
    dose: String,
    timeMillis: Long,
    intervalHours: Int?,
    ringtoneUri: String?
) {
    // iOS dummy implementation
}

actual fun cancelMedicineReminder(alarmId: Long) {
    // iOS dummy implementation
}
