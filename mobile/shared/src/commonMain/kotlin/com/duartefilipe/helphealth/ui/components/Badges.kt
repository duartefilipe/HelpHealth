package com.duartefilipe.helphealth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duartefilipe.helphealth.ui.theme.*

@Composable
fun BaseBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    iconPrefix: String? = null
) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconPrefix != null) {
                Text(
                    text = "$iconPrefix ",
                    fontSize = 11.sp
                )
            }
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TarjaBadge(tarja: String?) {
    val (bg, text, textColor) = when (tarja?.lowercase()) {
        "preta", "tarja preta" -> Triple(ColorTarjaPreta, "Tarja Preta", Color.White)
        "vermelha", "tarja vermelha" -> Triple(ColorTarjaVermelha, "Tarja Vermelha", Color.White)
        "amarela", "generico", "genérico" -> Triple(ColorTarjaAmarela, "Genérico", Color.Black)
        else -> Triple(ColorTarjaIsento, "Isento de Prescrição", Color.White)
    }

    BaseBadge(text = text, backgroundColor = bg, textColor = textColor)
}

@Composable
fun RefrigeracaoBadge() {
    BaseBadge(
        text = "Termolábil (Refrigeração)",
        backgroundColor = ColorRefrigeracao,
        iconPrefix = "❄️"
    )
}

@Composable
fun RetencaoReceitaBadge() {
    BaseBadge(
        text = "Retenção de Receita",
        backgroundColor = ColorRetencaoReceita,
        iconPrefix = "📋"
    )
}

@Composable
fun SameManufacturerBadge() {
    BaseBadge(
        text = "Selo do Fabricante",
        backgroundColor = ColorSeloFabricante,
        iconPrefix = "🏷️"
    )
}

@Composable
fun FarmaciaPopularBadge() {
    BaseBadge(
        text = "Farmácia Popular",
        backgroundColor = ColorFarmaciaPopular,
        iconPrefix = "🏥"
    )
}

@Composable
fun CategoriaBadge(categoria: String?) {
    val bg = when (categoria?.uppercase()) {
        "REFERENCIA", "REFERÊNCIA" -> Color(0xFF1565C0)
        "GENERICO", "GENÉRICO" -> Color(0xFFFBC02D)
        else -> Color(0xFF7B1FA2)
    }
    val textColor = if (categoria?.uppercase()?.contains("GEN") == true) Color.Black else Color.White
    BaseBadge(text = categoria ?: "OUTROS", backgroundColor = bg, textColor = textColor)
}
