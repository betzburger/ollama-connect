package com.ollamaconnect

import java.io.File

class DesktopStorage : LocalStorage {
    private val baseDir = File(System.getProperty("user.home"), ".ollama-connect").apply {
        mkdirs()
    }

    override fun getBaseDir(): String {
        return baseDir.absolutePath
    }

    override fun writeText(fileName: String, content: String) {
        val file = File(baseDir, fileName)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }

    override fun readText(fileName: String): String? {
        val file = File(baseDir, fileName)
        return if (file.exists()) {
            file.readText(Charsets.UTF_8)
        } else {
            null
        }
    }

    override fun exists(fileName: String): Boolean {
        return File(baseDir, fileName).exists()
    }

    override fun delete(fileName: String): Boolean {
        val file = File(baseDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    override fun listFiles(dirName: String): List<String> {
        val dir = File(baseDir, dirName)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.list()?.toList() ?: emptyList()
    }
}

actual fun getPlatformStorage(context: Any?): LocalStorage {
    return DesktopStorage()
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
    try {
        val selection = java.awt.datatransfer.StringSelection(text)
        java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
