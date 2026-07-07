package com.ollamaconnect.store

import androidx.compose.runtime.Composable
import com.ollamaconnect.LocalStorage
import com.ollamaconnect.models.ModelPreset
import com.ollamaconnect.generateUUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import ollama_connect.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class ModelPresetStore(private val storage: LocalStorage) {

    var customPresets: List<ModelPreset> = emptyList()
        private set

    val allPresets: List<ModelPreset>
        get() = builtInPresets + customPresets

    fun preset(id: String): ModelPreset? {
        return allPresets.firstOrNull { it.id == id }
    }

    private val folderName = "Presets"
    private val fileName = "$folderName/presets.json"

    init {
        loadFromDisk()
    }

    // MARK: - CRUD

    fun addCustomPreset(preset: ModelPreset) {
        val p = preset.copy(
            isBuiltIn = false,
            id = if (preset.id.isEmpty()) generateUUID() else preset.id
        )
        customPresets = customPresets + p
        saveToDisk()
    }

    fun updateCustomPreset(preset: ModelPreset) {
        if (preset.isBuiltIn) return
        customPresets = customPresets.map {
            if (it.id == preset.id) preset else it
        }
        saveToDisk()
    }

    fun deleteCustomPreset(id: String) {
        customPresets = customPresets.filterNot { it.id == id }
        saveToDisk()
    }

    // MARK: - Disk I/O

    private fun loadFromDisk() {
        val text = storage.readText(fileName)
        if (text != null) {
            try {
                val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<List<ModelPreset>>(text)
                customPresets = decoded.map { it.copy(isBuiltIn = false) }
            } catch (e: Exception) {
                customPresets = emptyList()
            }
        } else {
            customPresets = emptyList()
        }
    }

    private fun saveToDisk() {
        try {
            val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
            val text = json.encodeToString(customPresets)
            storage.writeText(fileName, text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val builtInPresets: List<ModelPreset> = listOf(
            // --- Gemma 4 ---
            ModelPreset(
                id = "builtin.gemma4.default",
                name = "Gemma 4 – Standard",
                summary = "Offizielle Empfehlung (Unsloth/Google): ausgewogen.",
                temperature = 1.0,
                topK = 64,
                topP = 0.95,
                minP = 0.0,
                presencePenalty = 0.0,
                isBuiltIn = true
            ),
            ModelPreset(
                id = "builtin.gemma4.ollama",
                name = "Gemma 4 – Ollama",
                summary = "Von Unsloth speziell für Ollama empfohlen (niedrige Temp).",
                temperature = 0.1,
                topK = 64,
                topP = 0.95,
                minP = 0.0,
                presencePenalty = 0.0,
                isBuiltIn = true
            ),

            // --- Qwen3.6 ---
            ModelPreset(
                id = "builtin.qwen36.thinking.general",
                name = "Qwen3.6 – Thinking, allgemein",
                summary = "Denkmodus für offene Aufgaben.",
                temperature = 1.0,
                topK = 20,
                topP = 0.95,
                minP = 0.0,
                presencePenalty = 1.5,
                isBuiltIn = true
            ),
            ModelPreset(
                id = "builtin.qwen36.thinking.coding",
                name = "Qwen3.6 – Thinking, Coding",
                summary = "Denkmodus, präzise für Code & WebDev.",
                temperature = 0.6,
                topK = 20,
                topP = 0.95,
                minP = 0.0,
                presencePenalty = 0.0,
                isBuiltIn = true
            ),
            ModelPreset(
                id = "builtin.qwen36.instruct.general",
                name = "Qwen3.6 – Instruct, allgemein",
                summary = "Nicht-denkender Modus, klassische Antworten.",
                temperature = 0.7,
                topK = 20,
                topP = 0.80,
                minP = 0.0,
                presencePenalty = 1.5,
                isBuiltIn = true
            ),
            ModelPreset(
                id = "builtin.qwen36.instruct.reasoning",
                name = "Qwen3.6 – Instruct, Reasoning",
                summary = "Nicht-denkender Modus, optimiert für Logik.",
                temperature = 1.0,
                topK = 20,
                topP = 0.95,
                minP = 0.0,
                presencePenalty = 1.5,
                isBuiltIn = true
            ),

            // --- Klassische Universal-Presets ---
            ModelPreset(
                id = "builtin.universal.precise",
                name = "Universal – Präzise",
                summary = "Niedrige Temp für Fakten & Code.",
                temperature = 0.2,
                topK = 40,
                topP = 0.9,
                minP = 0.0,
                presencePenalty = 0.0,
                isBuiltIn = true
            ),
            ModelPreset(
                id = "builtin.universal.balanced",
                name = "Universal – Ausgewogen",
                summary = "App-Default. Sicherer Allrounder.",
                temperature = 0.7,
                topK = 40,
                topP = 0.9,
                minP = 0.0,
                presencePenalty = 0.0,
                isBuiltIn = true
            ),
            ModelPreset(
                id = "builtin.universal.creative",
                name = "Universal – Kreativ",
                summary = "Hohe Temp für Schreiben & Brainstorming.",
                temperature = 1.2,
                topK = 80,
                topP = 0.95,
                minP = 0.5,
                presencePenalty = 0.0,
                isBuiltIn = true
            )
        )

        val nameKeys: Map<String, StringResource> = mapOf(
            "builtin.gemma4.default" to Res.string.preset_gemma4_default_name,
            "builtin.gemma4.ollama" to Res.string.preset_gemma4_ollama_name,
            "builtin.qwen36.thinking.general" to Res.string.preset_qwen36_thinking_general_name,
            "builtin.qwen36.thinking.coding" to Res.string.preset_qwen36_thinking_coding_name,
            "builtin.qwen36.instruct.general" to Res.string.preset_qwen36_instruct_general_name,
            "builtin.qwen36.instruct.reasoning" to Res.string.preset_qwen36_instruct_reasoning_name,
            "builtin.universal.precise" to Res.string.preset_universal_precise_name,
            "builtin.universal.balanced" to Res.string.preset_universal_balanced_name,
            "builtin.universal.creative" to Res.string.preset_universal_creative_name
        )

        val summaryKeys: Map<String, StringResource> = mapOf(
            "builtin.gemma4.default" to Res.string.preset_gemma4_default_summary,
            "builtin.gemma4.ollama" to Res.string.preset_gemma4_ollama_summary,
            "builtin.qwen36.thinking.general" to Res.string.preset_qwen36_thinking_general_summary,
            "builtin.qwen36.thinking.coding" to Res.string.preset_qwen36_thinking_coding_summary,
            "builtin.qwen36.instruct.general" to Res.string.preset_qwen36_instruct_general_summary,
            "builtin.qwen36.instruct.reasoning" to Res.string.preset_qwen36_instruct_reasoning_summary,
            "builtin.universal.precise" to Res.string.preset_universal_precise_summary,
            "builtin.universal.balanced" to Res.string.preset_universal_balanced_summary,
            "builtin.universal.creative" to Res.string.preset_universal_creative_summary
        )
    }
}

@Composable
fun localizedPresetName(preset: ModelPreset): String {
    val key = ModelPresetStore.nameKeys[preset.id]
    return if (preset.isBuiltIn && key != null) stringResource(key) else preset.name
}

@Composable
fun localizedPresetSummary(preset: ModelPreset): String {
    val key = ModelPresetStore.summaryKeys[preset.id]
    return if (preset.isBuiltIn && key != null) stringResource(key) else preset.summary
}
