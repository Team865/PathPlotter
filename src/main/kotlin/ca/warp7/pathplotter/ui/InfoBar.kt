package ca.warp7.pathplotter.ui

import ca.warp7.pathplotter.util.f2
import edu.wpi.first.networktables.ConnectionNotification
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign.*

class InfoBar  {
    private fun label(icon: Ikon): Label {
        val label = Label()
        label.graphic = FontIcon.of(icon, 15, Color.valueOf("#5a8ade"))
        label.style = "-fx-font-weight:bold"
        return label
    }

    private val time = label(MDI_TIMER)
    private val dist = label(MDI_RULER)
    private val curve = label(MDI_VECTOR_CURVE)
    private val vel = label(MDI_SPEEDOMETER)
    private val compute = label(MDI_LAPTOP)

    private val red = Background(BackgroundFill(Color.RED, CornerRadii(6.0), null ))
    private val green = Background(BackgroundFill(Color.GREEN, CornerRadii(6.0), null ))

    private val robotStatus = Pane().apply {
        prefWidth = 12.0
        prefHeight = 12.0
        maxHeight = 12.0
        background = red
    }

    private val robotLabel = Label("Robot Disconnected")

    private val robotStatusCont = HBox(robotStatus, robotLabel).apply {
        this.spacing = 4.0
        alignment = Pos.CENTER
    }

    val container = HBox()

    init {
        setTime(0.0, 3.5)
        setDist(0.0, 2.1)
        setVel(0.0, 0.0, 0.0, 0.0)
        setCurve(0.1, 0.1, 3.5)
        container.children.addAll(time, dist, vel, curve, compute, robotStatusCont)
        container.spacing = 12.0
        container.padding = Insets(4.0, 12.0, 4.0, 12.0)
        container.style = "-fx-background-color: #eee"
    }

    fun setTime(current: Double, total: Double) {
        time.text = "${current.f2}/${total.f2}s"
    }

    fun setComputeTime(time: Double) {
        compute.text = "${time.f2}ms"
    }

    fun setDist(current: Double, total: Double) {
        dist.text = "${current.f2}/${total.f2}m"
    }

    fun setCurve(k: Double, dk: Double, sdk2: Double) {
        curve.text = "κ=${k.f2}   δκ=${dk.f2}   Σδκ²=${sdk2.f2}"
    }

    fun setVel(v: Double, w: Double, dv: Double, dw: Double) {
        vel.text = "v=${v.f2}   ω=${w.f2}   δv/δt=${dv.f2}   δω/δt=${dw.f2}"
    }

    fun setConnection(conn: ConnectionNotification) {
        if (conn.connected) {
            robotStatus.background = green
            robotLabel.text = "${conn.conn.remote_ip}@${conn.conn.remote_port}"
        } else {
            robotStatus.background = red
            robotLabel.text = "Robot Disconnected"
        }
    }
}