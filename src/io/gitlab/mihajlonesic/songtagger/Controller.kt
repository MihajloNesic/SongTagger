package io.gitlab.mihajlonesic.songtagger

import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v1Tag
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.awt.image.BufferedImage
import javafx.scene.control.ButtonType
import javafx.scene.control.Alert
import java.awt.Desktop
import java.lang.NullPointerException
import java.net.URI
import java.text.SimpleDateFormat

class Controller(private val stage: Stage) {

    // Program parameters
    private val DEV = false
    private val VERSION = "1.1"

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

    private var hasArtwork = false

    // TODO: Re-work genre list?
    // https://en.wikipedia.org/wiki/List_of_ID3v1_Genres
    private val genres = FXCollections.observableArrayList(
        "None",
        "Blues",
        "Classic Rock",
        "Country",
        "Dance",
        "Disco",
        "Funk",
        "Grunge",
        "Hip-Hop",
        "Jazz",
        "Metal",
        "New Age",
        "Oldies",
        "Other",
        "Pop",
        "Rhythm and Blues",
        "Rap",
        "Reggae",
        "Rock",
        "Techno",
        "Industrial",
        "Alternative",
        "Ska",
        "Death Metal",
        "Pranks",
        "Soundtrack",
        "Euro-Techno",
        "Ambient",
        "Trip-Hop",
        "Vocal",
        "Jazz & Funk",
        "Fusion",
        "Trance",
        "Classical",
        "Instrumental",
        "Acid",
        "House",
        "Game",
        "Sound clip",
        "Gospel",
        "Noise",
        "Alternative Rock",
        "Bass",
        "Soul",
        "Punk",
        "Space",
        "Meditative",
        "Instrumental Pop",
        "Instrumental Rock",
        "Ethnic",
        "Gothic",
        "Darkwave",
        "Techno-Industrial",
        "Electronic",
        "Pop-Folk",
        "Eurodance",
        "Dream",
        "Southern Rock",
        "Comedy",
        "Cult",
        "Gangsta",
        "Top 40",
        "Christian Rap",
        "Pop/Funk",
        "Jungle music",
        "Native US",
        "Cabaret",
        "New Wave",
        "Psychedelic",
        "Rave",
        "Showtunes",
        "Trailer",
        "Lo-Fi",
        "Tribal",
        "Acid Punk",
        "Acid Jazz",
        "Polka",
        "Retro",
        "Musical",
        "Rock ’n’ Roll",
        "Hard Rock"
    )

    /**
     * Gets genre ID based on selected genre
     * ID is based on ID3v1 genre
     * https://en.wikipedia.org/wiki/List_of_ID3v1_Genres
     */
    private val genreID
        get() = when(comboGenres.selectionModel.selectedIndex) {
            -1 -> -1
            0 -> -1
            else -> comboGenres.selectionModel.selectedIndex - 1
        }

    /**
     * Gets genre name based on ID3v1 genre id
     */
    private val genreName
        get() = when(genreID) {
            -1 -> ""
            else -> comboGenres.items[genreID+1]
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
        song = Song()

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

        colorPicker.value = Color.valueOf("#ececec")
        colorPicker.setOnAction { handleColorChanged() }

        comboGenres.items.addAll(genres)

        Platform.runLater{ selectSong.requestFocus() }
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
            contentText = "Version $VERSION\n\nA JavaFX app for tagging audio files. Built entirely in Kotlin.\n\n"
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
            devLog("\nSelected song path: ${songFile.absolutePath}")
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
            devLog("Artwork image path: ${songArtFile.absolutePath}")
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
                        devLog("Removing artwork...")
                        try {
                            val audioFile = AudioFileIO.read(songFile)
                            val tag = audioFile.tag

                            tag.deleteArtworkField()
                            audioFile.commit()

                            devLog("Artwork removed.")

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
     */
    private fun handleColorChanged() {
        val color = Util.toHEX(colorPicker.value)
        val colorDefBtn = Util.toHEX(colorPicker.value.darker().darker())
        root.style = "-fx-base: $color; -fx-default-button: $colorDefBtn;"
    }

    /**
     * Handles reading song tags
     */
    private fun readTags() {
        val audioFile = AudioFileIO.read(songFile)
        val tag = audioFile.tagOrCreateAndSetDefault

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
                    devLog("No artwork data")
                }
            }
        } else {
            hasArtwork = false
            devLog("No artwork data")
        }

        setFieldsFromSong(song)
        devLog("Song data: $song")

