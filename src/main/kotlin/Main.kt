import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import feature_graph.domain.ChartLogic
import feature_graph.domain.DataRepository
import feature_graph.presentation.graph_screen.ChartScreen
import menubar_feature.domain.MenuBarActions
import menubar_feature.presentation.WindowMenuBar


fun main() = application {
    val dataRepository = remember { DataRepository() }
    val chartLogic = remember { ChartLogic(dataRepository) }
    val menuBarActions = remember { MenuBarActions(dataRepository, chartLogic) }

    if (!menuBarActions.shouldCloseApp) { // <-- is this the right way to close a window?
        Window(
            onCloseRequest = ::exitApplication,
            title = "Core Plotter v1.0.0",
            state = rememberWindowState(width = 1024.dp, height = 768.dp),
        ) {


            // Call the MenuBar composable
            // Call the main and only screen: ChartScreen
            MaterialTheme {
                WindowMenuBar(menuBarActions)
                ChartScreen(chartLogic)
            }
        }
    }
}