import androidx.compose.runtime.Composable
import com.duartefilipe.helphealth.data.DatabaseDriverFactory

actual fun getPlatformName(): String = "Android"

@Composable fun MainView(databaseDriverFactory: DatabaseDriverFactory) = App(databaseDriverFactory = databaseDriverFactory)
