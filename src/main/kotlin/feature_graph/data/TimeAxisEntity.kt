package feature_graph.data

/**
 * Created by Alon Minski on 26/02/2022.
 */
data class TimeAxisEntity(
    val axes: List<Double>,
    val timestamp: Long
)

data class DataLine(val value: TimeAxisEntity)
