package com.duartefilipe.helphealth.ui.alarms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
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
import com.duartefilipe.helphealth.util.*
import kotlinx.coroutines.launch

@Composable
fun AlarmsScreen(
    repository: MedicineRepository,
    onMedicineSelect: (Medicamentos) -> Unit
) {
    var alarms by remember { mutableStateOf<List<Alarmes>>(emptyList()) }
    var alarmToEdit by remember { mutableStateOf<Alarmes?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

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
            FloatingActionButton(onClick = {
                alarmToEdit = null
                showEditDialog = true
            }, backgroundColor = MaterialTheme.colors.secondary) {
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
                            cancelMedicineReminder(alarm.id)
                            refreshAlarms()
                        },
                        onEdit = {
                            alarmToEdit = alarm
                            showEditDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlarmEditDialog(
            repository = repository,
            initialAlarm = alarmToEdit,
            onDismiss = { showEditDialog = false },
            onSaved = {
                refreshAlarms()
                showEditDialog = false
            }
        )
    }
}

@Composable
fun AlarmCard(alarm: Alarmes, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alarm.nome_medicamento, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (alarm.tipo == "INTERVALO") {
                    Text("Início: ${alarm.horarios} | A cada ${alarm.intervalo}h", color = Color.Gray)
                } else {
                    Text("Horários: ${alarm.horarios}", color = Color.Gray)
                }
                Text("Dose: ${alarm.dose}", color = Color.Gray, fontSize = 14.sp)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AlarmEditDialog(
    repository: MedicineRepository,
    initialAlarm: Alarmes?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Medicamentos>>(emptyList()) }
    var selectedMedicine by remember { mutableStateOf<Medicamentos?>(null) }
    var isEditing = initialAlarm != null

    var tipo by remember { mutableStateOf(initialAlarm?.tipo ?: "INTERVALO") }
    var horarios by remember { mutableStateOf(initialAlarm?.horarios ?: "08:00") }
    var intervalo by remember { mutableStateOf(initialAlarm?.intervalo?.toString() ?: "8") }
    var dose by remember { mutableStateOf(initialAlarm?.dose ?: "1 unidade") }
    
    var toqueUri by remember { mutableStateOf(initialAlarm?.toque_uri ?: "") }
    var toqueNome by remember { mutableStateOf(initialAlarm?.toque_nome ?: "Padrão do Sistema") }
    var isPlayingRingtone by remember { mutableStateOf(false) }

    val ringtones = remember { getAvailableRingtones() }
    var ringtoneDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialAlarm != null) {
            val dbMed = repository.searchMedicamentos(initialAlarm.nome_medicamento, 0, 1).firstOrNull()
            selectedMedicine = dbMed
        }
    }

    LaunchedEffect(searchQuery) {
        if (!isEditing && searchQuery.length > 2) {
            searchResults = repository.searchMedicamentos(searchQuery, 0, 10)
        } else {
            searchResults = emptyList()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            stopRingtonePreview()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Alarme" else "Novo Alarme", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                item {
                    if (selectedMedicine == null && !isEditing) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar medicamento...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.heightIn(max = 200.dp)) {
                            searchResults.forEach { med ->
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
                        Text("Medicamento: ${selectedMedicine?.nome_comercial ?: initialAlarm?.nome_medicamento}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = tipo == "INTERVALO", onClick = { tipo = "INTERVALO" })
                            Text("Intervalo")
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = tipo == "FIXO", onClick = { tipo = "FIXO" })
                            Text("Horários Fixos")
                        }
                        
                        if (tipo == "INTERVALO") {
                            OutlinedTextField(
                                value = horarios,
                                onValueChange = { horarios = it },
                                label = { Text("Horário de Início (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = intervalo,
                                onValueChange = { intervalo = it.filter { char -> char.isDigit() } },
                                label = { Text("Intervalo (em horas)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = horarios,
                                onValueChange = { horarios = it },
                                label = { Text("Horários (ex: 08:00,12:00)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        OutlinedTextField(
                            value = dose,
                            onValueChange = { dose = it },
                            label = { Text("Dose (ex: 1 Comprimido)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Toque do Despertador", fontSize = 14.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { ringtoneDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(toqueNome)
                                }
                                DropdownMenu(expanded = ringtoneDropdownExpanded, onDismissRequest = { ringtoneDropdownExpanded = false }) {
                                    ringtones.forEach { ringtone ->
                                        DropdownMenuItem(onClick = {
                                            toqueUri = ringtone.first
                                            toqueNome = ringtone.second
                                            ringtoneDropdownExpanded = false
                                            isPlayingRingtone = false
                                            stopRingtonePreview()
                                        }) {
                                            Text(ringtone.second)
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = {
                                if (isPlayingRingtone) {
                                    stopRingtonePreview()
                                    isPlayingRingtone = false
                                } else {
                                    playRingtonePreview(toqueUri)
                                    isPlayingRingtone = true
                                }
                            }) {
                                Icon(
                                    if (isPlayingRingtone) Icons.Default.Add else Icons.Default.PlayArrow, // Fallback icon instead of Stop
                                    contentDescription = "Ouvir Toque"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedMedicine != null || isEditing) {
                Button(onClick = {
                    val nome = selectedMedicine?.nome_comercial ?: initialAlarm?.nome_medicamento ?: ""
                    val ean = selectedMedicine?.ean ?: initialAlarm?.ean ?: ""
                    
                    if (isEditing && initialAlarm != null) {
                        repository.updateAlarme(
                            id = initialAlarm.id,
                            nome = nome,
                            tipo = tipo,
                            horarios = horarios,
                            intervalo = intervalo.toIntOrNull(),
                            dose = dose,
                            toqueUri = toqueUri,
                            toqueNome = toqueNome
                        )
                        // Cancel old alarm
                        cancelMedicineReminder(initialAlarm.id)
                        
                        // Schedule new alarm
                        scheduleMedicineReminder(
                            alarmId = initialAlarm.id,
                            medicineName = nome,
                            dose = dose,
                            timeMillis = 0L, // Handled in Android side
                            intervalHours = if (tipo == "INTERVALO") intervalo.toIntOrNull() else -1,
                            ringtoneUri = toqueUri
                        )
                    } else {
                        repository.addAlarme(
                            ean = ean,
                            nome = nome,
                            tipo = tipo,
                            horarios = horarios,
                            intervalo = intervalo.toIntOrNull(),
                            dose = dose,
                            toqueUri = toqueUri,
                            toqueNome = toqueNome
                        )
                        
                        // In reality, we'd fetch the newly inserted ID, but for demo we can query it or rely on a simple hash
                        val alarmsList = repository.getAllAlarmes()
                        val newlyAdded = alarmsList.lastOrNull()
                        if (newlyAdded != null) {
                            scheduleMedicineReminder(
                                alarmId = newlyAdded.id,
                                medicineName = nome,
                                dose = dose,
                                timeMillis = 0L, // Handled in Android side
                                intervalHours = if (tipo == "INTERVALO") intervalo.toIntOrNull() else -1,
                                ringtoneUri = toqueUri
                            )
                        }
                    }
                    onSaved()
                }) {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
