package com.ollamaconnect.store

import com.ollamaconnect.LocalStorage
import com.ollamaconnect.formatMemoryDate

class PersonaStore(
    private val storage: LocalStorage,
    private val settings: AppSettings
) {
    // MARK: - Editable text
    var config: String = ""
    var soul: String = ""
    var memory: String = ""

    // MARK: - Toggles (persisted in AppSettings)
    var configEnabled: Boolean
        get() = settings.getBoolean("oc_personaConfigEnabled", true)
        set(value) {
            settings.setBoolean("oc_personaConfigEnabled", value)
        }

    var soulEnabled: Boolean
        get() = settings.getBoolean("oc_personaSoulEnabled", true)
        set(value) {
            settings.setBoolean("oc_personaSoulEnabled", value)
        }

    var memoryEnabled: Boolean
        get() = settings.getBoolean("oc_personaMemoryEnabled", true)
        set(value) {
            settings.setBoolean("oc_personaMemoryEnabled", value)
        }

    var autoMemoryEnabled: Boolean
        get() = settings.getBoolean("oc_personaAutoMemoryEnabled", true)
        set(value) {
            settings.setBoolean("oc_personaAutoMemoryEnabled", value)
        }

    // MARK: - File locations
    private val folderName = "Assistant"
    private val configFileName = "$folderName/config.md"
    private val soulFileName = "$folderName/soul.md"
    private val memoryFileName = "$folderName/memory.md"

    // MARK: - Init
    init {
        seedDefaultsIfMissing()
        reloadFromDisk()
    }

    // MARK: - Disk I/O
    private fun seedDefaultsIfMissing() {
        if (!storage.exists(configFileName)) {
            storage.writeText(configFileName, defaultConfig)
        }
        if (!storage.exists(soulFileName)) {
            storage.writeText(soulFileName, defaultSoul)
        }
        if (!storage.exists(memoryFileName)) {
            storage.writeText(memoryFileName, defaultMemory)
        }
    }

    fun reloadFromDisk() {
        config = storage.readText(configFileName) ?: ""
        soul = storage.readText(soulFileName) ?: ""
        memory = storage.readText(memoryFileName) ?: ""
    }

    fun saveConfig() {
        storage.writeText(configFileName, config)
    }

    fun saveSoul() {
        storage.writeText(soulFileName, soul)
    }

    fun saveMemory() {
        storage.writeText(memoryFileName, memory)
    }

    fun saveAll() {
        saveConfig()
        saveSoul()
        saveMemory()
    }

    // MARK: - Reset to defaults
    fun resetConfigToDefault() {
        config = defaultConfig
        saveConfig()
    }

    fun resetSoulToDefault() {
        soul = defaultSoul
        saveSoul()
    }

    fun resetMemoryToDefault() {
        memory = defaultMemory
        saveMemory()
    }

    // MARK: - Composite system prompt
    fun composedSystemPrompt(): String {
        val sections = mutableListOf<String>()

        if (configEnabled) {
            val body = config.trim()
            if (body.isNotEmpty()) {
                sections.add("# Kontext & Rahmen\n$body")
            }
        }

        if (soulEnabled) {
            val body = soul.trim()
            if (body.isNotEmpty()) {
                sections.add("# Persönlichkeit & Stil\n$body")
            }
        }

        if (memoryEnabled) {
            val body = memory.trim()
            if (body.isNotEmpty()) {
                sections.add("# Gedächtnis\nDu erinnerst dich an die folgenden Fakten aus früheren Gesprächen:\n\n$body")
            }
        }

        if (autoMemoryEnabled) {
            sections.add(memoryToolInstruction)
        }

        return sections.joinToString("\n\n")
    }

    // MARK: - Memory extraction
    fun extractAndStoreMemories(text: String): String {
        if (!autoMemoryEnabled) return text

        var cleaned = text
        var foundAny = false

        val openTag = "<remember>"
        val closeTag = "</remember>"

        while (true) {
            val startIndex = cleaned.indexOf(openTag, ignoreCase = true)
            if (startIndex == -1) break
            val endIndex = cleaned.indexOf(closeTag, startIndex + openTag.length, ignoreCase = true)
            if (endIndex == -1) break

            val body = cleaned.substring(startIndex + openTag.length, endIndex).trim()
            if (body.isNotEmpty()) {
                appendMemoryEntry(body)
                foundAny = true
            }
            cleaned = cleaned.removeRange(startIndex, endIndex + closeTag.length)
        }

        if (foundAny) {
            saveMemory()
        }

        return cleaned.trim()
    }

    private fun appendMemoryEntry(body: String) {
        val stamp = formatMemoryDate(System.currentTimeMillis())
        val entry = "- $body _(notiert $stamp)_"

        val current = memory.trim()
        if (current.isEmpty()) {
            memory = "$memoryHeader\n\n$entry\n"
        } else {
            memory = "$current\n$entry\n"
        }
    }

    // MARK: - Defaults
    companion object {
        const val defaultConfig = """# Konfiguration

Hier definierst du, **wer der Assistent ist**, **wer du bist** und welche
Rahmenbedingungen für alle Chats gelten.

## Wer du bist (Assistent)
Du heißt **Mira**. Du bist ein hilfsbereiter, fundierter KI-Assistent,
der lokal auf einem privaten Server läuft.

## Wer ich bin (User)
Ich heiße _<dein Name>_. Schreibe hier kurz, was du tust und was dir
wichtig ist — Beruf, Interessen, Tools, mit denen du arbeitest.

## Rahmenbedingungen
- Antworte standardmäßig auf **Deutsch**.
- Halte dich an Fakten. Wenn du etwas nicht weißt, sage es.
- Bei Code: präzise, kommentierte Beispiele.
- Frage nach, wenn meine Anfrage mehrdeutig ist."""

        const val defaultSoul = """# Persönlichkeit

Hier definierst du den **Vibe** des Assistenten — Tonfall, Energie,
Sprachstil. Diese Eigenschaften prägen, _wie_ der Assistent klingt,
nicht _was_ er sagt.

## Tonfall
- Warm, aufmerksam, leicht trocken-humorvoll.
- Kein Marketing-Sprech, keine Floskeln, keine Schmeicheleien.
- Klar und direkt, ohne grob zu sein.

## Stil
- Lieber konkret als vage.
- Kurze Sätze sind okay. Listen, wenn sie helfen.
- Eigene Meinung, wenn danach gefragt wird.

## Anti-Muster
- Keine endlosen Einleitungen ("Das ist eine großartige Frage!").
- Keine Zusammenfassung am Ende, wenn die Antwort kurz war."""

        const val memoryHeader = "# Gedächtnis\n\nDieser Bereich wird automatisch ergänzt, wenn ich dich bitte, dir etwas zu merken. Du kannst hier auch manuell editieren."

        const val defaultMemory = "$memoryHeader\n\n- Beispiel: _Mein Lieblingseditor ist Xcode._"

        const val memoryToolInstruction = """# Wie du dir Dinge merkst

Wenn der User dich ausdrücklich bittet, etwas zu **merken** — oder du eine Information für so wichtig hältst, dass sie über diesen Chat hinaus relevant bleibt — schreibe sie in einen <remember>…</remember>-Block.

Beispiel:
<remember>Peter arbeitet hauptsächlich an iOS-Apps mit SwiftUI.</remember>

Regeln:
- Pro Fakt ein eigener <remember>-Block.
- Knapp formulieren, in der dritten Person ("Peter mag …").
- Nur Fakten, keine Vermutungen.
- Diese Blöcke werden aus deiner Antwort entfernt, bevor sie angezeigt werden — der User sieht sie also nicht. Bestätige kurz im normalen Text, was du dir gemerkt hast."""
    }
}
