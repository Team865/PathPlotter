@file:Suppress("unused")

package ca.warp7.planner2.fx

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCombination
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon


inline fun menuBar(builder: MenuBar.() -> Unit): MenuBar = MenuBar().apply(builder)


fun MenuItem.name(name: String) {
    text = name
}


fun MenuItem.action(handler: (ActionEvent) -> Unit) {
    onAction = EventHandler(handler)
}


fun Menu.name(name: String) {
    text = name
}

fun menuItem(name: String, icon: Ikon?, combo: KeyCombination?, action: () -> Unit): MenuItem {
    val mi = MenuItem(name)
    if (combo != null) {
        mi.accelerator = combo
    }
    if (icon != null) {
        mi.graphic = FontIcon.of(icon, 15)
    }
    mi.setOnAction { action() }
    return mi
}