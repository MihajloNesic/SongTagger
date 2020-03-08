package io.gitlab.mihajlonesic.songtagger

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.id3.ID3v1Tag
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.logging.ConsoleHandler
import java.util.logging.Logger

class Controller(private val stage: Stage) {

    // Program parameters
    private val version = "2.2"
    private val logger = Logger.getLogger(Controller::class.java.name)

    private val programIcon = Image(SongTagger::class.java.getResourceAsStream("/icon.png"))

    // Controls from the form
    @FXML lateinit var infoLabel: Label

    @FXML lateinit var songPath: TextField
    @FXML lateinit var selectSong: Button

    @FXML lateinit var songArtwork: ImageView
    @FXML lateinit var changeArtwork: Button
    @FXML lateinit var downloadArtwork: Button
    @FXML lateinit var removeArtwork: Button

    @FXML lateinit var albumField: TextField
    @FXML lateinit var titleField: TextField
    @FXML lateinit var artistField: TextField
    @FXML lateinit var trackNumberField: TextField
    @FXML lateinit var yearField: TextField
    @FXML lateinit var comboGenres: ComboBox<String>

    @FXML lateinit var removeTags: Button
    @FXML lateinit var save: Button

    @FXML lateinit var colorPicker: ColorPicker

    @FXML lateinit var root: VBox

    // Other controls
    private val audioFilters = FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.m4a")
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
    private lateinit var songArtArtwork: Artwork

    // Properties
    private val configFilePath = File("songtagger.properties").path
    private var propColor: Color? = null

    private var hasArtwork = false

    // https://en.wikipedia.org/wiki/List_of_ID3v1_Genres
    private val genres = FXCollections.observableArrayList(
        "None",
        "Other",
        "Acid",
        "Acid Jazz",
        "Acid Punk",
        "Alternative",
        "Alternative Rock",
        "Ambient",
        "Bass",
        "Blues",
        "Cabaret",
        "Christian Rap",
        "Classical",
        "Classic Rock",
        "Comedy",
        "Country",
        "Cult",
        "Dance",
        "Darkwave",
        "Death Metal",
        "Disco",
        "Dream",
        "Electronic",
        "Ethnic",
        "Euro-Techno",
        "Eurodance",
        "Funk",
        "Fusion",
        "Game",
        "Gangsta",
        "Gospel",
        "Gothic",
        "Grunge",
        "Hard Rock",
        "Hip-Hop",
        "House",
        "Industrial",
        "Instrumental",
        "Instrumental Pop",
        "Instrumental Rock",
        "Jazz",
        "Jazz & Funk",
        "Jungle music",
        "Lo-Fi",
        "Meditative",
        "Metal",
        "Musical",
        "Native US",
        "New Age",
        "New Wave",
        "Noise",
        "Oldies",
        "Polka",
        "Pop",
        "Pop-Folk",
        "Pop/Funk",
        "Pranks",
        "Psychedelic",
        "Punk",
        "Rap",
        "Rave",
        "Reggae",
        "Retro",
        "Rhythm and Blues",
        "Rock",
        "Rock ’n’ Roll",
        "Showtunes",
        "Ska",
        "Soul",
        "Sound clip",
        "Soundtrack",
        "Southern Rock",
        "Space",
        "Techno",
        "Techno-Industrial",
        "Top 40",
        "Trailer",
        "Trance",
        "Tribal",
        "Trip-Hop",
        "Vocal"
    )

    /**
     * Gets genre name based on genre combo selection
     */
    private val genreName
        get() = when(comboGenres.selectionModel.selectedIndex) {
            -1 -> ""
            0 -> ""
            else -> comboGenres.items[comboGenres.selectionModel.selectedIndex]
        }

    /**
     * Handles input for number-only input fields
     */
    private var numberInputHandler: EventHandler<KeyEvent> = object : EventHandler<KeyEvent> {
        private var willConsume = false

        override fun handle(event: KeyEvent) {
            if (willConsume)
                event.consume()

            if (!event.code.isDigitKey) {
                if (event.eventType == KeyEvent.KEY_PRESSED)
                    willConsume = true
                else if (event.eventType == KeyEvent.KEY_RELEASED)
                    willConsume = false
            }
        }
    }

