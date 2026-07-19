package com.duartefilipe.helphealth

import App
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.duartefilipe.helphealth.data.DatabaseDriverFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val driverFactory = DatabaseDriverFactory(this)
        val scannedEan = intent.getStringExtra("SCANNED_EAN")

        setContent {
            App(
                databaseDriverFactory = driverFactory,
                onScanBarcodeClick = {
                    val intent = Intent(this, BarcodeScannerActivity::class.java)
                    startActivity(intent)
                },
                scannedBarcodeQuery = scannedEan,
                onOpenUrl = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            )
        }
    }
}