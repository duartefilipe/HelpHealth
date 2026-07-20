package com.duartefilipe.helphealth.util

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(isEnabled: Boolean, onBack: () -> Unit) {
    // No-op on Desktop
}
