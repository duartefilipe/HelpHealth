package com.duartefilipe.helphealth.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.components.*

@Composable
fun SearchScreen(
    repository: MedicineRepository,
    onMedicineSelect: (Medicamentos) -> Unit,
    onScanBarcodeClick: () -> Unit,
    onSyncClick: () -> Unit = {},
    searchQueryOverride: String? = null
) {
    var searchQuery by remember { mutableStateOf(searchQueryOverride ?: "") }
    var searchResults by remember { mutableStateOf(repository.searchMedicamentos(searchQuery)) }
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (searchResults.isEmpty()) {
            searchResults = repository.getAllMedicamentos()
        }
    }

    LaunchedEffect(searchQueryOverride) {
        if (!searchQueryOverride.isNullOrBlank()) {
            searchQuery = searchQueryOverride
            searchResults = repository.searchMedicamentos(searchQueryOverride)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HelpHealth 🟢 Online", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            isSyncing = true
                            onSyncClick()
                            searchResults = repository.getAllMedicamentos()
                            isSyncing = false
                        }
                    ) {
                        Text(if (isSyncing) "⏳" else "🔄", fontSize = 18.sp, color = Color.White)
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchResults = repository.searchMedicamentos(it)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Pesquisar por Remédio, Princípio ou EAN...") },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        cursorColor = MaterialTheme.colors.primary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onScanBarcodeClick,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Text("📷", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Digite o nome ou escaneie o código de barras." else "Nenhum medicamento encontrado.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { medicine ->
                        MedicineCard(medicine = medicine, onClick = { onMedicineSelect(medicine) })
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCard(medicine: Medicamentos, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 3.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medicine.nome_comercial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.primary
                )
                CategoriaBadge(categoria = medicine.categoria_regulatoria)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fórmula: ${medicine.principio_ativo}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )

            if (!medicine.concentracao.isNullOrBlank() || !medicine.forma_farmaceutica.isNullOrBlank()) {
                Text(
                    text = "${medicine.concentracao ?: ""} - ${medicine.forma_farmaceutica ?: ""}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (!medicine.ean.isNullOrBlank()) {
                Text(
                    text = "EAN: ${medicine.ean}",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TarjaBadge(tarja = medicine.tarja)
                if (medicine.precisa_refrigeracao == 1L) {
                    RefrigeracaoBadge()
                }
                if (medicine.retencao_receita == 1L) {
                    RetencaoReceitaBadge()
                }
                if (medicine.faz_parte_farmacia_popular == 1L) {
                    FarmaciaPopularBadge()
                }
            }
        }
    }
}
