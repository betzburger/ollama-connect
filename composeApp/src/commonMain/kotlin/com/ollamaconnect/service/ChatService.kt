package com.ollamaconnect.service

import com.ollamaconnect.models.ChatMessage
import com.ollamaconnect.models.OllamaModelInfo
import com.ollamaconnect.models.OllamaOptions
import kotlinx.coroutines.flow.Flow

interface ChatService {
    suspend fun fetchModels(): List<OllamaModelInfo>
    fun streamChat(
        model: String,
        messages: List<ChatMessage>,
        options: OllamaOptions?
    ): Flow<String>
}
