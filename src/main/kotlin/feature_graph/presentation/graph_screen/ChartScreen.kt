package feature_graph.presentation.graph_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import feature_graph.domain.ChartLogic
import org.jfree.chart.ChartPanel
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Created by Alon Minski on 25/02/2022.
 */

typealias AxisVisibility = (isChecked: Boolean) -> Unit

@Composable
fun ChartScreen(chartLogic: ChartLogic) {
    Column {
        Row(modifier = Modifier.padding(5.dp), Arrangement.spacedBy(5.dp)) {
            Button(
                onClick = { chartLogic.onEvent(ChartEvent.PlayRecordingFile) },
                enabled = chartLogic.isFileLoaded
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
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.padding(5.dp), Arrangement.spacedBy(5.dp)) {

            AxisSelector(
                axisXVisibility = chartLogic.axisX1Visibility,
                axisYVisibility = chartLogic.axisY1Visibility,
                axisZ1Visibility = chartLogic.axisZ1Visibility,
                axisX1Action = { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisX1", isChecked)) },
                axisY1Action = { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisY1", isChecked)) },
                axisZ1Action = { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisZ1", isChecked)) }
            )

            TheChart(chartLogic.chart)
        }
    }
}

@Composable
private fun AxisSelector(
    axisXVisibility: Boolean,
    axisYVisibility: Boolean,
    axisZ1Visibility: Boolean,
    axisX1Action: AxisVisibility,
    axisY1Action: AxisVisibility,
    axisZ1Action: AxisVisibility
) {
    Card(elevation = 5.dp, shape = RoundedCornerShape(0.dp)) {
        Column(modifier = Modifier.padding(8.dp).wrapContentWidth().fillMaxHeight()) {
            LabelledCheckbox("x1 Axis", axisXVisibility) { axisX1Action.invoke(it) }
            LabelledCheckbox("y1 Axis", axisYVisibility) { axisY1Action.invoke(it) }
            LabelledCheckbox("z1 Axis", axisZ1Visibility) { axisZ1Action.invoke(it) }
            LabelledCheckbox("x2 Axis") {}
            LabelledCheckbox("y2 Axis") {}
            LabelledCheckbox("z2 Axis") {}
            LabelledCheckbox("x3 Axis") {}
            LabelledCheckbox("y3 Axis") {}
            LabelledCheckbox("z3 Axis") {}
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