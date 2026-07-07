package com.ollamaconnect.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ollamaconnect.LocalStorage
import com.ollamaconnect.models.*
import com.ollamaconnect.service.*
import com.ollamaconnect.store.AppSettings
import com.ollamaconnect.store.ModelPresetStore
import com.ollamaconnect.store.PersonaStore
import com.ollamaconnect.generateUUID
import io.ktor.client.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.cancellable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ChatViewModel(
    private val storage: LocalStorage,
    val settings: AppSettings,
    val personaStore: PersonaStore,
    val presetStore: ModelPresetStore
) : ViewModel() {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        // Local LLM inference (especially "thinking" models) can take far
        // longer than an HTTP client's default read timeout (e.g. OkHttp's
        // 10s) before the first byte of the response arrives.
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = 10_000
        }
    }

    // MARK: - Connection Settings

    var host by mutableStateOf(settings.getString("oc_host", ""))
    var serverKind by mutableStateOf(ServerKind.fromRaw(settings.getString("oc_serverKind", ServerKind.OLLAMA.rawValue)))
    var port by mutableStateOf(loadInitialPort())
    var savedHosts = mutableStateListOf<SavedHost>()

    private fun portKey(kind: ServerKind): String = "oc_port_${kind.rawValue}"

    private fun loadInitialPort(): String {
        val kind = ServerKind.fromRaw(settings.getString("oc_serverKind", ServerKind.OLLAMA.rawValue))
        val saved = settings.getString(portKey(kind), "")
        if (saved.isNotEmpty()) return saved
        val legacy = settings.getString("oc_port", "")
        if (legacy.isNotEmpty()) return legacy
        return kind.defaultPort.toString()
    }

    // MARK: - Model Selection

    var availableModels = mutableStateListOf<OllamaModelInfo>()
    var selectedModel by mutableStateOf("")

    // MARK: - Chat State

    val messages = mutableStateListOf<ChatMessage>()
    var inputText by mutableStateOf("")
    var systemPrompt by mutableStateOf(settings.getString("oc_systemPrompt", ""))

    // MARK: - Model Parameters

    var temperature by mutableStateOf(settings.getDouble("oc_temperature", 0.7))
    var topK by mutableStateOf(settings.getDouble("oc_topK", 40.0))
    var topP by mutableStateOf(settings.getDouble("oc_topP", 0.9))
    var minP by mutableStateOf(settings.getDouble("oc_minP", 0.0))
    var presencePenalty by mutableStateOf(settings.getDouble("oc_presencePenalty", 0.0))
    var contextMessageLimit by mutableStateOf(settings.getInt("oc_contextLimit", 0))
    var contextTokenLimit by mutableStateOf(settings.getInt("oc_contextTokens", 0))
    var activePresetID by mutableStateOf(settings.getString("oc_activePresetID", ""))

    // MARK: - Conversation Persistence

    val conversations = mutableStateListOf<Conversation>()
    var currentConversation by mutableStateOf<Conversation?>(null)

    // MARK: - UI State

    var isConnected by mutableStateOf(false)
    var isLoadingModels by mutableStateOf(false)
    var isGenerating by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var streamJob: Job? = null

    // MARK: - Computed

    val canSend: Boolean
        get() = inputText.trim().isNotEmpty() && isConnected && !isGenerating && selectedModel.isNotEmpty()

    val contextInfo: String?
        get() {
            return if (contextMessageLimit > 0 && messages.size > contextMessageLimit) {
                "Kontext: letzte $contextMessageLimit von ${messages.size} Nachrichten"
            } else {
                null
            }
        }

    val connectionService: ChatService?
        get() {
            val trimmedHost = host.trim()
            if (trimmedHost.isEmpty()) return null
            val portInt = port.toIntOrNull() ?: return null
            return when (serverKind) {
                ServerKind.OLLAMA -> OllamaService(trimmedHost, portInt, httpClient)
                ServerKind.LLAMA_SERVER -> LlamaServerService(trimmedHost, portInt, httpClient)
            }
        }

    init {
        loadSavedHosts()
        loadConversations()
    }

    fun setServerKindValue(kind: ServerKind) {
        settings.setString(portKey(serverKind), port)
        serverKind = kind
        settings.setString("oc_serverKind", kind.rawValue)

        val saved = settings.getString(portKey(kind), "")
        port = if (saved.isNotEmpty()) {
            saved
        } else {
            kind.defaultPort.toString()
        }
        settings.setString("oc_port", port)
    }

    fun savePortForCurrentKind() {
        settings.setString(portKey(serverKind), port)
        settings.setString("oc_port", port)
    }

    // MARK: - Connection

    fun connect() {
        val service = connectionService
        if (service == null) {
            errorMessage = "Bitte eine gültige IP-Adresse und einen Port eingeben."
            return
        }

        errorMessage = null
        isLoadingModels = true

        viewModelScope.launch {
            try {
                val models = service.fetchModels()
                availableModels.clear()
                availableModels.addAll(models.sortedBy { it.name })
                selectedModel = models.firstOrNull()?.name ?: ""
                isConnected = true
                saveCurrentHost()
            } catch (e: Exception) {
                isConnected = false
                errorMessage = e.message ?: "Unbekannter Fehler bei der Verbindung."
            } finally {
                isLoadingModels = false
            }
        }
    }

    fun disconnect() {
        stopGeneration()
        isConnected = false
        availableModels.clear()
        selectedModel = ""
        errorMessage = null
    }

    // MARK: - Conversation Management

    fun newChat() {
        stopGeneration()
        messages.clear()
        currentConversation = null
        errorMessage = null
        systemPrompt = settings.getString("oc_systemPrompt", "")
    }

    fun saveSystemPromptDefault() {
        settings.setString("oc_systemPrompt", systemPrompt)
    }

    fun saveModelParameterDefaults() {
        settings.setDouble("oc_temperature", temperature)
        settings.setDouble("oc_topK", topK)
        settings.setDouble("oc_topP", topP)
        settings.setDouble("oc_minP", minP)
        settings.setDouble("oc_presencePenalty", presencePenalty)
        settings.setInt("oc_contextLimit", contextMessageLimit)
        settings.setInt("oc_contextTokens", contextTokenLimit)
    }

    fun applyPreset(preset: ModelPreset) {
        temperature = preset.temperature
        topK = preset.topK.toDouble()
        topP = preset.topP
        minP = preset.minP
        presencePenalty = preset.presencePenalty
        preset.numCtx?.let { contextTokenLimit = it }
        activePresetID = preset.id
        settings.setString("oc_activePresetID", preset.id)
        saveModelParameterDefaults()
    }

    fun clearActivePreset() {
        activePresetID = ""
        settings.setString("oc_activePresetID", "")
    }

    fun loadConversation(conversation: Conversation) {
        stopGeneration()
        currentConversation = conversation
        messages.clear()
        messages.addAll(
            conversation.persistedMessages
                .sortedBy { it.sortOrder }
                .map { pm ->
                    ChatMessage(
                        id = pm.id,
                        role = pm.role,
                        content = pm.content,
                        timestamp = pm.timestamp
                    )
                }
        )
        errorMessage = null
        systemPrompt = conversation.systemPrompt

        if (conversation.modelName.isNotEmpty() && availableModels.any { it.name == conversation.modelName }) {
            selectedModel = conversation.modelName
        }
    }

    fun renameConversation(conversation: Conversation, newTitle: String) {
        conversation.title = newTitle.trim()
        saveConversations()
    }

    fun exportConversation(conversation: Conversation): String {
        val lines = mutableListOf<String>()
        lines.add("# ${conversation.title}")
        lines.add("Modell: ${conversation.modelName} | ${com.ollamaconnect.formatDateTime(conversation.createdAt)}")
        if (conversation.systemPrompt.isNotEmpty()) {
            lines.add("\n> System: ${conversation.systemPrompt}")
        }
        lines.add("")
        conversation.persistedMessages.sortedBy { it.sortOrder }.forEach { msg ->
            val role = if (msg.role == "user") "Du" else "Assistent"
            lines.add("**$role:**")
            lines.add(msg.content)
            lines.add("")
        }
        lines.add("---")
        lines.add("_Exportiert aus Ollama Connect (Kotlin)_")
        return lines.joinToString("\n")
    }

    fun deleteConversation(conversation: Conversation) {
        if (currentConversation?.id == conversation.id) {
            newChat()
        }
        conversations.removeAll { it.id == conversation.id }
        saveConversations()
    }

    // MARK: - Chat Action

    fun sendMessage() {
        val content = inputText.trim()
        val service = connectionService
        if (content.isEmpty() || selectedModel.isEmpty() || !isConnected || service == null) return

        inputText = ""
        messages.add(ChatMessage(role = MessageRole.USER.rawValue, content = content))
        messages.add(ChatMessage(role = MessageRole.ASSISTANT.rawValue, content = ""))
        val assistantIndex = messages.size - 1

        isGenerating = true
        errorMessage = null

        var history = messages.take(messages.size - 1).toList()
        if (contextMessageLimit > 0 && history.size > contextMessageLimit) {
            history = history.takeLast(contextMessageLimit)
        }

        val composedPrompt = buildEffectiveSystemPrompt()
        if (composedPrompt.isNotEmpty()) {
            history = listOf(ChatMessage(role = MessageRole.SYSTEM.rawValue, content = composedPrompt)) + history
        }

        val model = selectedModel
        val options = OllamaOptions(
            temperature = temperature,
            topK = topK.toInt(),
            topP = topP,
            minP = if (minP > 0.0) minP else null,
            presencePenalty = if (presencePenalty != 0.0) presencePenalty else null,
            numCtx = if (contextTokenLimit > 0) contextTokenLimit else null
        )

        streamJob = viewModelScope.launch {
            try {
                var rawBuffer = ""
                service.streamChat(model, history, options).cancellable().collect { chunk ->
                    rawBuffer += chunk
                    messages[assistantIndex] = messages[assistantIndex].copy(
                        content = rawBuffer.strippingHiddenBlocks()
                    )
                }
                finalizeAssistantMessage(assistantIndex, rawBuffer)
                persistMessages()
            } catch (e: Exception) {
                if (e is CancellationException || e.cause is CancellationException) {
                    // User stopped generation - save what we have
                    if (messages.size > assistantIndex && messages[assistantIndex].content.isNotEmpty()) {
                        persistMessages()
                    }
                } else {
                    errorMessage = e.message ?: "Generierungsfehler."
                    if (messages.size > assistantIndex && messages[assistantIndex].content.isEmpty()) {
                        messages.removeAt(assistantIndex)
                    }
                }
            } finally {
                isGenerating = false
            }
        }
    }

    private fun buildEffectiveSystemPrompt(): String {
        val parts = mutableListOf<String>()
        val personaBlock = personaStore.composedSystemPrompt()
        if (personaBlock.isNotEmpty()) {
            parts.add(personaBlock)
        }
        val perChat = systemPrompt.trim()
        if (perChat.isNotEmpty()) {
            parts.add("# Anweisungen für diesen Chat\n$perChat")
        }
        return parts.joinToString("\n\n")
    }

    private fun finalizeAssistantMessage(index: Int, rawBuffer: String) {
        if (messages.size <= index) return
        val withoutThinking = rawBuffer.strippingThinkingBlocksOnly()
        val cleaned = personaStore.extractAndStoreMemories(withoutThinking)
        messages[index] = messages[index].copy(content = cleaned)
    }

    fun stopGeneration() {
        streamJob?.cancel()
        streamJob = null
        isGenerating = false
    }

    // MARK: - Persistence Helpers

    private fun persistMessages() {
        if (currentConversation == null) {
            val title = generateTitle()
            val conv = Conversation(
                title = title,
                modelName = selectedModel,
                systemPrompt = systemPrompt
            )
            conversations.add(0, conv)
            currentConversation = conv
        }

        val conversation = currentConversation ?: return
        val existingCount = conversation.persistedMessages.size

        val newPersisted = conversation.persistedMessages.toMutableList()

        for (i in existingCount until messages.size) {
            val msg = messages[i]
            if (msg.role == MessageRole.ASSISTANT.rawValue && msg.content.isEmpty()) continue
            val pm = PersistedMessage(
                id = msg.id,
                role = msg.role,
                content = msg.content,
                sortOrder = i,
                timestamp = msg.timestamp,
                conversationId = conversation.id
            )
            newPersisted.add(pm)
        }

        // Update the last assistant message content if it was streaming and is updated
        if (newPersisted.isNotEmpty()) {
            val lastPM = newPersisted.last()
            val lastMsg = messages.lastOrNull()
            if (lastPM.role == MessageRole.ASSISTANT.rawValue && lastMsg != null && lastMsg.role == MessageRole.ASSISTANT.rawValue) {
                lastPM.content = lastMsg.content
            }
        }

        conversation.persistedMessages = newPersisted
        conversation.updatedAt = System.currentTimeMillis()
        conversation.modelName = selectedModel

        saveConversations()
    }

    private fun generateTitle(): String {
        val firstUserMsg = messages.firstOrNull { it.role == MessageRole.USER.rawValue }?.content ?: "Neuer Chat"
        val trimmed = firstUserMsg.trim()
        return if (trimmed.length <= 50) trimmed else trimmed.take(47) + "…"
    }

    private fun loadConversations() {
        val text = storage.readText("conversations.json")
        if (text != null) {
            try {
                val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<List<Conversation>>(text)
                conversations.clear()
                conversations.addAll(decoded.sortedByDescending { it.updatedAt })
            } catch (e: Exception) {
                conversations.clear()
            }
        }
    }

    private fun saveConversations() {
        try {
            val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
            val text = json.encodeToString(conversations.toList())
            storage.writeText("conversations.json", text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // MARK: - Host Management

    fun selectHost(saved: SavedHost) {
        host = saved.address
        port = saved.port.toString()
        if (saved.kind != serverKind) {
            serverKind = saved.kind
            settings.setString("oc_serverKind", saved.kind.rawValue)
        }
        settings.setString("oc_host", host)
        settings.setString("oc_port", port)
        settings.setString(portKey(serverKind), port)
    }

    fun removeHost(hostToRemove: SavedHost) {
        savedHosts.remove(hostToRemove)
        saveSavedHosts()
    }

    private fun saveCurrentHost() {
        val trimmedHost = host.trim()
        val portInt = port.toIntOrNull() ?: return
        if (trimmedHost.isEmpty()) return

        settings.setString("oc_host", trimmedHost)
        settings.setString("oc_port", port)
        settings.setString(portKey(serverKind), port)
        settings.setString("oc_serverKind", serverKind.rawValue)

        savedHosts.removeAll { it.address == trimmedHost && it.kind == serverKind }
        savedHosts.add(0, SavedHost(address = trimmedHost, port = portInt, kind = serverKind))

        if (savedHosts.size > 8) {
            val trimmedList = savedHosts.take(8)
            savedHosts.clear()
            savedHosts.addAll(trimmedList)
        }
        saveSavedHosts()
    }

    private fun loadSavedHosts() {
        val text = storage.readText("saved_hosts.json")
        if (text != null) {
            try {
                val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<List<SavedHost>>(text)
                savedHosts.clear()
                savedHosts.addAll(decoded)
            } catch (e: Exception) {
                savedHosts.clear()
            }
        }
    }

    private fun saveSavedHosts() {
        try {
            val text = Json.encodeToString(savedHosts.toList())
            storage.writeText("saved_hosts.json", text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// MARK: - String Extensions (stripping think/remember blocks)

private fun String.strippingHiddenBlocks(): String {
    return strippingBlocks(listOf("think", "remember"))
}

private fun String.strippingThinkingBlocksOnly(): String {
    return strippingBlocks(listOf("think"))
}

private fun String.strippingBlocks(tags: List<String>): String {
    var result = this

    for (tag in tags) {
        val openTag = "<$tag>"
        val closeTag = "</$tag>"

        var removedOne = true
        while (removedOne) {
            removedOne = false
            val startIdx = result.indexOf(openTag, ignoreCase = true)
            if (startIdx != -1) {
                val endIdx = result.indexOf(closeTag, startIdx + openTag.length, ignoreCase = true)
                if (endIdx != -1) {
                    result = result.removeRange(startIdx, endIdx + closeTag.length)
                    removedOne = true
                }
            }
        }

        // Trim an unclosed (still-streaming) opening tag from the end
        val startIdx = result.indexOf(openTag, ignoreCase = true)
        if (startIdx != -1) {
            result = result.substring(0, startIdx)
        }
    }

    return result.trim()
}
