@file:Suppress("unused", "SpellCheckingInspection", "NOTHING_TO_INLINE")

package ca.warp7.planner2.fx

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox


fun <T : Node> T.styleClass(sc: String): T {
    styleClass.add(sc)
    properties["FXKtStyleClass"] = sc
    return this
}


fun Node.noStyleClass() {
    styleClass.remove(properties["FXKtStyleClass"] ?: "")
}


fun HBox.align(pos: Pos) {
    alignment = pos
}


fun VBox.align(pos: Pos) {
    alignment = pos
}


fun Region.height(height: Double) {
    prefHeight = height
}


fun Region.width(width: Double) {
    prefWidth = width
}