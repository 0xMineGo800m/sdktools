package feature_graph.presentation.graph_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import feature_graph.domain.AxisVisibility
import feature_graph.domain.ChartLogic
import org.jfree.chart.ChartPanel
import org.jfree.data.time.TimeSeries
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Created by Alon Minski on 25/02/2022.
 */

@Composable
fun ChartScreen(chartLogic: ChartLogic) {
    Column {
        Row(modifier = Modifier.padding(5.dp), Arrangement.spacedBy(5.dp)) {
            Button(
                onClick = { chartLogic.onEvent(ChartEvent.PlayRecordingFile) },
                enabled = chartLogic.isFileLoaded && !(chartLogic.isSocketReadingActive && chartLogic.isFileReadingActive)
            ) {
                Text("Play recording")
            }

            Button(
                onClick = { chartLogic.onEvent(ChartEvent.StopRecordingFile) },
                enabled = chartLogic.isFileReadingActive
            ) {
                Text("Stop playing")
            }

            Button(
                onClick = { chartLogic.onEvent(ChartEvent.ResetChartYRange) },
                enabled = chartLogic.isSocketReadingActive || chartLogic.isFileReadingActive
            ) {
                Text("Reset chart y range")
            }

            Button(
                onClick = { chartLogic.onEvent(ChartEvent.ResetChart) },
                enabled = !chartLogic.isSocketReadingActive && !chartLogic.isFileReadingActive
            ) {
                Text("Reset graph")
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.padding(5.dp), Arrangement.spacedBy(5.dp)) {

            AxisSelector(
                axes = chartLogic.axes,
                axisVisibility = { name, visibility ->
                    chartLogic.onEvent(ChartEvent.AxisVisibility(name, visibility))
                }

//                axisXVisibility = chartLogic.axisX1Visibility,
//                axisYVisibility = chartLogic.axisY1Visibility,
//                axisZ1Visibility = chartLogic.axisZ1Visibility,
//                axisX1Action = { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisX1", isChecked)) },
//                axisY1Action = { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisY1", isChecked)) },
//                axisZ1Action = { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisZ1", isChecked)) }
            )

            TheChart(chartLogic.chartPanel)
        }
    }
}

@Composable
private fun AxisSelector(axes: SnapshotStateMap<TimeSeries, Boolean>, axisVisibility: AxisVisibility) {
    val keys = axes.keys.toList()
    val visibilities = axes.values.toList()
    Card(elevation = 5.dp, shape = RoundedCornerShape(0.dp)) {
        LazyColumn(modifier = Modifier.padding(8.dp).wrapContentWidth().fillMaxHeight()) {
            items(axes.size) { oneItem ->

                LabelledCheckbox(keys[oneItem].key.toString(), visibilities[oneItem]) { isChecked ->
                    axisVisibility.invoke(keys[oneItem].key.toString(), isChecked)
                }
            }
        }
    }
}

@Composable
fun LabelledCheckbox(label: String, isChecked: Boolean = false, action: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                action(it)
            },
            enabled = true,
            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
        )
        Text(text = label, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun TheChart(chart: ChartPanel) {
    Card(elevation = 5.dp, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxSize()) {
        Box {
            SwingPanel(
                factory = {
                    JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        add(chart)
                    }
                }
            )
        }
    }
}