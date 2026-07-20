package com.duartefilipe.helphealth.util

actual fun downloadPdf(url: String, fileName: String) {
    println("Download PDF not implemented for Desktop: $url -> $fileName")
}

actual fun getAvailableRingtones(): List<Pair<String, String>> = emptyList()
actual fun playRingtonePreview(uri: String) {}
actual fun stopRingtonePreview() {}
