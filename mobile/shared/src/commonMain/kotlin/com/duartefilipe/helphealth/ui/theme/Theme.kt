package com.duartefilipe.helphealth.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF00897B)
val PrimaryDarkGreen = Color(0xFF005B4F)
val SecondaryBlue = Color(0xFF0288D1)

val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)

// Badges Clínicos
val ColorTarjaVermelha = Color(0xFFD32F2F)
val ColorTarjaPreta = Color(0xFF212121)
val ColorTarjaAmarela = Color(0xFFFBC02D)
val ColorTarjaIsento = Color(0xFF00897B)

val ColorRefrigeracao = Color(0xFF0288D1)
val ColorRetencaoReceita = Color(0xFFE65100)
val ColorSeloFabricante = Color(0xFF2E7D32)
val ColorFarmaciaPopular = Color(0xFF1565C0)

private val LightColorPalette = lightColors(
    primary = PrimaryGreen,
    primaryVariant = PrimaryDarkGreen,
    secondary = SecondaryBlue,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val DarkColorPalette = darkColors(
    primary = PrimaryGreen,
    primaryVariant = PrimaryDarkGreen,
    secondary = SecondaryBlue,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun HelpHealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        content = content
    )
}
