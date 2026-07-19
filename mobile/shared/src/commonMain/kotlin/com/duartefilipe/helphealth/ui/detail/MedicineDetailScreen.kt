package com.duartefilipe.helphealth.ui.detail

import androidx.compose.foundation.background
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
import com.duartefilipe.helphealth.util.NetworkStatus

@Composable
fun MedicineDetailScreen(
    medicine: Medicamentos,
    repository: MedicineRepository,
    networkStatus: NetworkStatus,
    onBackClick: () -> Unit,
    onOpenBulaUrl: (String) -> Unit
) {
    var selectedUf by remember { mutableStateOf("SP") }
    val interchangeability = remember(medicine) { repository.checkInterchangeability(medicine) }
    val precoState = remember(medicine, selectedUf) {
        medicine.ean?.let { repository.getPrecoState(it, selectedUf) }
    }

    val ufList = listOf("SP", "RJ", "MG", "RS", "PR", "SC", "BA", "PE", "CE", "DF", "GO", "ES")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intercambialidade & Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("⬅", fontSize = 20.sp, color = Color.White)
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = medicine.nome_comercial,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            )
                            CategoriaBadge(categoria = medicine.categoria_regulatoria)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Princípio Ativo: ${medicine.principio_ativo}", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text(text = "Dosagem/Forma: ${medicine.concentracao ?: ""} - ${medicine.forma_farmaceutica ?: ""}", fontSize = 14.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TarjaBadge(tarja = medicine.tarja)
                            if (medicine.precisa_refrigeracao == 1L) RefrigeracaoBadge()
                            if (medicine.retencao_receita == 1L) RetencaoReceitaBadge()
                            if (interchangeability.sameManufacturerBadge) SameManufacturerBadge()
                        }
                    }
                }
            }

            // Preço Máximo ao Consumidor (PMC por UF)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preço Máximo (PMC)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("UF: ", fontWeight = FontWeight.Bold)
                                DropdownUfSelector(selectedUf = selectedUf, ufList = ufList, onUfSelected = { selectedUf = it })
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (precoState != null) {
                            Text(text = "PMC (18% ICMS - Teto): R$ ${precoState.pmc_18_icms ?: "N/A"}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                            Text(text = "PMC (0% ICMS): R$ ${precoState.pmc_zero_icms ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)
                        } else {
                            Text(text = "Preço Teto PMC gravado na base local para $selectedUf.", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Bulário Eletrônico & Farmácia Popular (Recursos Online / Offline)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isOnline = networkStatus == NetworkStatus.Available
                    Button(
                        onClick = {
                            medicine.link_bula_paciente?.let { onOpenBulaUrl(it) }
                        },
                        enabled = isOnline && !medicine.link_bula_paciente.isNullOrBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            disabledBackgroundColor = Color.LightGray
                        )
                    ) {
                        Text(if (isOnline) "📄 Abrir Bula Anvisa" else "📄 Bula (Offline - Inativo)")
                    }
                }
            }

            // Seção de Medicamentos Equivalentes (Intercambiais)
            item {
                Text(
                    text = "Genéricos e Similares Intercambiáveis (${interchangeability.equivalents.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }

            items(interchangeability.equivalents) { equivalent ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = equivalent.nome_comercial, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Fabricante: ${equivalent.cnpj_fabricante ?: "Oficial"}", fontSize = 12.sp, color = Color.Gray)
                        }
                        CategoriaBadge(categoria = equivalent.categoria_regulatoria)
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownUfSelector(selectedUf: String, ufList: List<String>, onUfSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(text = selectedUf, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ufList.forEach { uf ->
                DropdownMenuItem(onClick = {
                    onUfSelected(uf)
                    expanded = false
                }) {
                    Text(text = uf)
                }
            }
        }
    }
}
