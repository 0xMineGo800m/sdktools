package feature_graph.data

/**
 * Created by Alon Minski on 17/03/2022.
 */
data class TimeAxisMeta(val names: ArrayList<String> = arrayListOf()) {

    val axisCount: Int
        get() = names.size
}
