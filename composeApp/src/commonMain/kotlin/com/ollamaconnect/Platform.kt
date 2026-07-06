package com.ollamaconnect

interface LocalStorage {
    fun getBaseDir(): String
    fun writeText(fileName: String, content: String)
    fun readText(fileName: String): String?
    fun exists(fileName: String): Boolean
    fun delete(fileName: String): Boolean
    fun listFiles(dirName: String): List<String>
}

expect fun getPlatformStorage(context: Any?): LocalStorage

expect fun generateUUID(): String

expect fun formatMemoryDate(timestamp: Long): String

expect fun formatDateTime(timestamp: Long): String

expect fun formatTime(timestamp: Long): String

expect fun formatShortDate(timestamp: Long): String

expect fun shareText(text: String, context: Any?)
