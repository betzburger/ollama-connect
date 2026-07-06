package com.ollamaconnect

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ollama Connect",
        icon = painterResource("icon.png")
    ) {
        App(storage = getPlatformStorage(null))
    }
}
