package com.duartefilipe.helphealth.util

actual fun downloadPdf(url: String, fileName: String) {
    // iOS dummy implementation
}

actual fun getAvailableRingtones(): List<Pair<String, String>> = emptyList()
actual fun playRingtonePreview(uri: String) {}
actual fun stopRingtonePreview() {}
