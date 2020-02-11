package ca.warp7.planner2.ui

import ca.warp7.planner2.util.f2
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.HBox
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
    private val nt = label(MDI_ACCESS_POINT)

    val container = HBox()

    init {
        setTime(0.0, 3.5)
        setDist(0.0, 2.1)
        setVel(0.0, 0.0, 0.0, 0.0)
        setCurve(0.1, 0.1, 3.5)
        nt.text = "Connected"
        container.children.addAll(time, dist, vel, curve, nt)
        container.spacing = 12.0
        container.padding = Insets(4.0, 12.0, 4.0, 12.0)
        container.style = "-fx-background-color: #eee"
    }

    fun setTime(current: Double, total: Double) {
        time.text = "${current.f2}/${total.f2}s"
    }

    fun setDist(current: Double, total: Double) {
        dist.text = "${current.f2}/${total.f2}m"
    }

    fun setCurve(k: Double, dk: Double, sdk2: Double) {
        curve.text = "κ=${k.f2}   δκ=${dk.f2}   Σδκ²=${sdk2.f2}"
    }

    fun setVel(v: Double, w: Double, dv: Double, dw: Double) {
        vel.text = "v=${v.f2}m/s   ω=${w.f2}rad/s   δv/δt=${dv.f2}m/s²   δω/δt=${dw.f2}rad/s²"
    }
}