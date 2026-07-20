package com.duartefilipe.helphealth.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.media.RingtoneManager
import android.media.Ringtone

private var currentRingtone: Ringtone? = null

actual fun downloadPdf(url: String, fileName: String) {
    val context = ContextProvider.context
    val request = DownloadManager.Request(Uri.parse(url)).apply {
        setTitle("Bula: $fileName")
        setDescription("Baixando bula do paciente...")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$fileName.pdf")
        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}

actual fun getAvailableRingtones(): List<Pair<String, String>> {
    val context = ContextProvider.context
    val ringtoneManager = RingtoneManager(context)
    ringtoneManager.setType(RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_NOTIFICATION)
    val cursor = ringtoneManager.cursor
    val list = mutableListOf<Pair<String, String>>()
    list.add(Pair("", "Padrão do Sistema"))
    if (cursor != null && cursor.moveToFirst()) {
        do {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            list.add(Pair(uri, title))
        } while (cursor.moveToNext())
    }
    return list
}

actual fun playRingtonePreview(uri: String) {
    stopRingtonePreview()
    if (uri.isBlank()) return
    val context = ContextProvider.context
    currentRingtone = RingtoneManager.getRingtone(context, Uri.parse(uri))
    currentRingtone?.play()
}

actual fun stopRingtonePreview() {
    currentRingtone?.stop()
    currentRingtone = null
}
