package feature_graph.presentation.graph_screen

/**
 * Created by Alon Minski on 27/02/2022.
 */
data class ChartState(
    val axisX1Visible: Boolean = true,
    val axisY1Visible: Boolean = true,
    val axisZ1Visible: Boolean = true,
    val isFileLoaded: Boolean = false
)
