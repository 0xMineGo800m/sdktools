package feature_graph.presentation.graph_screen

/**
 * Created by Alon Minski on 27/02/2022.
 */
sealed class ChartEvent {
    data class AxisVisibility(val axisName: String, val isVisible: Boolean) : ChartEvent()
    data class OnSocketGotData(val socketDataLine: String) : ChartEvent()
    object OnSocketNoData : ChartEvent()
    object OnFileLoaded : ChartEvent()
    object PlayRecordingFile : ChartEvent()
    object StopRecordingFile : ChartEvent()
    object ResetChartYRange : ChartEvent()
    object ResetChart : ChartEvent()
}
