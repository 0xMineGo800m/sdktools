package menubar_feature.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import feature_graph.domain.ChartLogic
import feature_graph.domain.DataRepository
import feature_graph.presentation.graph_screen.ChartEvent
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.net.InetSocketAddress
import java.nio.file.Path


/**
 * Created by Alon Minski on 24/02/2022.
 */
class MenuBarActions(private val dataRepository: DataRepository, private val chartLogic: ChartLogic) {
    val openDialog = DialogState<Path?>()
    var shouldCloseApp by mutableStateOf(false)

    suspend fun openFile() {
        val pathToFile = openDialog.awaitResult()
        if (pathToFile != null) {
            dataRepository.recordingFile = pathToFile.toFile()
            chartLogic.onEvent(ChartEvent.OnFileLoaded)
        }
    }

    suspend fun exitApplication() {
        shouldCloseApp = true
    }

    // FEEDBACK:
    // This also belongs in a data related class like the repo :)
    suspend fun connectToPort() {
        CoroutineScope(IO).launch {

            val server = aSocket(ActorSelectorManager(IO)).tcp().bind(InetSocketAddress("127.0.0.1", 1337))
            println("[+] Started server ${server.localAddress}")

            while (true) {
                val socket = server.accept()

                launch {
                    println("[+] Socket accepted: ${socket.remoteAddress}")

                    val input = socket.openReadChannel()

                    try {
                        run loop@{
                            while (isActive) {
                                val line = input.readUTF8Line()
                                if (line.isNullOrEmpty()) {
                                    cancel()
                                    return@loop
                                }

                                chartLogic.onEvent(ChartEvent.OnSocketGotData(line))
                                println("[+] Input from socket: $line")
                            }
                        }

                        println("[+] Terminating socket...")
                        socket.close() // <-- is this a lint bug?

                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

class DialogState<T> {

    private var onResult: CompletableDeferred<T>? by mutableStateOf(null)
    val isAwaiting get() = onResult != null

    suspend fun awaitResult(): T {
        onResult = CompletableDeferred()
        val result = onResult!!.await()
        onResult = null
        return result
    }

    fun onResult(result: T) = onResult!!.complete(result)
}