    /**
     * Initialize form actions and other controls
     */
    internal fun init() {
        logger.useParentHandlers = false
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = LogFormatter()
        logger.addHandler(consoleHandler)

        logger.info("SongTagger $version starting...")

        song = Song()

        loadPropertyFile()

        infoLabel.setOnMouseClicked { handleInfoClicked() }

        selectSong.setOnAction { handleSelectSong() }
        changeArtwork.setOnAction { handleChangeArtwork() }
        downloadArtwork.setOnAction { handleArtworkDownload() }
        removeArtwork.setOnAction { handleRemoveArtwork() }
        removeTags.setOnAction { handleRemoveTags() }
        save.setOnAction { handleSave() }

        trackNumberField.addEventFilter(KeyEvent.ANY, numberInputHandler)
        yearField.addEventFilter(KeyEvent.ANY, numberInputHandler)

        songFileChooser.title = "Select Song"
        songFileChooser.extensionFilters.addAll(audioFilters, audioFilterMp3)

        songArtworkChooser.title = "Select an Artwork"
        songArtworkChooser.extensionFilters.addAll(imageFilters, imageFilterPNG, imageFilterJPEG)

        songSaveArtworkChooser.title = "Save an Artwork"
        songSaveArtworkChooser.extensionFilters.addAll(imageFilterPNG)

        songSaveChooser.title = "Save to"

        colorPicker.value = if(propColor != null) propColor else Color.valueOf("#ececec")
        colorPicker.setOnAction { handleColorChanged(colorPicker.value, true) }
        handleColorChanged(colorPicker.value, false)

        comboGenres.items.addAll(genres)

        Platform.runLater{ selectSong.requestFocus() }

        logger.info("SongTagger $version initialized and started")
    }

    /**
     * Loads a property file
     */
    private fun loadPropertyFile() {
        try {
            logger.info("Configuration file found at $configFilePath")

            val input = FileInputStream(configFilePath)
            val prop = Properties()
            prop.load(input)

            val color = prop.getProperty("color")
            if(color != null) propColor = Color.valueOf(color)

            input.close()

            logger.info("Configuration file loaded")
        } catch (ex: IOException) {
            logger.warning("Could not load configuration file")
        } catch (ex: IllegalArgumentException) {
            logger.warning("Some values in configuration file were invalid")
        } catch (ex: Exception) {
            logger.warning("Could not find configuration file")
        }
    }

    /**
     * Saves a property to a configuration file
     *
     * @param propertyName Name of the property
     * @param propertyValue Value of the property
     */
    private fun saveProperty(propertyName: String, propertyValue: String) {
        val prop = Properties()
        try {
            val input = FileInputStream(configFilePath)
            prop.load(input)
            input.close()
        } catch (ex: IOException) {
            logger.warning("Configuration file not found and will be created")
        }
        try {
            val output = FileOutputStream(configFilePath)
            prop.setProperty(propertyName, propertyValue)
            prop.store(output, null)
            output.close()
            logger.info("Property '$propertyName' saved to '$propertyValue' in a configuration file")
        } catch (ex: IOException) {
            logger.warning("Could not save property '$propertyName' to configuration file")
        } catch (ex: Exception) {
            logger.warning("Could not save to configuration file")
        }
    }

    /**
     * Show program info
     */
    private fun handleInfoClicked() {
        Alert(Alert.AlertType.INFORMATION).apply {
            val iconImageView = ImageView(programIcon)
            iconImageView.fitWidth = 64.0
            iconImageView.fitHeight = 64.0
            graphic = iconImageView
            headerText = "SongTagger by Mihajlo Nesic"
            title = "SongTagger"
            contentText = "Version $version\n\nA JavaFX app for tagging audio files. Built entirely in Kotlin.\n\n"
            width = 200.0
            buttonTypes.clear()

            val window = this.dialogPane.scene.window as Stage
            window.icons.add(programIcon)

            val buttonWebsite = ButtonType("Website", ButtonBar.ButtonData.LEFT)

            buttonTypes.addAll(buttonWebsite, ButtonType.CLOSE)

            val result = showAndWait()

            if (result.get() == buttonWebsite) {
                try {
                    Desktop.getDesktop().browse(URI("https://mihajlonesic.gitlab.io/projects/songtagger/"))
                } catch (ex: Exception) {}
            }

        }
    }

    /**
     * Handles song selection
     */
    private fun handleSelectSong() {
        val file = songFileChooser.showOpenDialog(stage)
        if(file != null) {
            songFile = file
            logger.info("Selected song path: ${songFile.absolutePath}")
            songPath.text = songFile.absolutePath
            clearControls(false)
            readTags()
            controlButtons(false)
            controlFields(false)
        }
    }

    /**
     * Handles artwork changing
     */
    private fun handleChangeArtwork() {
        val file = songArtworkChooser.showOpenDialog(stage)
        if(file != null) {
            songArtFile = file
            logger.info("Artwork image path: ${songArtFile.absolutePath}")
            setSongImageView(songArtFile)
        }
    }

