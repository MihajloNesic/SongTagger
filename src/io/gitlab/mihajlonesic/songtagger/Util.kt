package io.gitlab.mihajlonesic.songtagger

import javafx.scene.control.Alert

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
    }
}