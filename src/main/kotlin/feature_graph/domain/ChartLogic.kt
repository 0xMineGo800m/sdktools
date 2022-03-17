package feature_graph.domain

import androidx.compose.runtime.*
import com.google.gson.Gson
import feature_graph.data.DataLine
import feature_graph.data.TimeAxisEntity
import feature_graph.data.TimeAxisMeta
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

typealias AxisVisibility = (axisName: String, isChecked: Boolean) -> Unit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChartLogic(private val dataRepository: DataRepository) {

    private val gson = Gson()
    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    private var jFreeChart: JFreeChart? = null
    private var currentPlayingFile: File? = null
    val axes = mutableStateListOf<TimeSeries>()
    val axesVisibility = mutableStateListOf<Boolean>()
    var isFileLoaded by mutableStateOf(false)
    var isFileReadingActive by mutableStateOf(false)
    var isSocketReadingActive by mutableStateOf(false)
    var chartPanel by mutableStateOf(createChart())

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

            is ChartEvent.OnSocketNoData -> {
                handleSocketNoData()
            }

            is ChartEvent.ResetChartYRange -> {
                handleResetChartYRange()
            }

            is ChartEvent.ResetChart -> {
                handleResetChart()
            }
        }
    }

    private fun handleSocketNoData() {
        isSocketReadingActive = false
    }

    private fun handleResetChart() {
        val timeSeriesCollection = jFreeChart?.xyPlot?.dataset as TimeSeriesCollection
        timeSeriesCollection.removeAllSeries()

        axes.forEach { t ->
            t.clear()
            timeSeriesCollection.addSeries(t)
        }
    }

    private fun handleResetChartYRange() {
        jFreeChart?.xyPlot?.rangeAxis?.range = Range(RANGE_MIN, RANGE_MAX)
    }

    private fun handleSocketData(socketDataLine: String) {
        //TODO: fix this
        isFileReadingActive = false
        isSocketReadingActive = true

        // FEEDBACK:
        // Parsing GSON is data related logic and job of the repository.
        // This class ideally also shouldn't know where the data is coming from
        // (like from a DB, from sockets, etc.), but this is more advanced stuff
        // and nothing to worry about if you're just getting started with all the
        // architectural stuff.
        val data = gson.fromJson(socketDataLine, TimeAxisEntity::class.java)
//        axisX1Series.add(Millisecond(), data.accelX)
//        axisY1Series.add(Millisecond(), data.accelY)
//        axisZ1Series.add(Millisecond(), data.accelZ)
    }

    private fun handleOnFileLoaded() {
        handleResetChart()

        if (initChartLogicAndAxes()) return

        isSocketReadingActive = false
        isFileLoaded = true
    }


    private fun initChartLogicAndAxes(): Boolean {
        currentPlayingFile = dataRepository.recordingFile
        if (currentPlayingFile == null) return true

        val fis = FileInputStream(currentPlayingFile)
        val scanner = Scanner(fis)
        val firstLine = scanner.nextLine()
        val timeAxesMetaData = gson.fromJson(firstLine, TimeAxisMeta::class.java)
        initAxisMap(timeAxesMetaData)
        fis.close()
        return false
    }

    private fun handleAxisVisibility(event: ChartEvent.AxisVisibility) {
        run loop@{
            axes.forEachIndexed { index, timeSeries ->
                if (timeSeries.key.toString() == event.axisName) {
                    jFreeChart?.xyPlot?.renderer?.setSeriesVisible(index, event.isVisible)
                    axesVisibility[index] = event.isVisible
                    return@loop
                }
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
        scanner.nextLine()

        // FEEDBACK:
        // 4. File reading logic belongs in a separate data related class such as the repository
        coroutineScope.launch {
            while (scanner.hasNextLine() && isFileReadingActive) {
                val line = scanner.nextLine()
                val data = gson.fromJson(line, DataLine::class.java)
                val axisValues = data.value.axes
                val time = data.value.timestamp
                println(data)

                val iterator = axes.iterator()
                axisValues.forEachIndexed { index, doubleVal ->
//                    val timeAXis = iterator.next().key
                    axes[index].add(Millisecond(), doubleVal)
                }

                delay(40)
            }

            isFileReadingActive = false
            fis.close()
        }
    }

    private fun initAxisMap(timeAxesMetaData: TimeAxisMeta?) {
        axes.clear()
        axesVisibility.clear()
        timeAxesMetaData?.names?.forEach { name ->
            axes.add(TimeSeries(name))
            axesVisibility.add(true)
        }

        chartPanel = createChart()
    }

    private fun stopRecordingFile() {
        isFileReadingActive = false
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        currentPlayingFile = null
        isFileLoaded = false
    }

    private fun createChart(): ChartPanel {
        val dataset = TimeSeriesCollection()
        axes.forEach { t ->
            t.maximumItemCount = 200
            dataset.addSeries(t)
        }

        val renderer = XYSplineRenderer()
        for (index in 0..axes.size) {
            renderer.setSeriesPaint(index, ui.theme.colorMap[index])
            renderer.setSeriesShapesVisible(index, false)
        }

        val xAxis = DateAxis("Time")
        val yAxis = NumberAxis("Value")
        val plot = XYPlot(dataset, xAxis, yAxis, renderer)
        jFreeChart = JFreeChart("Plotter", JFreeChart.DEFAULT_TITLE_FONT, plot, true).apply {
            xyPlot.isDomainPannable = true
            xyPlot.isRangePannable = true
            xyPlot.rangeAxis.range = Range(RANGE_MIN, RANGE_MAX)
        }

        val frame = ChartPanel(jFreeChart)
        frame.isVisible = true

        return frame
    }

    companion object {
        private const val RANGE_MIN = -15.0
        private const val RANGE_MAX = 15.0
    }
}