    /**
     * Downloads a song artwork image from the song tag
     */
    private fun handleArtworkDownload() {
        if(hasArtwork) {
            val image = SwingFXUtils.toFXImage(songArtArtwork.image as BufferedImage?, null)
            val file = songSaveArtworkChooser.showSaveDialog(stage)
            if (file != null) {
                Util.saveImageToFile(image, file)
                Util.alertConfirm("Artwork saved to ${file.absolutePath}")
            }
        }
    }

    /**
     * Removes the song artwork tag
     */
    private fun handleRemoveArtwork() {
        if(hasArtwork) {
            val alert = Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
            alert.headerText = null
            alert.title = "Removing song artwork"
            alert.contentText = "Are you sure you want to remove artwork from the song?"
            val window = alert.dialogPane.scene.window as Stage
            window.icons.add(programIcon)

            alert.showAndWait().ifPresent { type ->
                when (type) {
                    ButtonType.YES -> {
                        logger.info("Removing artwork...")
                        try {
                            val audioFile = AudioFileIO.read(songFile)
                            val tag = audioFile.tag

                            tag.deleteArtworkField()
                            audioFile.commit()

                            logger.info("Artwork removed.")

                            clearControls(true)
                            controlFields(true)
                            controlButtons(true)
                            Util.alertConfirm("Artwork removed")
                        }
                        catch (ex: Exception) {
                            Util.alertError("An error has occurred. Try again.")
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles GUI color changing
     *
     * @param value Color that will be applied
     */
    private fun handleColorChanged(value: Color, shouldSave: Boolean) {
        val color = Util.toHEX(value)
        val colorDefBtn = Util.toHEX(value.darker().darker())
        root.style = "-fx-base: $color; -fx-default-button: $colorDefBtn;"
        if(shouldSave) saveProperty("color", color)
    }

    /**
     * Handles reading song tags
     */
    private fun readTags() {
        val audioFile = AudioFileIO.read(songFile)
        val tag = audioFile.tagOrCreateAndSetDefault

        song = Song()
        song.album = tag.getFirst(FieldKey.ALBUM)
        song.title = tag.getFirst(FieldKey.TITLE)
        song.artist = tag.getFirst(FieldKey.ARTIST)
        song.trackNumber = tag.getFirst(FieldKey.TRACK)
        song.year = tag.getFirst(FieldKey.YEAR)

        val genre = tag.getFirst(FieldKey.GENRE)

        if(genre.isNotEmpty()) {
            song.genre = genre
        }

        if(!tag.artworkList.isNullOrEmpty()) {
            val artwork = tag.firstArtwork
            if(artwork != null) {
                try {
                    val artworkImage = SwingFXUtils.toFXImage(artwork.image as BufferedImage?, null)
                    songArtwork.image = artworkImage
                    songArtArtwork = artwork
                    hasArtwork = true
                }
                catch(ex: NullPointerException) {
                    hasArtwork = false
                    logger.warning("No artwork data")
                }
            }
        } else {
            hasArtwork = false
            logger.warning("No artwork data")
        }

        setFieldsFromSong(song)
        logger.info("Song data: $song")

        // Little easter egg :)
        val comment = tag.getFirst(FieldKey.COMMENT)
        if(comment == "SongTagger by MihajloNesic") {
            logger.info("Song was tagged with SongTagger! :)")
            save.text = "Save :)"
        }
        else save.text = "Save"
    }

    /**
     * Removes all tag data from song file
     */
    private fun handleRemoveTags() {
        val alert = Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        alert.headerText = null
        alert.title = "Removing song tag data"
        alert.contentText = "Are you sure you want to remove all tag data from the song?"
        val window = alert.dialogPane.scene.window as Stage
        window.icons.add(programIcon)

        alert.showAndWait().ifPresent { type ->
            when (type) {
                ButtonType.YES -> {
                    when {
                        songFile.extension == "mp3" -> {
                            logger.info("Removing MP3 file tag data...")

                            val audioFile = AudioFileIO.read(songFile) as MP3File

                            // ID3v1
                            if(audioFile.hasID3v1Tag()) {
                                logger.info("Removing ID3v1...")
                                audioFile.delete(audioFile.iD3v1Tag)
                            }

                            // ID3v2
                            if(audioFile.hasID3v2Tag()) {
                                logger.info("Removing ID3v2...")
                                audioFile.delete(audioFile.iD3v2Tag)
                            }

                            logger.info("Committing...")
                            audioFile.commit()

                            clearControls(true)
                            controlFields(true)
                            controlButtons(true)

                            logger.info("Song tags removed.")
                            Util.alertConfirm("Tag data has been removed")
                        }
                        songFile.extension == "m4a" -> {
                            // TODO: Implement tag removing for m4a files
                            Util.alertError("Removing tag data from M4A files is not supported yet :(")
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles song saving
     */
    private fun handleSave() {
        logger.info("Saving ")
        when {
            songFile.extension == "mp3" -> {
                logger.info("MP3...")

                setSongFromFields()
                logger.info("New song data: $song")

                val audioFile = AudioFileIO.read(songFile) as MP3File

                // ID3v1
                if(audioFile.hasID3v1Tag()) {
                    logger.info("Removing ID3v1...")
                    audioFile.delete(audioFile.iD3v1Tag)
                }

                val id3v1Tag = ID3v1Tag()

                setTagFieldsFromSong(id3v1Tag, song)

                logger.info("Setting ID3v1")
                audioFile.iD3v1Tag = id3v1Tag

                // ID3v2
                if(audioFile.hasID3v2Tag()) {
                    logger.info("Removing ID3v2...")
                    audioFile.delete(audioFile.iD3v2Tag)
                }

                val id3v2Tag = audioFile.tagOrCreateDefault

                setTagFieldsFromSong(id3v2Tag, song)

                if(hasArtwork) {
                    id3v2Tag.deleteArtworkField()
                    id3v2Tag.setField(songArtArtwork)
                }

                logger.info("Setting ID3v2")
                audioFile.tag = id3v2Tag

                logger.info("Committing...")
                audioFile.commit()

                logger.info("Saved!")
                Util.alertConfirm("Song successfully saved!")
            }
            songFile.extension == "m4a" -> {
                logger.info("M4A...")

                val audioFile = AudioFileIO.read(songFile)
                val tag = audioFile.tag

                setSongFromFields()
                logger.info("New song data: $song")

                setTagFieldsFromSong(tag, song)

                if(hasArtwork) {
                    tag.deleteArtworkField()
                    tag.setField(songArtArtwork)
                }

                logger.info("Committing...")
                audioFile.commit()

                logger.info("Saved!")
                Util.alertConfirm("Song successfully saved!")
            }
            else -> Util.alertError("File extension not supported yet :(")
        }
    }

    /**
     * Sets tag fields from song
     *
     * @param tag Tag
     * @param song Song
     */
    private fun setTagFieldsFromSong(tag: Tag, song: Song) {
        if(!song.album.isBlank()) tag.setField(FieldKey.ALBUM, song.album)
        if(!song.title.isBlank()) tag.setField(FieldKey.TITLE, song.title)
        if(!song.artist.isBlank()) tag.setField(FieldKey.ARTIST, song.artist)
        if(!song.trackNumber.isBlank()) tag.setField(FieldKey.TRACK, song.trackNumber)
        if(!song.year.isBlank()) tag.setField(FieldKey.YEAR, song.year)
        if(!song.genre.isBlank()) tag.setField(FieldKey.GENRE, song.genre)
        tag.setField(FieldKey.COMMENT, "SongTagger by MihajloNesic")
    }

    /**
     * Enables or disables all buttons except song selection
     *
     * @param disable Are controls disabled
     */
    private fun controlButtons(disable: Boolean) {
        changeArtwork.isDisable = disable
        downloadArtwork.isDisable = disable
        removeArtwork.isDisable = disable
        removeTags.isDisable = disable
        save.isDisable = disable
    }

    /**
     * Enables or disables input fields
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
     * Clears all controls setting them to default states
     *
     * @param clearSongPath Should this method clear 'songPath' field
     */
    private fun clearControls(clearSongPath: Boolean) {
        if(clearSongPath) songPath.clear()
        albumField.clear()
        titleField.clear()
        artistField.clear()
        trackNumberField.clear()
        yearField.clear()
        comboGenres.selectionModel.select(-1)
        songArtwork.image = null
        System.gc()
    }

    /**
     * Sets an image to `songArtwork` image view
     *
     * @param file an image file
     */
    private fun setSongImageView(file: File) {
        val image = Image(file.toURI().toString(), 800.0, 800.0, false, true)
        songArtwork.image = image
        songArtArtwork = ArtworkFactory.createArtworkFromFile(file)
        hasArtwork = true
    }

    /**
     * Sets fields based on song information
     *
     * @param song a song to read info from
     */
    private fun setFieldsFromSong(song: Song) {
        albumField.text = song.album
        titleField.text = song.title
        artistField.text = song.artist
        trackNumberField.text = song.trackNumber
        yearField.text = song.year

        if(song.genre.isNotEmpty()) {
            comboGenres.selectionModel.select(song.genre)
        } else comboGenres.selectionModel.select(-1)
    }

    /**
     * Sets song information based on fields
     */
    private fun setSongFromFields() {
        song.album = albumField.text
        song.title = titleField.text
        song.artist = artistField.text
        song.trackNumber = trackNumberField.text
        song.year = yearField.text
        song.genre = genreName
    }
}