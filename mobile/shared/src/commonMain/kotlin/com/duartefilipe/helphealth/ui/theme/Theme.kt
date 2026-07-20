package com.duartefilipe.helphealth.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryPurple = Color(0xFF8A2BE2) // Roxo Vibrante (estilo CliqueFarma)
val PrimaryDarkPurple = Color(0xFF4A148C)
val SecondaryColor = Color(0xFF00C853) // Verde secundário para sucesso/preço

val BackgroundLight = Color(0xFFF9F9F9) // Fundo super limpo e sutil
val SurfaceLight = Color(0xFFFFFFFF) // Cartões perfeitamente brancos

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
    primary = PrimaryPurple,
    primaryVariant = PrimaryDarkPurple,
    secondary = SecondaryColor,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121)
)

private val DarkColorPalette = darkColors(
    primary = Color(0xFFB388FF), // Roxo claro legível no escuro
    primaryVariant = PrimaryPurple,
    secondary = SecondaryColor,
    background = Color(0xFF121212), // Cinza escuro elegante, não preto absoluto
    surface = Color(0xFF1E1E1E), // Cartões levemente mais claros
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5)
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
