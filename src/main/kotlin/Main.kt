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
    // FEEDBACK:
    // It seems like there is no delegate implementation for Compose Desktop
    // as we know it from Android

    // You don't really need to store these objects as state.
    // State is used to let the UI change as soon as the state changes.
    // However, the repo will be initialized once and then never change.
    // So, it's enough to just put it in a remember block.
    val dataRepository = remember { mutableStateOf(DataRepository()) } // <-- Why can't I use a delegate 'by' here ? :(

    // FEEDBACK:
    // If you refer to other state in a remember block (dataRepository.value),
    // you should also use that state as key for the remember function like this

    // val chartLogic = remember(dataRepository.value) { dataRepository.value }

    // This will make sure that whenever dataRepository changes, the remember block is updated
    // and called again with the new value of the repo. Otherwise, the chart logic would always
    // only get the very first initialization of the repo and then never be called again.
    // However, as I said in the comment above, the repo doesn't need to be a compose state, so
    // you can ignore this.
    val chartLogic = remember { ChartLogic(dataRepository.value) }

    // FEEDBACK:
    // Same here, doesn't need to be a state
    val menuBarActions = remember { mutableStateOf(MenuBarActions(dataRepository.value, chartLogic)) }

    // FEEDBACK: Looks good to me, it properly exits the program :)
    if (!menuBarActions.value.shouldCloseApp) { // <-- is this the right way to close a window?
        Window(
            onCloseRequest = ::exitApplication,
            title = "Core Plotter v1.0.0",
            state = rememberWindowState(width = 1024.dp, height = 768.dp),
        ) {


            // Call the MenuBar composable
            // Call the main and only screen: ChartScreen
            MaterialTheme {
                WindowMenuBar(menuBarActions.value)
                ChartScreen(chartLogic)
            }
        }
    }
}