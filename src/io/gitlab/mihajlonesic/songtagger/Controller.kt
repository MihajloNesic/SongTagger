package io.gitlab.mihajlonesic.songtagger

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class Controller(private val stage: Stage) {

    // Controls from the form
    @FXML lateinit var songPath: TextField
    @FXML lateinit var selectSong: Button

    // Other controls
    private val audioFilerMp3 = FileChooser.ExtensionFilter("MP3 (*.mp3)", "*.mp3")
    private val songFileChooser = FileChooser()

    // Variables
    private lateinit var song: Song
    private lateinit var songFile: File

    /**
     * Initialize form actions and other controls
     */
    internal fun init() {
        selectSong.setOnAction { handleSelectSong() }

        songFileChooser.title = "Select Song"
        songFileChooser.extensionFilters.add(audioFilerMp3)
    }

    private fun handleSelectSong() {
        var file = songFileChooser.showOpenDialog(stage)
        if(file != null) {
            songFile = file;
            println("Selected song path: ${songFile.absolutePath}")
            songPath.text = songFile.absolutePath
        }
    }

}