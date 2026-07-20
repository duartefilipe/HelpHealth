package com.duartefilipe.helphealth.ui.detail

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.EquivalentMedicine
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.components.*
import com.duartefilipe.helphealth.util.NetworkStatus
import com.duartefilipe.helphealth.util.downloadPdf
import com.duartefilipe.helphealth.util.scheduleMedicineReminder

import com.russhwolf.settings.Settings

@Composable
fun MedicineDetailScreen(
    medicine: Medicamentos,
    repository: MedicineRepository,
    networkStatus: NetworkStatus,
    onBackClick: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val settings = remember { Settings() }
    var selectedUf by remember { mutableStateOf(settings.getString("last_selected_uf", "SP")) }
    val interchangeability = remember(medicine, selectedUf) { repository.checkInterchangeability(medicine, selectedUf) }
    
    // Favoritos
    var isFavorito by remember(medicine.ean) { 
        mutableStateOf(medicine.ean?.let { repository.isFavorito(it) } ?: false)
    }

    val precoState = remember(medicine, selectedUf) {
        settings.putString("last_selected_uf", selectedUf)
        medicine.ean?.let { repository.getPrecoState(it, selectedUf) }
    }

    val ufList = listOf("SP", "RJ", "MG", "RS", "PR", "SC", "BA", "PE", "CE", "DF", "GO", "ES")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Medicamento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    medicine.ean?.let { ean ->
                        IconButton(onClick = {
                            repository.toggleFavorito(ean, !isFavorito)
                            isFavorito = !isFavorito
                        }) {
                            Icon(
                                imageVector = if (isFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favoritar",
                                tint = if (isFavorito) Color.Red else Color.White
                            )
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 0.dp
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
                    elevation = 0.dp,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = medicine.nome_comercial,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CategoriaBadge(categoria = medicine.categoria_regulatoria)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(text = "Princípio Ativo", fontSize = 12.sp, color = Color.Gray)
                        Text(text = medicine.principio_ativo, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.DarkGray)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(text = "Dosagem e Forma", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "${medicine.concentracao ?: ""} • ${medicine.forma_farmaceutica ?: ""}", fontSize = 15.sp, color = Color.DarkGray)

                        if (!medicine.ean.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Código EAN", fontSize = 12.sp, color = Color.Gray)
                            Text(text = medicine.ean, fontSize = 15.sp, color = Color.DarkGray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Wrap row of badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TarjaBadge(tarja = medicine.tarja)
                            if (medicine.precisa_refrigeracao == 1L) RefrigeracaoBadge()
                            if (medicine.retencao_receita == 1L) RetencaoReceitaBadge()
                            if (interchangeability.sameManufacturerBadge) SameManufacturerBadge()
                        }
                    }
                }
            }

            // Descrição (Pra que serve?)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pra que serve?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Este medicamento contém o princípio ativo ${medicine.principio_ativo}. A indicação exata depende da concentração e forma farmacêutica. Para obter as indicações terapêuticas precisas, posologia e contraindicações, consulte a bula oficial do paciente clicando abaixo.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Preço Máximo ao Consumidor (PMC por UF)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Preço Máximo (PMC)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("UF: ", fontWeight = FontWeight.Bold, color = Color.Gray)
                                DropdownUfSelector(selectedUf = selectedUf, ufList = ufList, onUfSelected = { selectedUf = it })
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (precoState != null) {
                            Text(text = "Teto (18% ICMS): R$ ${precoState.pmc_18_icms ?: "N/A"}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF059669))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Teto (0% ICMS): R$ ${precoState.pmc_zero_icms ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            Text(text = "Preço Teto PMC não encontrado na base local para $selectedUf.", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
            
            // Bula e Consulta
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    val isOnline = networkStatus == NetworkStatus.Available
                    Button(
                        onClick = {
                            medicine.link_bula_paciente?.let {
                                downloadPdf(it, medicine.nome_comercial)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isOnline && !medicine.link_bula_paciente.isNullOrBlank()
                    ) {
                        Text(if (isOnline) "Baixar Bula Oficial (PDF)" else "Sem conexão com internet", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val queryParam = medicine.ean.takeIf { !it.isNullOrBlank() } ?: medicine.nome_comercial
                    OutlinedButton(
                        onClick = {
                            onOpenUrl("https://consultaremedios.com.br/busca?termo=$queryParam")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isOnline
                    ) {
                        Text("Ver no Consulta Remédios", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            onOpenUrl("https://www.drogaraia.com.br/search?w=$queryParam")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isOnline
                    ) {
                        Text("Ver na Droga Raia", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Criar Lembrete
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Lembrete de Medicamento", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Deseja criar um alarme para este remédio?", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val interval = 8
                                medicine.ean?.let {
                                    repository.addAlarme(it, interval, 0, medicine.nome_comercial, interval.toString())
                                    scheduleMedicineReminder(medicine.nome_comercial, interval)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lembrar a cada 8 horas", color = Color.White)
                        }
                    }
                }
            }

            // Genéricos e Similares (Equivalentes)
            if (interchangeability.equivalents.isNotEmpty()) {
                item {
                    Text(
                        "Genéricos e Similares",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(interchangeability.equivalents) { eqMed ->
                    val isCheapest = interchangeability.equivalents.firstOrNull()?.medicine?.ean == eqMed.medicine.ean && eqMed.pmc18 != null
                    EquivalentMedicineCard(
                        equivalent = eqMed.medicine,
                        preco = eqMed.pmc18,
                        isCheapest = isCheapest
                    )
                }
            }
        }
    }
}

@Composable
fun EquivalentMedicineCard(equivalent: Medicamentos, preco: Double?, isCheapest: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = equivalent.nome_comercial, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = equivalent.cnpj_fabricante ?: "Fabricante Desconhecido", fontSize = 12.sp, color = Color.Gray)
                }
                
                if (preco != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "R$ ${preco.toString().replace(".", ",")}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colors.primary
                        )
                        if (isCheapest) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "🏆 Mais Barato",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
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
