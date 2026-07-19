import androidx.compose.ui.window.ComposeUIViewController
import com.duartefilipe.helphealth.data.DatabaseDriverFactory

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController { App(databaseDriverFactory = DatabaseDriverFactory()) }