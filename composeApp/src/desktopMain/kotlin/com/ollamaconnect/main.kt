package com.ollamaconnect

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ollama Connect"
    ) {
        App(storage = getPlatformStorage(null))
    }
}
