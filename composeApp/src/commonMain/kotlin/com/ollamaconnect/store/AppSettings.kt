package com.ollamaconnect.store

import com.ollamaconnect.LocalStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppSettings(private val storage: LocalStorage) {
    private val fileName = "settings.json"
    private var data: MutableMap<String, String> = mutableMapOf()

    init {
        load()
    }

    private fun load() {
        val text = storage.readText(fileName)
        if (text != null) {
            try {
                data = Json.decodeFromString<Map<String, String>>(text).toMutableMap()
            } catch (e: Exception) {
                data = mutableMapOf()
            }
        }
    }

    private fun save() {
        try {
            val text = Json.encodeToString(data as Map<String, String>)
            storage.writeText(fileName, text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getString(key: String, default: String): String {
        return data[key] ?: default
    }

    fun setString(key: String, value: String) {
        data[key] = value
        save()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return data[key]?.toBooleanStrictOrNull() ?: default
    }

    fun setBoolean(key: String, value: Boolean) {
        data[key] = value.toString()
        save()
    }

    fun getDouble(key: String, default: Double): Double {
        return data[key]?.toDoubleOrNull() ?: default
    }

    fun setDouble(key: String, value: Double) {
        data[key] = value.toString()
        save()
    }

    fun getInt(key: String, default: Int): Int {
        return data[key]?.toIntOrNull() ?: default
    }

    fun setInt(key: String, value: Int) {
        data[key] = value.toString()
        save()
    }
}
