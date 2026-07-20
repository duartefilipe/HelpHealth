import androidx.compose.runtime.*
import com.duartefilipe.helphealth.data.DatabaseDriverFactory
import com.duartefilipe.helphealth.data.DatabaseSyncManager
import com.duartefilipe.helphealth.db.Medicamentos
import com.duartefilipe.helphealth.repository.MedicineRepository
import com.duartefilipe.helphealth.ui.detail.MedicineDetailScreen
import com.duartefilipe.helphealth.ui.MainScreen
import com.duartefilipe.helphealth.ui.search.SearchScreen
import com.duartefilipe.helphealth.ui.theme.HelpHealthTheme
import com.duartefilipe.helphealth.util.BackHandler
import com.duartefilipe.helphealth.util.ConnectivityObserver
import com.duartefilipe.helphealth.util.NetworkStatus
import com.russhwolf.settings.Settings

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
    onScanBarcodeClick: () -> Unit = {},
    scannedBarcodeQuery: String? = null,
    onOpenUrl: (String) -> Unit = {}
) {
    val settings = remember { Settings() }
    val repository = remember { MedicineRepository(databaseDriverFactory) }
    val syncManager = remember { DatabaseSyncManager() }
    var selectedMedicine by remember { mutableStateOf<Medicamentos?>(null) }
    
    // Carrega a preferência salva (padrão true = claro)
    var isLightMode by remember { mutableStateOf(settings.getBoolean("is_light_mode", true)) }
    val appScope = rememberCoroutineScope()
    val networkStatus = remember {
        object : ConnectivityObserver {
            override fun currentStatus() = NetworkStatus.Available
        }.currentStatus()
    }

    HelpHealthTheme(darkTheme = !isLightMode) {
        if (selectedMedicine == null) {
            MainScreen(
                repository = repository,
                onMedicineSelect = { selectedMedicine = it },
                networkStatus = networkStatus,
                onScanBarcodeClick = onScanBarcodeClick,
                appScope = appScope,
                searchQueryOverride = scannedBarcodeQuery,
                isLightMode = isLightMode,
                onToggleTheme = {
                    isLightMode = !isLightMode
                    settings.putBoolean("is_light_mode", isLightMode)
                }
            )
        } else {
            BackHandler {
                selectedMedicine = null
            }
            MedicineDetailScreen(
                medicine = selectedMedicine!!,
                repository = repository,
                networkStatus = networkStatus,
                onBackClick = { selectedMedicine = null },
                onOpenUrl = onOpenUrl
            )
        }
    }
}

expect fun getPlatformName(): String