package io.gitlab.mihajlonesic.songtagger

import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.roundToInt


class Util {
    companion object {

        /**
         * Constructs an `Alert` object based on parameters
         * Header text is `null` by default
         * Width os `220.0` by default
         *
         * @param type Type of alert
         * @param alertTitle Title
         * @param message Message
         * @return Alert JavaFX object
         * @see javafx.scene.control.Alert
         */
        private fun alert(type: Alert.AlertType, alertTitle: String, message: String): Alert {
            val iconImage = Image(SongTagger::class.java.getResourceAsStream("/icon.png"))
            return Alert(type).apply {
                headerText = null
                title = alertTitle
                contentText = message
                width = 220.0
                buttonTypes.clear()
                buttonTypes.add(ButtonType.OK)
                val window = dialogPane.scene.window as Stage
                window.icons.add(iconImage)
            }
        }

        /**
         * Shows `ERROR` message
         *
         * @param message Message
         * @see alert
         */
        fun alertError(message: String) {
            alert(Alert.AlertType.ERROR, "Error", message).showAndWait()
        }

        /**
         * Shows `ERROR` message with Exception stack trace
         *
         * @param message Message
         * @param exception Exception thrown
         * @see alert
         */
        fun alertException(message: String, exception: Exception) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            exception.printStackTrace(printWriter)
            val exceptionText = stringWriter.toString()

            val alert = alert(Alert.AlertType.ERROR, "Error", message)

            val label = Label("The exception stacktrace (send me this!)")

            val textArea = TextArea(exceptionText)
            textArea.isEditable = false
            textArea.isWrapText = true

            GridPane.setVgrow(textArea, Priority.ALWAYS)
            GridPane.setHgrow(textArea, Priority.ALWAYS)

            val expContent = GridPane()
            expContent.add(label, 0, 0)
            expContent.add(textArea, 0, 1)

            alert.dialogPane.expandableContent = expContent

            alert.showAndWait()
        }

        /**
         * Shows `INFORMATION` message and provides a 'Open Folder' button
         *
         * @param message Message
         * @param file A file in which location a directory will open
         * @see alert
         */
        fun alertOpen(message: String, file: File) {
            val alert = alert(Alert.AlertType.INFORMATION, "Information", message)
            alert.buttonTypes.clear()
            alert.buttonTypes.add(ButtonType.CLOSE)
            alert.buttonTypes.add(ButtonType.OK)
            alert.buttonTypes.add(ButtonType("Open Folder", ButtonBar.ButtonData.YES))

            val out = alert.showAndWait()

            if (out.isPresent) {
                if (out.get().buttonData == ButtonBar.ButtonData.YES) {
                    Desktop.getDesktop().open(File(file.parent))
                }
            }
        }

        /**
         * Shows `CONFIRMATION` message
         *
         * @param message Message
         * @see alert
         */
        fun alertConfirm(message: String) {
            alert(Alert.AlertType.CONFIRMATION, "Confirmation", message).showAndWait()
        }

        /**
         * Extracts an image to a file
         *
         * @param image Image to be extracted/saved
         * @param outputFile Image file
         */
        fun saveImageToFile(image: Image, outputFile: File) {
            val bImage = SwingFXUtils.fromFXImage(image, null)
            try {
                ImageIO.write(bImage, outputFile.extension, outputFile)
            } catch (e: IOException) {
                throw IOException(e)
            }
        }

        /**
         * Converts a color to HEX
         *
         * @param color Color
         * @return Hexadecimal value of the color
         */
        fun toHEX(color: Color): String {
            return String.format("#%02X%02X%02X",
                (color.red * 255).roundToInt(),
                (color.green * 255).roundToInt(),
                (color.blue * 255).roundToInt()
            )
        }

        /**
         * Generates a random HEX color
         */
        fun randomColor(): String {
            val obj = Random()
            val randNum = obj.nextInt(0xffffff + 1)
            return String.format("#%06x", randNum)
        }
    }
}