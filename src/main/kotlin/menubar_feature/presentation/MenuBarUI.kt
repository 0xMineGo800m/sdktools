package menubar_feature.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import kotlinx.coroutines.launch
import menubar_feature.domain.MenuBarActions
import java.awt.FileDialog
import java.io.File
import java.nio.file.Path

/**
 * Created by Alon Minski on 26/02/2022.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.WindowMenuBar(actions: MenuBarActions) = MenuBar {

    val scope = rememberCoroutineScope()

    fun openFile() = scope.launch { actions.openFile() }
    fun connectToPort() = scope.launch { actions.connectToPort()}
    fun exit() = scope.launch { actions.exitApplication() }

    Menu("File") {
        Item("Open recording...", onClick = { openFile() }, shortcut = KeyShortcut(Key.O, meta = true))
        Item("Save", onClick = { }, shortcut = KeyShortcut(Key.S, meta = true))
        Separator()
        Item("Exit", onClick = { exit() }, shortcut = KeyShortcut(Key.Q, meta = true))
    }

    Menu("Actions") {
        Item("Connect to port", onClick = { connectToPort()})
        Item("Export", onClick = { })
    }

    if (actions.openDialog.isAwaiting) {
        FileDialog(
            title = "Load file",
            isLoad = true,
            onResult = { actions.openDialog.onResult(it) }
        )
    }
}

@Composable
fun FrameWindowScope.FileDialog(
    title: String,
    isLoad: Boolean,
    onResult: (result: Path?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(window, "Choose a file", if (isLoad) LOAD else SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title
        }
    },
    dispose = FileDialog::dispose
)