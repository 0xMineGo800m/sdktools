package feature_graph.presentation.graph_screen

/**
 * Created by Alon Minski on 27/02/2022.
 */
sealed class ChartEvent {
    data class AxisVisibility(val axisName: String, val isVisible: Boolean) : ChartEvent()
    object OnFileLoaded : ChartEvent()
    object PlayRecordingFile : ChartEvent()
    object StopRecordingFile : ChartEvent()
}
