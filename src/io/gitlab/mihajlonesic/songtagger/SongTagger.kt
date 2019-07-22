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
        val fxmlLoader = FXMLLoader(javaClass.getResource("/SongTagger.fxml"))
        fxmlLoader.setController(controller)

        val root = fxmlLoader.load<Any>() as Parent

        val applicationIcon = Image(javaClass.getResourceAsStream("/icon.png"))

        controller.init()

        stage.apply {
            title = "SongTagger"
            scene = Scene(root)
            isResizable = false
            icons.add(applicationIcon)
            scene.stylesheets.add("style.css")
            show()
        }
    }
}

fun main(args: Array<String>) {
    print(  "  ____                   _____                           \n" +
            " / ___|  ___  _ __   __ |_   _|_ _  __ _  __ _  ___ _ __ \n" +
            " \\___ \\ / _ \\| '_ \\ / _` || |/ _` |/ _` |/ _` |/ _ \\ '__|\n" +
            "  ___) | (_) | | | | (_| || | (_| | (_| | (_| |  __/ |   \n" +
            " |____/ \\___/|_| |_|\\__, ||_|\\__,_|\\__, |\\__, |\\___|_|   \n" +
            " by Mihajlo Nesic   |___/          |___/ |___/           \n" +
            "\n")
    Application.launch(SongTagger::class.java, *args)
}