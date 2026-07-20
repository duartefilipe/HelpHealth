package com.duartefilipe.helphealth.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.search.MedicineCard

@Composable
fun FavoritesScreen(
    repository: MedicineRepository,
    onMedicineSelect: (Medicamentos) -> Unit
) {
    var favorites by remember { mutableStateOf<List<Medicamentos>>(emptyList()) }

    // Fetch favorites on launch
    LaunchedEffect(Unit) {
        favorites = repository.getFavoritosPaged(0, 100)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Meus Favoritos", fontWeight = FontWeight.Bold) },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        )

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Você ainda não tem medicamentos favoritos.", color = androidx.compose.ui.graphics.Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { medicine ->
                    MedicineCard(medicine = medicine, onClick = { onMedicineSelect(medicine) })
                }
            }
        }
    }
}
