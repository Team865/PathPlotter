@file:Suppress("unused")

package ca.warp7.planner2.fx

import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox


fun Pane.add(node: Node) {
    children.add(node)
}


fun Pane.addAll(vararg node: Node) {
    children.addAll(node)
}


fun HBox.hspace() = add(hbox {
    HBox.setHgrow(this, Priority.ALWAYS)
})


fun VBox.vspace() = add(vbox {
    VBox.setVgrow(this, Priority.ALWAYS)
})


fun <T : Node> T.hgrow(): T {
    HBox.setHgrow(this, Priority.ALWAYS)
    return this
}


fun <T : Node> T.vgrow(): T {
    VBox.setVgrow(this, Priority.ALWAYS)
    return this
}


fun SplitPane.addFixed(vararg node: Node) {
    node.forEach { SplitPane.setResizableWithParent(it, false) }
    items.addAll(node)
}