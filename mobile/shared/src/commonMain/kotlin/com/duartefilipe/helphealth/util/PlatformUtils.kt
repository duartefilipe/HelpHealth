package com.duartefilipe.helphealth.util

expect fun downloadPdf(url: String, fileName: String)

expect fun getAvailableRingtones(): List<Pair<String, String>>
expect fun playRingtonePreview(uri: String)
expect fun stopRingtonePreview()
