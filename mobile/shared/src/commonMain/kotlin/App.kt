import androidx.compose.runtime.*
import com.duartefilipe.helphealth.data.DatabaseDriverFactory
import com.duartefilipe.helphealth.data.DatabaseSyncManager
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.detail.MedicineDetailScreen
import com.duartefilipe.helphealth.ui.search.SearchScreen
import com.duartefilipe.helphealth.ui.theme.HelpHealthTheme
import com.duartefilipe.helphealth.util.ConnectivityObserver
import com.duartefilipe.helphealth.util.NetworkStatus

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
    onScanBarcodeClick: () -> Unit = {},
    scannedBarcodeQuery: String? = null,
    onOpenUrl: (String) -> Unit = {}
) {
    val repository = remember { MedicineRepository(databaseDriverFactory) }
    val syncManager = remember { DatabaseSyncManager() }
    var selectedMedicine by remember { mutableStateOf<Medicamentos?>(null) }
    val networkStatus = remember {
        object : ConnectivityObserver {
            override fun currentStatus() = NetworkStatus.Available
        }.currentStatus()
    }

    HelpHealthTheme {
        if (selectedMedicine == null) {
            SearchScreen(
                repository = repository,
                onMedicineSelect = { selectedMedicine = it },
                onScanBarcodeClick = onScanBarcodeClick,
                onSyncClick = {
                    // Dispara a sincronização
                },
                searchQueryOverride = scannedBarcodeQuery
            )
        } else {
            MedicineDetailScreen(
                medicine = selectedMedicine!!,
                repository = repository,
                networkStatus = networkStatus,
                onBackClick = { selectedMedicine = null },
                onOpenBulaUrl = onOpenUrl
            )
        }
    }
}

expect fun getPlatformName(): String