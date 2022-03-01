package menubar_feature.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import feature_graph.domain.ChartLogic
import feature_graph.domain.DataRepository
import feature_graph.presentation.graph_screen.ChartEvent
import kotlinx.coroutines.CompletableDeferred
import java.nio.file.Path

/**
 * Created by Alon Minski on 24/02/2022.
 */
class MenuBarActions(private val dataRepository: DataRepository, private val chartLogic: ChartLogic) {
    val openDialog = DialogState<Path?>()
    var shouldCloseApp by mutableStateOf(false)

    suspend fun openFile() {
        val pathToFile = openDialog.awaitResult()
        if (pathToFile != null) {
            dataRepository.recordingFile = pathToFile.toFile()
            chartLogic.onEvent(ChartEvent.OnFileLoaded)
        }
    }

    fun exitApplication() {
        shouldCloseApp = true
    }

    fun connectToPort() {

    }
}

class DialogState<T> {
    private var onResult: CompletableDeferred<T>? by mutableStateOf(null)

    val isAwaiting get() = onResult != null

    suspend fun awaitResult(): T {
        onResult = CompletableDeferred()
        val result = onResult!!.await()
        onResult = null
        return result
    }

    fun onResult(result: T) = onResult!!.complete(result)
}