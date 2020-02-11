package ca.warp7.planner2

import javafx.application.Application
import javafx.application.HostServices
import javafx.stage.Stage

class PlannerApplication : Application() {
    override fun start(primaryStage: Stage) {
        host = hostServices
        PathPlotter().show()
    }

    companion object {
        var host: HostServices? = null
    }
}

fun main() {
    Application.launch(PlannerApplication::class.java)
}