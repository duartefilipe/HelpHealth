package com.duartefilipe.helphealth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.search.SearchScreen
import com.duartefilipe.helphealth.ui.favorites.FavoritesScreen
import com.duartefilipe.helphealth.ui.alarms.AlarmsScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun MainScreen(
    repository: MedicineRepository,
    onMedicineSelect: (Medicamentos) -> Unit,
    networkStatus: com.duartefilipe.helphealth.util.NetworkStatus,
    onScanBarcodeClick: () -> Unit,
    appScope: CoroutineScope,
    searchQueryOverride: String? = null,
    isLightMode: Boolean,
    onToggleTheme: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary,
                elevation = 8.dp
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Busca") },
                    label = { Text("Busca") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alarmes") },
                    label = { Text("Alarmes") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = Color.Gray
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> SearchScreen(
                    repository = repository,
                    onMedicineSelect = onMedicineSelect,
                    networkStatus = networkStatus,
                    onScanBarcodeClick = onScanBarcodeClick,
                    appScope = appScope,
                    searchQueryOverride = searchQueryOverride,
                    isLightMode = isLightMode,
                    onToggleTheme = onToggleTheme
                )
                1 -> FavoritesScreen(
                    repository = repository,
                    onMedicineSelect = onMedicineSelect
                )
                2 -> AlarmsScreen(
                    repository = repository,
                    onMedicineSelect = onMedicineSelect
                )
            }
        }
    }
}
