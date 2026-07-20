package com.duartefilipe.helphealth.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duartefilipe.helphealth.data.DatabaseSyncManager
import com.duartefilipe.helphealth.db.HelpHealthDatabase
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Simple JSON string field extractor — avoids org.json dependency in commonMain
private fun extractStringField(json: String, field: String): String? {
    val key = "\"$field\""
    val idx = json.indexOf(key)
    if (idx < 0) return null
    val afterKey = json.indexOf(':', idx + key.length)
    if (afterKey < 0) return null
    val rest = json.substring(afterKey + 1).trimStart()
    if (rest.startsWith("null")) return null
    if (rest.startsWith("\"")) {
        val end = rest.indexOf('"', 1)
        if (end < 0) return null
        return rest.substring(1, end)
    }
    // boolean or number
    val endIdx = rest.indexOfFirst { it == ',' || it == '}' || it == ']' }
    return if (endIdx > 0) rest.substring(0, endIdx).trim() else rest.trim()
}

private fun extractBoolField(json: String, field: String): Boolean {
    return extractStringField(json, field)?.equals("true", ignoreCase = true) == true
}

private fun splitJsonArray(jsonArrayStr: String): List<String> {
    val result = mutableListOf<String>()
    var depth = 0
    var start = -1
    for (i in jsonArrayStr.indices) {
        val c = jsonArrayStr[i]
        if (c == '{') {
            if (depth == 0) start = i
            depth++
        } else if (c == '}') {
            depth--
            if (depth == 0 && start >= 0) {
                result.add(jsonArrayStr.substring(start, i + 1))
                start = -1
            }
        }
    }
    return result
}

