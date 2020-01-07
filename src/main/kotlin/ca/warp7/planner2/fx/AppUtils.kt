package ca.warp7.planner2.fx

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyCombination.*

fun runOnFxThread(action: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        action()
    } else {
        Platform.runLater(action)
    }
}

fun label(text: String) = Label(text)


fun <T> List<T>.observable(): ObservableList<T> {
    return FXCollections.observableList(this)
}

fun combo(
        keyCode: KeyCode,
        control: Boolean = false,
        alt: Boolean = false,
        shift: Boolean = false
): KeyCodeCombination {
    val args = ArrayList<KeyCombination.Modifier>()
    if (control) args.add(SHORTCUT_DOWN)
    if (alt) args.add(ALT_DOWN)
    if (shift) args.add(SHIFT_DOWN)
    return KeyCodeCombination(keyCode, *args.toTypedArray())
}