package com.duartefilipe.helphealth.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

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
