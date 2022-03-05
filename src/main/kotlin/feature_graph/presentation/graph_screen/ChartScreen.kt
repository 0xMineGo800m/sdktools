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
            Button(
                onClick = { chartLogic.onEvent(ChartEvent.PlayRecordingFile) },
                enabled = chartLogic.graphState.isFileLoaded
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
            // FEEDBACK:
            // Here, you're re-initializing this list (and adding items) on every recomposition of this row.
            // This is called a side-effect and should be avoided.
            // This is all business logic and should go into chart logic. So, axisVisibilities
            // should effectively become a state in your chart logic class.
            val axisVisibilities = ArrayList<Boolean>()

            // Observing which AXIS is visible states...
            axisVisibilities.add(chartLogic.graphState.axisX1Visible)
            axisVisibilities.add(chartLogic.graphState.axisY1Visible)
            axisVisibilities.add(chartLogic.graphState.axisZ1Visible)

            // When changing check boxes, will inform ChartLogic of the UI change

            // FEEDBACK:
            // This is good and correct sending of events to the business layer :)
            // I'd personally prefer a name like OnAxisVisibilityChange over AxisVisibility
            // because it's more expressive about what the event tells the business layer
            AxisSelector(
                { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisX1", isChecked)) },
                { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisY1", isChecked)) },
                { isChecked -> chartLogic.onEvent(ChartEvent.AxisVisibility("AxisZ1", isChecked)) }
            )

            // FEEDBACK:
            // Same as above, this should be avoided. What you could also do here is something like this:

            // val axis = remember(
            //     chartLogic.axisX1Series,
            //     chartLogic.axisY1Series,
            //     chartLogic.axisZ1Series
            // ) {
            //     listOf(chartLogic.axisX1Series, chartLogic.axisY1Series, chartLogic.axisZ1Series)
            // }

            // This would initialize the axis list once and then only update it as soon as one of the
            // passed states change.

            // Adding the 3 axis data sets to the JFreeChart object...
            val axis = ArrayList<TimeSeries>()
            axis.add(chartLogic.axisX1Series)
            axis.add(chartLogic.axisY1Series)
            axis.add(chartLogic.axisZ1Series)

            TheChart(axisVisibilities, axis, chartLogic)
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

// FEEDBACK:
// Don't use initial states as parameters to composables. I fully understand your thinking here
// because I did this mistake so often as well when I started learning it :D
// Explanation: If you use something like initCheckedState and then use that to initialize the
// state in the labelled checkbox, there is no way for you to change this state from other composables.
// Imagine, you'd like to check this checkbox when another checkbox is clicked. With this implementation
// it doesn't work because each LabelledCheckbox handles its own state.
// The solution/better way is to use state hoisting here.
// Something like this:

//@Composable
//fun LabelledCheckbox(
//    label: String,
//    isChecked: Boolean,
//    action: (Boolean) -> Unit
//) {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Checkbox(
//            checked = isChecked,
//            onCheckedChange = {
//                action(it)
//            },
//            enabled = true,
//            colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
//        )
//        Text(text = label, style = MaterialTheme.typography.body2)
//    }
//}

// The state shouldn't be handled by the composables themselves, it should be handled
// in your case by the chart logic class. So wherever you would now use this composable,
// you could send an OnCheckedChange event to the chart logic class and update the state there
// which would make this checkbox recompose.

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

/**
 * Using a JFreeChart object to render graphs. This is an old library and is written in Java Swing. Using a SwingPanel I can add it to the Compose tree.
 */
@Composable
fun TheChart(axisVisibilities: List<Boolean>, axis: List<TimeSeries>, chartLogic: ChartLogic) {
    Card(elevation = 5.dp, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxSize()) {

        // FEEDBACK:
        // ALL of this should go in another class like the chart logic one. This gets called on
        // every recomposition. As soon as you use non-compose code in a composable function, this
        // is always a danger sign.
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
        val plot = XYPlot(dataset, xAxis, yAxis, renderer)
        val chart = JFreeChart("Plotter", JFreeChart.DEFAULT_TITLE_FONT, plot, true)
        chart.xyPlot.isDomainPannable = true
        chart.xyPlot.isRangePannable = true
        chart.xyPlot.rangeAxis.range = chartLogic.graphState.yAxisRange
        chart.xyPlot.addChangeListener { event -> chartLogic.onEvent(ChartEvent.OnChartPlotChangeEvent(event)) }

        // FEEDBACK:
        // It's frightening indeed :D
        val frame =
            ChartPanel(chart) // <-- This call is a bit frightening. If this gets called each recomposition... isn't that a performance hit?? Do I need to use LaunchedEffect here?
        frame.isVisible = true

        Box {
            SwingPanel(
                factory = {
                    JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        add(frame)
                    }
                }
            )
        }
    }
}