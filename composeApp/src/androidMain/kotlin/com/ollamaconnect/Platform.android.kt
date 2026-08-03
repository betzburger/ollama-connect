package com.ollamaconnect

import android.content.Context
import java.io.File

class AndroidStorage(private val context: Context) : LocalStorage {
    override fun getBaseDir(): String {
        return context.filesDir.absolutePath
    }

    override fun writeText(fileName: String, content: String) {
        val file = File(context.filesDir, fileName)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }

    override fun readText(fileName: String): String? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.readText(Charsets.UTF_8)
        } else {
            null
        }
    }

    override fun exists(fileName: String): Boolean {
        return File(context.filesDir, fileName).exists()
    }

    override fun delete(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    override fun listFiles(dirName: String): List<String> {
        val dir = File(context.filesDir, dirName)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.list()?.toList() ?: emptyList()
    }
}

actual fun getPlatformStorage(context: Any?): LocalStorage {
    return AndroidStorage(context as Context)
}

actual fun generateUUID(): String {
    return java.util.UUID.randomUUID().toString()
}

actual fun formatMemoryDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMAN)
    return sdf.format(date)
}

actual fun formatDateTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMAN)
    return sdf.format(date)
}

actual fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.GERMAN)
    return sdf.format(date)
}

actual fun formatShortDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val sdf = java.text.SimpleDateFormat("dd. MMM, HH:mm", java.util.Locale.GERMAN)
    return sdf.format(date)
}

actual fun formatTokenCount(value: Int): String {
    return java.text.NumberFormat.getIntegerInstance().format(value)
}

actual fun shareText(text: String, context: Any?) {
    val ctx = context as Context
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(android.content.Intent.createChooser(intent, "Chat teilen"))
}
