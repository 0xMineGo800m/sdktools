package feature_graph.presentation.graph_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import feature_graph.domain.ChartLogic
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYSplineRenderer
import org.jfree.data.Range
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
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
            Button(onClick = { chartLogic.onEvent(ChartEvent.PlayRecordingFile) }, enabled = chartLogic.graphState.isFileLoaded) {
                Text("Play recording")
            }

            Button(onClick = { chartLogic.onEvent(ChartEvent.StopRecordingFile) }, enabled = chartLogic.isUpdateActive) {
                Text("Stop recording")
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.padding(5.dp), Arrangement.spacedBy(5.dp)) {
            val axisVisibilities = ArrayList<Boolean>()
            axisVisibilities.add(chartLogic.graphState.axisX1Visible)
            axisVisibilities.add(chartLogic.graphState.axisY1Visible)
            axisVisibilities.add(chartLogic.graphState.axisZ1Visible)

            AxisSelector(
                { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisX1", isChecked)) },
                { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisY1", isChecked)) },
                { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisZ1", isChecked)) }
            )

            val axis = ArrayList<TimeSeries>()
            axis.add(chartLogic.axisX1Series)
            axis.add(chartLogic.axisY1Series)
            axis.add(chartLogic.axisZ1Series)

            TheChart(axisVisibilities, axis)
        }
    }
}

@Composable
private fun AxisSelector(axisX1Action: AxisVisibility, axisY1Action: AxisVisibility, axisZ1Action: AxisVisibility) {
    Card(elevation = 5.dp, shape = RoundedCornerShape(0.dp)) {
        Column(modifier = Modifier.padding(8.dp).wrapContentWidth().fillMaxHeight()) {
            LabelledCheckbox("x1 Axis", true) { axisX1Action.invoke(it) }
            LabelledCheckbox("y1 Axis", true) { axisY1Action.invoke(it) }
            LabelledCheckbox("z1 Axis", true) { axisZ1Action.invoke(it) }
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
fun LabelledCheckbox(label: String, initCheckedState: Boolean = false, action: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val isChecked = remember { mutableStateOf(initCheckedState) }
        Checkbox(
            checked = isChecked.value,
            onCheckedChange = {
                isChecked.value = it
                action(it)
            },
            enabled = true,
            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
        )
        Text(text = label, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun TheChart(axisVisibilities: List<Boolean>, axis: List<TimeSeries>) {
    Card(elevation = 5.dp, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxSize()) {

        val dataset = TimeSeriesCollection()
        axis.forEach { axis ->
            axis.maximumItemCount = 200
            dataset.addSeries(axis)
        }

        val renderer = XYSplineRenderer()

        axisVisibilities.forEachIndexed { index, isVisible ->
            renderer.setSeriesPaint(index, ui.theme.colorMap[index])
            renderer.setSeriesShapesVisible(index, false)
            renderer.setSeriesVisible(index, isVisible)
        }

        val xAxis = DateAxis("Time")
        val yAxis = NumberAxis("Value")
        yAxis.range = Range(-10.0, 10.0)
        val plot = XYPlot(dataset, xAxis, yAxis, renderer)
        val chart = JFreeChart("Plotter", JFreeChart.DEFAULT_TITLE_FONT, plot, true)
        chart.xyPlot.isDomainPannable = true
        chart.xyPlot.isRangePannable = true
        val frame = ChartPanel(chart)
        frame.isVisible = true

        Box {
            SwingPanel(
                factory = {
                    JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        add(frame)
                    }
                })
        }
    }
}