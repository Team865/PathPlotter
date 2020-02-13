package ca.warp7.pathplotter

import javafx.application.Application
import javafx.application.HostServices
import javafx.stage.Stage

class PPApplication : Application() {
    override fun start(primaryStage: Stage) {
        host = hostServices
        PathPlotter().show()
    }

    companion object {
        var host: HostServices? = null
    }
}

fun main() {
    Application.launch(PPApplication::class.java)
}