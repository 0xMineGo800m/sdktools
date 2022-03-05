package feature_graph.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import feature_graph.data.TimeAxisEntity
import feature_graph.presentation.graph_screen.ChartEvent
import kotlinx.coroutines.*
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYSplineRenderer
import org.jfree.data.Range
import org.jfree.data.time.Millisecond
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by Alon Minski on 18/02/2022.
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChartLogic(private val dataRepository: DataRepository) {

    private val gson = Gson()
    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    private val axisX1Series = TimeSeries("Accel X1")
    private val axisY1Series = TimeSeries("Accel Y1")
    private val axisZ1Series = TimeSeries("Accel Z1")
    private var jFreeChart: JFreeChart? = null
    private var currentPlayingFile: File? = null
    var axisX1Visibility by mutableStateOf(true)
    var axisY1Visibility by mutableStateOf(true)
    var axisZ1Visibility by mutableStateOf(true)
    var isFileLoaded by mutableStateOf(false)
    var isFileReadingActive by mutableStateOf(false)
    var isSocketReadingActive by mutableStateOf(false)
    val chart by mutableStateOf(createChart())

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

            is ChartEvent.OnSocketGotData -> {
                handleSocketData(event.socketDataLine)
            }

            is ChartEvent.ResetChartYRange -> {
                handleResetChartYRange()
            }
        }
    }

    private fun handleResetChartYRange() {
        jFreeChart?.xyPlot?.rangeAxis?.range = Range(-10.0, 10.0)
    }

    private fun handleSocketData(socketDataLine: String) {
        isFileReadingActive = false
        isSocketReadingActive = true

        // FEEDBACK:
        // Parsing GSON is data related logic and job of the repository.
        // This class ideally also shouldn't know where the data is coming from
        // (like from a DB, from sockets, etc.), but this is more advanced stuff
        // and nothing to worry about if you're just getting started with all the
        // architectural stuff.
        val data = gson.fromJson(socketDataLine, TimeAxisEntity::class.java)
        axisX1Series.add(Millisecond(), data.accelX)
        axisY1Series.add(Millisecond(), data.accelY)
        axisZ1Series.add(Millisecond(), data.accelZ)
    }

    private fun handleOnFileLoaded() {
        // FEEDBACK:
        // Clearing a list state doesn't trigger recomposes.
        // I'd be VERY careful in general using mutable lists since these come with a lot of
        // dangers like this one. They also very often lead to race conditions.
        // It would be better to use immutable lists as state and then just do this to clear it:

        // axisX1Series = emptyList()

        // Recomposes are only triggered if you assign a new value to a compose state.
        axisX1Series.clear()
        axisY1Series.clear()
        axisZ1Series.clear()
        isFileLoaded = true
    }

    private fun handleAxisVisibility(event: ChartEvent.AxisVisibility) {
        when (event.axisName) {
            "AxisX1" -> {
                jFreeChart?.xyPlot?.renderer?.setSeriesVisible(0, event.isVisible)
                axisX1Visibility = event.isVisible
            }
            "AxisY1" -> {
                jFreeChart?.xyPlot?.renderer?.setSeriesVisible(1, event.isVisible)
                axisY1Visibility = event.isVisible
            }
            "AxisZ1" -> {
                jFreeChart?.xyPlot?.renderer?.setSeriesVisible(2, event.isVisible)
                axisZ1Visibility = event.isVisible
            }
        }
    }

    private fun playRecordingFile() {

        if (isFileReadingActive) return
        isFileReadingActive = true

        currentPlayingFile = dataRepository.recordingFile
        if (currentPlayingFile == null) return

        val fis = FileInputStream(currentPlayingFile)
        val scanner = Scanner(fis)

        // FEEDBACK:
        // 4. File reading logic belongs in a separate data related class such as the repository
        coroutineScope.launch {

            while (scanner.hasNextLine() && isFileReadingActive) {
                val line = scanner.nextLine()
                val data = gson.fromJson(line, TimeAxisEntity::class.java)
                println(data)
                axisX1Series.add(Millisecond(), data.accelX)
                axisY1Series.add(Millisecond(), data.accelY)
                axisZ1Series.add(Millisecond(), data.accelZ)

                delay(40)
            }

            isFileReadingActive = false
        }
    }

    private fun stopRecordingFile() {
        isFileReadingActive = false
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        currentPlayingFile = null
        isFileLoaded = false
    }

    private fun createChart(): ChartPanel {
        val axisSeries = ArrayList<TimeSeries>()
        axisSeries.add(axisX1Series)
        axisSeries.add(axisY1Series)
        axisSeries.add(axisZ1Series)

        val dataset = TimeSeriesCollection()
        axisSeries.forEach { axis ->
            axis.maximumItemCount = 200
            dataset.addSeries(axis)
        }

        val renderer = XYSplineRenderer()

        for (index in 0..axisSeries.size){
            renderer.setSeriesPaint(index, ui.theme.colorMap[index])
            renderer.setSeriesShapesVisible(index, false)
        }

        val xAxis = DateAxis("Time")
        val yAxis = NumberAxis("Value")
        val plot = XYPlot(dataset, xAxis, yAxis, renderer)
        jFreeChart = JFreeChart("Plotter", JFreeChart.DEFAULT_TITLE_FONT, plot, true).apply {
            xyPlot.isDomainPannable = true
            xyPlot.isRangePannable = true
            xyPlot.rangeAxis.range = Range(-10.0, 10.0)
        }

        val frame = ChartPanel(jFreeChart)
        frame.isVisible = true

        return frame
    }
}