        // Little easter egg :)
        val comment = tag.getFirst(FieldKey.COMMENT)
        if(comment == "SongTagger by MihajloNesic") {
            devLog("Song was tagged with SongTagger! :)")
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
                            val audioFile = AudioFileIO.read(songFile) as MP3File

                            // ID3v1
                            if(audioFile.hasID3v1Tag()) {
                                devLog("Removing ID3v1...")
                                audioFile.delete(audioFile.iD3v1Tag)
                            }

                            // ID3v2
                            if(audioFile.hasID3v2Tag()) {
                                devLog("Removing ID3v2...")
                                audioFile.delete(audioFile.iD3v2Tag)
                            }

                            audioFile.commit()

                            clearControls(true)
                            controlFields(true)
                            controlButtons(true)
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
        devLog("Saving ")
        when {
            songFile.extension == "mp3" -> {
                devLog("MP3...")

                setSongFromFields()
                devLog("New song data: $song")

                val audioFile = AudioFileIO.read(songFile) as MP3File

                // ID3v1
                if(audioFile.hasID3v1Tag()) {
                    devLog("Removing ID3v1...")
                    audioFile.delete(audioFile.iD3v1Tag)
                }

                val id3v1Tag = ID3v1Tag()

                if(!song.album.isNullOrBlank()) id3v1Tag.setField(FieldKey.ALBUM, song.album)
                if(!song.title.isNullOrBlank()) id3v1Tag.setField(FieldKey.TITLE, song.title)
                if(!song.artist.isNullOrBlank()) id3v1Tag.setField(FieldKey.ARTIST, song.artist)
                if(!song.trackNumber.isNullOrBlank()) id3v1Tag.setField(FieldKey.TRACK, song.trackNumber)
                if(!song.year.isNullOrBlank()) id3v1Tag.setField(FieldKey.YEAR, song.year)
                if(!song.genre.isNullOrBlank()) id3v1Tag.setField(FieldKey.GENRE, song.genre)

                id3v1Tag.setField(FieldKey.COMMENT, "SongTagger by MihajloNesic")

                audioFile.iD3v1Tag = id3v1Tag
                devLog("Setting ID3v1")

                // ID3v2
                if(audioFile.hasID3v2Tag()) {
                    devLog("Removing ID3v2...")
                    audioFile.delete(audioFile.iD3v2Tag)
                }

                val id3v2Tag = audioFile.tagOrCreateDefault

                if(!song.album.isNullOrBlank()) id3v2Tag.setField(FieldKey.ALBUM, song.album)
                if(!song.title.isNullOrBlank()) id3v2Tag.setField(FieldKey.TITLE, song.title)
                if(!song.artist.isNullOrBlank()) id3v2Tag.setField(FieldKey.ARTIST, song.artist)
                if(!song.trackNumber.isNullOrBlank()) id3v2Tag.setField(FieldKey.TRACK, song.trackNumber)
                if(!song.year.isNullOrBlank()) id3v2Tag.setField(FieldKey.YEAR, song.year)
                if(!song.genre.isNullOrBlank()) id3v2Tag.setField(FieldKey.GENRE, song.genre)

                id3v2Tag.setField(FieldKey.COMMENT, "SongTagger by MihajloNesic")

                if(hasArtwork) {
                    id3v2Tag.deleteArtworkField()
                    id3v2Tag.setField(songArtArtwork)
                }

                audioFile.tag = id3v2Tag
                devLog("Setting ID3v1")

                devLog("Committing...")
                audioFile.commit()

                devLog("Saved!")
                Util.alertConfirm("Song successfully saved!")
            }
            songFile.extension == "m4a" -> {
                devLog("M4A...")

                val audioFile = AudioFileIO.read(songFile)
                val tag = audioFile.tag

                setSongFromFields()
                devLog("New song data: $song")

                if(!song.album.isNullOrBlank()) tag.setField(FieldKey.ALBUM, song.album)
                if(!song.title.isNullOrBlank()) tag.setField(FieldKey.TITLE, song.title)
                if(!song.artist.isNullOrBlank()) tag.setField(FieldKey.ARTIST, song.artist)
                if(!song.trackNumber.isNullOrBlank()) tag.setField(FieldKey.TRACK, song.trackNumber)
                if(!song.year.isNullOrBlank()) tag.setField(FieldKey.YEAR, song.year)
                if(!song.genre.isNullOrBlank()) tag.setField(FieldKey.GENRE, song.genre)

                tag.setField(FieldKey.COMMENT, "SongTagger by MihajloNesic")

                if(hasArtwork) {
                    tag.deleteArtworkField()
                    tag.setField(songArtArtwork)
                }

                audioFile.commit()

                devLog("Saved!")
                Util.alertConfirm("Song successfully saved!")
            }
            else -> Util.alertError("File extension not supported yet :(")
        }
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

    /**
     * Developer option for debugging
     * If the DEV variable is true, the software will output a log
     *
     * @param message Message to print to the standard output stream
     */
    private fun devLog(message: String) {
        if(DEV) {
            val timeFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS")
            val time = timeFormat.format(System.currentTimeMillis())
            println("[$time] $message")
        }
    }
}