package com.ollamaconnect.service

import com.ollamaconnect.models.ChatMessage
import com.ollamaconnect.models.OllamaModelInfo
import com.ollamaconnect.models.OllamaOptions
import com.ollamaconnect.models.TokenUsage
import kotlinx.coroutines.flow.Flow

/** Events a chat backend emits while streaming a response. */
sealed class ChatStreamEvent {
    data class Text(val content: String) : ChatStreamEvent()
    data class Usage(val usage: TokenUsage) : ChatStreamEvent()
}

interface ChatService {
    suspend fun fetchModels(): List<OllamaModelInfo>
    fun streamChat(
        model: String,
        messages: List<ChatMessage>,
        options: OllamaOptions?
    ): Flow<ChatStreamEvent>
}
