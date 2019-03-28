package io.gitlab.mihajlonesic.songtagger

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import javafx.application.Platform
import javafx.stage.DirectoryChooser
import java.io.IOException
import javax.imageio.ImageIO
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.ComboBox
import java.awt.image.BufferedImage



class Controller(private val stage: Stage) {

    // Controls from the form
    @FXML lateinit var songPath: TextField
    @FXML lateinit var selectSong: Button

    @FXML lateinit var songArtwork: ImageView
    @FXML lateinit var changeArtwork: Button
    @FXML lateinit var downloadArtwork: Button

    @FXML lateinit var albumField: TextField
    @FXML lateinit var titleField: TextField
    @FXML lateinit var artistField: TextField
    @FXML lateinit var trackNumberField: TextField
    @FXML lateinit var yearField: TextField
    @FXML lateinit var comboGenres: ComboBox<String>

    @FXML lateinit var save: Button

    // Other controls
    private val audioFilterMp3 = FileChooser.ExtensionFilter("MP3 (*.mp3)", "*.mp3")
    private val imageFilters = FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.tiff", "*.gif")
    private val imageFilterPNG = FileChooser.ExtensionFilter("PNG (*.png)", "*.png")
    private val imageFilterJPEG = FileChooser.ExtensionFilter("JPEG (*.jpg; *.jpeg; *.jpe; *.jfif)", "*.jpg; *.jpeg; *.jpe; *.jfif")

    private val songFileChooser = FileChooser()
    private val songArtworkChooser = FileChooser()
    private val songSaveArtworkChooser = FileChooser()
    private val songSaveChooser = DirectoryChooser()

    // Variables
    private lateinit var song: Song
    private lateinit var songFile: File
    private lateinit var songArtFile: File

    // TODO: Add all ID3v1 genres
    // https://en.wikipedia.org/wiki/List_of_ID3v1_Genres
    private val genreID
        get() = (comboGenres.selectionModel.selectedIndex)

    /**
     * Initialize form actions and other controls
     */
    internal fun init() {
        selectSong.setOnAction { handleSelectSong() }
        changeArtwork.setOnAction { handleChangeArtwork() }
        downloadArtwork.setOnAction { handleArtworkDownload() }

        songFileChooser.title = "Select Song"
        songFileChooser.extensionFilters.add(audioFilterMp3)

        songArtworkChooser.title = "Select an Artwork"
        songArtworkChooser.extensionFilters.addAll(imageFilters, imageFilterPNG, imageFilterJPEG)

        songSaveArtworkChooser.title = "Save an Artwork"
        songSaveArtworkChooser.extensionFilters.addAll(imageFilterPNG, imageFilterJPEG)

        songSaveChooser.title = "Save to"

        save.setOnAction { handleSave() }

        Platform.runLater{ selectSong.requestFocus() }
    }

    /**
     * Handles song selection
     */
    private fun handleSelectSong() {
        var file = songFileChooser.showOpenDialog(stage)
        if(file != null) {
            songFile = file;
            println("Selected song path: ${songFile.absolutePath}")
            songPath.text = songFile.absolutePath
            this.readTags()
            this.controlButtons(false)
            this.controlFields(false)
        }
    }

    /**
     * Handles artwork changing
     */
    private fun handleChangeArtwork() {
        var file = songArtworkChooser.showOpenDialog(stage)
        if(file != null) {
            songArtFile = file
            println("Artwork image path: ${songArtFile.absolutePath}")
            setSongImageView(songArtFile)
        }
    }

    /**
     * This method currently downloads an image from the ImageView control
     *
     * Inevitably, when the song info and artwork is fetched, this method should
     * extract the artwork from the song file.
     *
     * TODO: Download artwork from file, not ImageView
     */
    private fun handleArtworkDownload() {
        var image = songArtwork.image
        if(image != null) {
            var file = songSaveArtworkChooser.showSaveDialog(stage)
            if (file != null) {
                saveImageToFile(image, file)
                Util.alertConfirm("Artwork saved to ${file.absolutePath}")
            }
        }
    }

    /**
     * Handles song saving
     */
    private fun handleSave() {
        // TODO: Save functionality
    }

    /**
     * Handles reading song tags
     */
    private fun readTags() {
        // TODO: Reading song tags functionality
    }

    /**
     * Enables or disables all buttons except song selection
     * TODO: Refactor method
     *
     * @param disable Are controls disabled
     */
    private fun controlButtons(disable: Boolean) {
        changeArtwork.isDisable = disable
        downloadArtwork.isDisable = disable
        save.isDisable = disable
    }

    /**
     * Enables or disables input fields
     * TODO: Refactor method
     *
     * @param disable Are fields disabled
     */
    private fun controlFields(disable: Boolean) {
        albumField.isDisable = disable
        titleField.isDisable = disable
        artistField.isDisable = disable
        trackNumberField.isDisable = disable
        yearField.isDisable = disable
        comboGenres.isDisable = disable
    }

    /**
     * Sets an image to `songArtwork` image view
     *
     * @param path The path of an image file
     */
    private fun setSongImageView(path: String) {
        val image = Image(path, 800.0, 800.0, false, true)
        songArtwork.image = image
    }

    /**
     * Sets an image to `songArtwork` image view
     *
     * @param file an image file
     * @see setSongImageView
     */
    private fun setSongImageView(file: File) {
        setSongImageView(file.toURI().toString())
    }

    /**
     * Extracts an image to a file
     *
     * @param image Image to be extracted/saved
     * @param outputFile Image file
     */
    private fun saveImageToFile(image: Image, outputFile: File) {
        val bImage = SwingFXUtils.fromFXImage(image, null)
        try {
            ImageIO.write(bImage, outputFile.extension, outputFile)
        } catch (e: IOException) {
            throw IOException(e)
        }
    }

}