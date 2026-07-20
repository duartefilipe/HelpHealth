package com.duartefilipe.helphealth

import App
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.appcompat.app.AppCompatActivity
import com.duartefilipe.helphealth.data.DatabaseDriverFactory
import com.duartefilipe.helphealth.util.ContextProvider

class MainActivity : AppCompatActivity() {

    private val scannedEanState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextProvider.context = this.applicationContext

        val driverFactory = DatabaseDriverFactory(this)
        scannedEanState.value = intent.getStringExtra("SCANNED_EAN")

        setContent {
            App(
                databaseDriverFactory = driverFactory,
                onScanBarcodeClick = {
                    val intent = Intent(this, BarcodeScannerActivity::class.java)
                    startActivity(intent)
                },
                scannedBarcodeQuery = scannedEanState.value,
                onOpenUrl = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        scannedEanState.value = intent.getStringExtra("SCANNED_EAN")
    }
}