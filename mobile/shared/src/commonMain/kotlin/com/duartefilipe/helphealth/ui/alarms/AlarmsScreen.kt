package com.duartefilipe.helphealth.ui.alarms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duartefilipe.helphealth.db.Alarmes
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.util.scheduleMedicineReminder

@Composable
fun AlarmsScreen(
    repository: MedicineRepository,
    onMedicineSelect: (Medicamentos) -> Unit
) {
    var alarms by remember { mutableStateOf<List<Alarmes>>(emptyList()) }
    var showSearchDialog by remember { mutableStateOf(false) }

    fun refreshAlarms() {
        alarms = repository.getAllAlarmes()
    }

    LaunchedEffect(Unit) {
        refreshAlarms()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Alarmes", fontWeight = FontWeight.Bold) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSearchDialog = true }, backgroundColor = MaterialTheme.colors.secondary) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Alarme")
            }
        }
    ) { innerPadding ->
        if (alarms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Nenhum alarme configurado.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onDelete = {
                            repository.deleteAlarme(alarm.id)
                            refreshAlarms()
                        }
                    )
                }
            }
        }
    }

    if (showSearchDialog) {
        AlarmSearchDialog(
            repository = repository,
            onDismiss = { showSearchDialog = false },
            onAlarmCreated = {
                refreshAlarms()
                showSearchDialog = false
            }
        )
    }
}

@Composable
fun AlarmCard(alarm: Alarmes, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(alarm.nome_medicamento, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Horário: ${alarm.hora.toString().padStart(2, '0')}:${alarm.minuto.toString().padStart(2, '0')}", color = Color.Gray)
                Text("Repetição: ${alarm.dias_semana} em ${alarm.dias_semana} horas", color = Color.Gray, fontSize = 12.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AlarmSearchDialog(
    repository: MedicineRepository,
    onDismiss: () -> Unit,
    onAlarmCreated: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Medicamentos>>(emptyList()) }
    var selectedMedicine by remember { mutableStateOf<Medicamentos?>(null) }
    
    var intervalHours by remember { mutableStateOf("8") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            searchResults = repository.searchMedicamentos(searchQuery, 0, 10)
        } else {
            searchResults = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Alarme", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (selectedMedicine == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar medicamento...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(searchResults) { med ->
                            Text(
                                text = med.nome_comercial,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMedicine = med }
                                    .padding(8.dp)
                            )
                            Divider()
                        }
                    }
                } else {
                    Text("Medicamento selecionado: ${selectedMedicine!!.nome_comercial}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = intervalHours,
                        onValueChange = { intervalHours = it.filter { char -> char.isDigit() } },
                        label = { Text("Intervalo (em horas)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (selectedMedicine != null) {
                Button(onClick = {
                    val interval = intervalHours.toIntOrNull() ?: 8
                    val hora = interval // Simplification for demo
                    // Insert into DB
                    repository.addAlarme(
                        ean = selectedMedicine!!.ean ?: "",
                        hora = hora,
                        minuto = 0,
                        nome = selectedMedicine!!.nome_comercial,
                        dias = interval.toString()
                    )
                    // Schedule OS notification
                    scheduleMedicineReminder(selectedMedicine!!.nome_comercial, interval)
                    onAlarmCreated()
                }) {
                    Text("Salvar Alarme")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (selectedMedicine != null) selectedMedicine = null else onDismiss()
            }) {
                Text(if (selectedMedicine != null) "Voltar" else "Cancelar")
            }
        }
    )
}
