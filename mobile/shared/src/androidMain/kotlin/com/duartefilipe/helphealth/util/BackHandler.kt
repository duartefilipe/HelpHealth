package com.duartefilipe.helphealth.util

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler as AndroidBackHandler

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    AndroidBackHandler(enabled = isEnabled, onBack = onBack)
}
