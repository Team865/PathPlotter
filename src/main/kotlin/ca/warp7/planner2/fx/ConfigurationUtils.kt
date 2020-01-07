@file:Suppress("unused")

package ca.warp7.planner2.fx

import javafx.scene.layout.HBox
import javafx.scene.layout.VBox


inline fun hbox(builder: HBox.() -> Unit): HBox = HBox().apply(builder)

inline fun vbox(builder: VBox.() -> Unit): VBox = VBox().apply(builder)