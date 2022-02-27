package feature_graph.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import feature_graph.data.TimeAxisEntity
import feature_graph.presentation.graph_screen.ChartEvent
import feature_graph.presentation.graph_screen.ChartState
import kotlinx.coroutines.*
import org.jfree.data.time.Millisecond
import org.jfree.data.time.TimeSeries
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by Alon Minski on 18/02/2022.
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object ChartLogic {

    private val gson = Gson()
    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    private var currentPlayingFile: File? = null
    var isUpdateActive by mutableStateOf(false)
    val axisX1Series by mutableStateOf(TimeSeries("Accel X1"))
    val axisY1Series by mutableStateOf(TimeSeries("Accel Y1"))
    val axisZ1Series by mutableStateOf(TimeSeries("Accel Z1"))

    var graphState by mutableStateOf(ChartState())

    fun onEvent(event: ChartEvent) {
        when (event) {
            is ChartEvent.AxisVisibility -> {
                handleAxisVisibility(event)
            }

            is ChartEvent.OnFileLoaded -> {
                handleOnFileLoaded()
            }

            is ChartEvent.PlayRecordingFile -> {
                playRecordingFile()
            }

            is ChartEvent.StopRecordingFile -> {
                stopRecordingFile()
            }
        }
    }

    private fun handleOnFileLoaded() {
        graphState = graphState.copy(isFileLoaded = true)
    }

    private fun handleAxisVisibility(event: ChartEvent.AxisVisibility) {
        when (event.axisName) {
            "AxisX1" -> {
                graphState = graphState.copy(axisX1Visible = event.isVisible)
            }
            "AxisY1" -> {
                graphState = graphState.copy(axisY1Visible = event.isVisible)
            }
            "AxisZ1" -> {
                graphState = graphState.copy(axisZ1Visible = event.isVisible)
            }
        }
    }

    private fun playRecordingFile() {

        if (isUpdateActive) return
        isUpdateActive = true

        currentPlayingFile = DataRepository.recordingFile
        if (currentPlayingFile == null) return

        val fis = FileInputStream(currentPlayingFile)
        val scanner = Scanner(fis)

        coroutineScope.launch {

            while (scanner.hasNextLine() && isUpdateActive) {
                val line = scanner.nextLine()
                val data = gson.fromJson(line, TimeAxisEntity::class.java)
                println(data)
                axisX1Series.add(Millisecond(), data.accelX)
                axisY1Series.add(Millisecond(), data.accelY)
                axisZ1Series.add(Millisecond(), data.accelZ)
                delay(40)
            }

            isUpdateActive = false
        }
    }

    private fun stopRecordingFile() {
        isUpdateActive = false
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        currentPlayingFile = null
        graphState = graphState.copy(isFileLoaded = false)
    }
}