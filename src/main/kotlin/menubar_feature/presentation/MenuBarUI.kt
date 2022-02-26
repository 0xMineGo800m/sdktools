package menubar_feature.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import menubar_feature.domain.MenuBarActions

/**
 * Created by Alon Minski on 26/02/2022.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.WindowMenuBar(actions: MenuBarActions) = MenuBar {
    Menu("File") {
        Item("Open recording...", onClick = { actions.openFile(window) }, shortcut = KeyShortcut(Key.O, meta = true))
        Item("Save", onClick = { }, shortcut = KeyShortcut(Key.S, meta = true))
        Separator()
        Item("Exit", onClick = { actions.exitApplication() }, shortcut = KeyShortcut(Key.Q, meta = true))
    }

    Menu("Actions") {
        Item("Export", onClick = { })
    }
}