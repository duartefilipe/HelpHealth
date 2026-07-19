import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.duartefilipe.helphealth.data.DatabaseDriverFactory

actual fun getPlatformName(): String = "Desktop"

@Composable fun MainView() = App(databaseDriverFactory = DatabaseDriverFactory())

@Preview
@Composable
fun AppPreview() {
    App(databaseDriverFactory = DatabaseDriverFactory())
}