suspend fun syncMedicinesFromServer(
    syncManager: DatabaseSyncManager,
    database: HelpHealthDatabase,
    onProgress: (String) -> Unit
): Int = withContext(Dispatchers.IO) {
    val dbQueries = database.helpHealthDatabaseQueries
    var totalImported = 0
    var page = 0
    val pageSize = 500

    try {
        while (true) {
            withContext(Dispatchers.Main) { onProgress("Baixando página ${page + 1}...") }
            val jsonText = syncManager.fetchMedicinesJson(page, pageSize) ?: break

            // Extract "content" array
            val contentStart = jsonText.indexOf("\"content\"")
            if (contentStart < 0) break
            val arrayStart = jsonText.indexOf('[', contentStart)
            if (arrayStart < 0) break
            // Find matching ]
            var depth = 0
            var arrayEnd = -1
            for (i in arrayStart until jsonText.length) {
                if (jsonText[i] == '[') depth++
                else if (jsonText[i] == ']') {
                    depth--
                    if (depth == 0) { arrayEnd = i; break }
                }
            }
            if (arrayEnd < 0) break

            val contentStr = jsonText.substring(arrayStart, arrayEnd + 1)
            val items = splitJsonArray(contentStr)

            if (items.isEmpty()) break

            for (item in items) {
                val ean = extractStringField(item, "ean") ?: "AUTO_${System.nanoTime()}"
                val nomeComercial = extractStringField(item, "nomeComercial") ?: ""
                val principioAtivo = extractStringField(item, "principioAtivo") ?: ""
                val concentracao = extractStringField(item, "concentracao")
                val formaFarmaceutica = extractStringField(item, "formaFarmaceutica")
                val categoriaRegulatoria = extractStringField(item, "categoriaRegulatoria") ?: "SIMILAR"
                val tarja = extractStringField(item, "tarja") ?: ""
                val retencaoReceita = if (extractBoolField(item, "retencaoReceita")) 1L else 0L
                val precisaRefrigeracao = if (extractBoolField(item, "precisaRefrigeracao")) 1L else 0L
                val fazParteFarmaciaPopular = if (extractBoolField(item, "fazParteFarmaciaPopular")) 1L else 0L
                val linkBulaPaciente = extractStringField(item, "linkBulaPaciente")
                val cnpjFabricante = extractStringField(item, "cnpjFabricante")
                val razaoSocial = extractStringField(item, "razaoSocial") ?: ""
                val pmcZeroSp = extractStringField(item, "pmcZeroSp")?.toDoubleOrNull()
                val pmc18Sp = extractStringField(item, "pmc18Sp")?.toDoubleOrNull()
                val pmcZeroRs = extractStringField(item, "pmcZeroRs")?.toDoubleOrNull()
                val pmc18Rs = extractStringField(item, "pmc18Rs")?.toDoubleOrNull()

                if (nomeComercial.isNotBlank()) {
                    try {
                        if (cnpjFabricante != null) {
                            dbQueries.insertFabricante(cnpjFabricante, razaoSocial, razaoSocial)
                        }
                        dbQueries.insertMedicamento(
                            ean = ean,
                            nome_comercial = nomeComercial,
                            principio_ativo = principioAtivo.ifBlank { nomeComercial },
                            concentracao = concentracao,
                            forma_farmaceutica = formaFarmaceutica,
                            categoria_regulatoria = categoriaRegulatoria,
                            tarja = tarja,
                            retencao_receita = retencaoReceita,
                            precisa_refrigeracao = precisaRefrigeracao,
                            link_bula_paciente = linkBulaPaciente,
                            faz_parte_farmacia_popular = fazParteFarmaciaPopular,
                            cnpj_fabricante = cnpjFabricante,
                            status_registro = "ATIVO"
                        )
                        if (pmcZeroSp != null || pmc18Sp != null) {
                            dbQueries.insertPreco(ean, "SP", pmcZeroSp, pmc18Sp)
                        }
                        if (pmcZeroRs != null || pmc18Rs != null) {
                            dbQueries.insertPreco(ean, "RS", pmcZeroRs, pmc18Rs)
                        }
                        totalImported++
                    } catch (_: Exception) {
                        // Ignora duplicatas
                    }
                }
            }

            // Check "last" field
            val isLast = jsonText.contains("\"last\":true")
            if (isLast) break
            page++
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    totalImported
}

@Composable
fun SearchScreen(
    repository: MedicineRepository,
    onMedicineSelect: (Medicamentos) -> Unit,
    onScanBarcodeClick: () -> Unit,
    appScope: kotlinx.coroutines.CoroutineScope,
    searchQueryOverride: String? = null
) {
    var searchQuery by remember { mutableStateOf(searchQueryOverride ?: "") }
    var currentPage by remember { mutableStateOf(0) }
    var canLoadMore by remember { mutableStateOf(true) }
    var searchResults by remember { mutableStateOf(repository.searchMedicamentos(searchQuery, page = 0)) }
    var isSyncing by remember { mutableStateOf(false) }
    var isServerOnline by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val syncManager = remember { DatabaseSyncManager() }
    val coroutineScope = rememberCoroutineScope()

    // Auto-sync: checa servidor e sincroniza automaticamente ao abrir
    LaunchedEffect(Unit) {
        val online = syncManager.checkServerOnline()
        isServerOnline = online
        if (online) {
            val localCount = repository.countMedicamentos()
            if (localCount < 100) { // Se tiver poucos dados locais, sincroniza automaticamente
                appScope.launch {
                    isSyncing = true
                    syncStatusText = "Conectado! Sincronizando..."
                    val imported = syncMedicinesFromServer(syncManager, repository.database) { status ->
                        syncStatusText = status
                    }
                    syncStatusText = if (imported > 0) "$imported novos medicamentos ✅" else "Base atualizada"
                    isSyncing = false
                    resetAndSearch(searchQuery)
                }
            }
        }
    }

    fun resetAndSearch(query: String) {
        searchQuery = query
        currentPage = 0
        canLoadMore = true
        searchResults = repository.searchMedicamentos(query, page = 0)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            canLoadMore && lastVisibleIndex >= searchResults.size - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && searchResults.isNotEmpty()) {
            val nextPage = currentPage + 1
            val newBatch = repository.searchMedicamentos(searchQuery, page = nextPage)
            if (newBatch.isEmpty()) {
                canLoadMore = false
            } else {
                currentPage = nextPage
                searchResults = searchResults + newBatch
            }
        }
    }

    LaunchedEffect(searchQueryOverride) {
        if (!searchQueryOverride.isNullOrBlank()) {
            resetAndSearch(searchQueryOverride)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = if (isServerOnline) "HelpHealth 🟢 Online" else "HelpHealth 🔴 Offline", 
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (syncStatusText.isNotBlank()) {
                            Text(
                                text = syncStatusText,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            appScope.launch {
                                isSyncing = true
                                syncStatusText = "Verificando servidor..."
                                isServerOnline = syncManager.checkServerOnline()
                                if (isServerOnline) {
                                    syncStatusText = "Sincronizando medicamentos..."
                                    val imported = syncMedicinesFromServer(syncManager, repository.database) { status ->
                                        syncStatusText = status
                                    }
                                    syncStatusText = if (imported > 0) "$imported novos medicamentos ✅" else "Base já atualizada"
                                    resetAndSearch(searchQuery)
                                } else {
                                    syncStatusText = "Servidor indisponível ❌"
                                }
                                isSyncing = false
                            }
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
                    onValueChange = { resetAndSearch(it) },
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
                    if (isSyncing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colors.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = syncStatusText.ifBlank { "Baixando medicamentos da Anvisa..." },
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (searchQuery.isBlank()) "Nenhum medicamento cadastrado.\nClique em 🔄 para sincronizar." else "Nenhum medicamento encontrado.",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { medicine ->
                        MedicineCard(medicine = medicine, onClick = { onMedicineSelect(medicine) })
                    }

                    if (canLoadMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Carregando mais medicamentos...", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
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
