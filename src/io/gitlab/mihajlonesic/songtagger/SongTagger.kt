package io.gitlab.mihajlonesic.songtagger

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.image.Image
import javafx.scene.Parent

/**
 * @author Mihajlo Nesic
 * @since March 2019
 */

class SongTagger: Application() {
    private lateinit var controller: Controller

    override fun start(stage: Stage) {
        controller = Controller(stage)
        startApp(stage)
    }

    private fun startApp(stage: Stage) {
        var fxmlLoader = FXMLLoader(javaClass.getResource("/SongTagger.fxml"))
        fxmlLoader.setController(controller)
        var root = fxmlLoader.load<Any>() as Parent

        // val applicationIcon = Image(javaClass.getResourceAsStream("kotlin.png"))

        controller.init()

        stage.apply {
            title = "SongTagger alpha 2.0"
            scene = Scene(root)
            isResizable = false
            // icons.add(applicationIcon)
            scene.stylesheets.add("style.css")
            show()
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(SongTagger::class.java, *args)
}