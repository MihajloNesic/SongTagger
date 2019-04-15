package io.gitlab.mihajlonesic.songtagger

import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.scene.paint.Color
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.roundToInt

class Util {
    companion object {

        /**
         * Constructs an `Alert` object based on parameters and displays it
         * Header text is `null` by default
         * Width os `220.0` by default
         *
         * @param type Type of alert
         * @param alertTitle Title
         * @param message Message
         */
        private fun alert(type: Alert.AlertType, alertTitle: String, message: String) {
            Alert(type).apply {
                headerText = null
                title = alertTitle
                contentText = message
                width = 220.0
                buttonTypes.clear()
                buttonTypes.add(ButtonType.OK)
                showAndWait()
            }
        }

        /**
         * Shows `ERROR` message
         *
         * @param message Message
         * @see alert
         */
        fun alertError(message: String) {
            alert(Alert.AlertType.ERROR, "Error", message)
        }

        /**
         * Shows `CONFIRMATION` message
         *
         * @param message Message
         * @see alert
         */
        fun alertConfirm(message: String) {
            alert(Alert.AlertType.CONFIRMATION, "Confirmation", message)
        }

        /**
         * Shows `INFORMATION` message
         *
         * @param message Message
         * @see alert
         */
        fun alertInfo(message: String) {
            alert(Alert.AlertType.INFORMATION, "Information", message)
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
    }
}