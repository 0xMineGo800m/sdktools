package menubar_feature.domain

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.ApplicationScope
import feature_graph.domain.ChartLogic
import feature_graph.domain.DataRepository
import javax.swing.JFileChooser

/**
 * Created by Alon Minski on 24/02/2022.
 */
class MenuBarActions(private val applicationScope: ApplicationScope) {

    fun openFile(parent: ComposeWindow) {
        val f = JFileChooser()
        f.fileSelectionMode = JFileChooser.FILES_ONLY

        when (f.showOpenDialog(parent)) {
            JFileChooser.APPROVE_OPTION -> {
                DataRepository.recordingFile = f.selectedFile
                ChartLogic.isFileLoaded = true
            }
        }
    }

    fun exitApplication() {
        applicationScope.exitApplication()
    }
}