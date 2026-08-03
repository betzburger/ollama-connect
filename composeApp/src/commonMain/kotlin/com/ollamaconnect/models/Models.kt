package com.ollamaconnect.models

import com.ollamaconnect.generateUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MARK: - Server Kind

/** Which HTTP API dialect a backend speaks. */
enum class APIFlavor {
    OLLAMA,
    OPENAI
}

@Serializable
enum class ServerKind(
    val rawValue: String,
    val displayName: String,
    val defaultPort: Int,
    val apiFlavor: APIFlavor
) {
    @SerialName("ollama")
    OLLAMA("ollama", "Ollama", 11434, APIFlavor.OLLAMA),

    @SerialName("llama_server")
    LLAMA_SERVER("llama_server", "llama-server", 8080, APIFlavor.OPENAI),

    @SerialName("omlx")
    OMLX("omlx", "oMLX", 8000, APIFlavor.OPENAI),

    @SerialName("rapid_mlx")
    RAPID_MLX("rapid_mlx", "Rapid-MLX", 8000, APIFlavor.OPENAI);

    companion object {
        fun fromRaw(raw: String): ServerKind {
            return entries.firstOrNull { it.rawValue == raw } ?: OLLAMA
        }
    }
}

// MARK: - Saved Host

@Serializable
data class SavedHost(
    val id: String = generateUUID(),
    val address: String,
    val port: Int,
    val kind: ServerKind
)

// MARK: - Chat Message

enum class MessageRole(val rawValue: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    companion object {
        fun fromRaw(raw: String): MessageRole {
            return entries.firstOrNull { it.rawValue == raw } ?: USER
        }
    }
}

@Serializable
data class ChatMessage(
    val id: String = generateUUID(),
    val role: String, // String representation for serialization
    var content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val roleEnum: MessageRole
        get() = MessageRole.fromRaw(role)
}

// MARK: - Ollama API Models

@Serializable
data class OllamaTagsResponse(
    val models: List<OllamaModelInfo>
)

@Serializable
data class OllamaModelInfo(
    val name: String,
    val size: Long? = null,
    @SerialName("modified_at") val modifiedAt: String? = null
) {
    val id: String
        get() = name

    val displaySize: String
        get() {
            val s = size ?: return ""
            val gb = s.toDouble() / 1_000_000_000
            if (gb >= 1) {
                return "${formatDecimal(gb, 1)} GB"
            }
            val mb = s.toDouble() / 1_000_000
            return "${formatDecimal(mb, 0)} MB"
        }
}

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessagePayload>,
    val stream: Boolean,
    val options: OllamaOptions? = null
)

@Serializable
data class OllamaOptions(
    val temperature: Double? = null,
    @SerialName("top_k") val topK: Int? = null,
    @SerialName("top_p") val topP: Double? = null,
    @SerialName("min_p") val minP: Double? = null,
    @SerialName("presence_penalty") val presencePenalty: Double? = null,
    @SerialName("num_ctx") val numCtx: Int? = null
)

@Serializable
data class OllamaMessagePayload(
    val role: String,
    val content: String
)

@Serializable
data class OllamaChatChunk(
    val message: OllamaMessagePayload? = null,
    val done: Boolean = false,
    val error: String? = null,
    @SerialName("prompt_eval_count") val promptEvalCount: Int? = null,
    @SerialName("eval_count") val evalCount: Int? = null
)

// MARK: - Token Usage

data class TokenUsage(
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
) {
    val resolvedTotal: Int?
        get() = totalTokens ?: listOfNotNull(promptTokens, completionTokens)
            .takeIf { it.isNotEmpty() }
            ?.sum()
}

// MARK: - OpenAI-compatible API Models (llama-server)

@Serializable
data class OpenAIModelsResponse(
    val data: List<OpenAIModelEntry>
)

@Serializable
data class OpenAIModelEntry(
    val id: String
)

@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIChatMessage>,
    val stream: Boolean,
    @SerialName("stream_options") val streamOptions: OpenAIStreamOptions? = null,
    val temperature: Double? = null,
    @SerialName("top_k") val top_k: Int? = null,
    @SerialName("top_p") val top_p: Double? = null,
    @SerialName("min_p") val min_p: Double? = null,
    @SerialName("presence_penalty") val presence_penalty: Double? = null
)

@Serializable
data class OpenAIStreamOptions(
    @SerialName("include_usage") val includeUsage: Boolean
)

@Serializable
data class OpenAIChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIChatChunk(
    val choices: List<OpenAIChoice> = emptyList(),
    val usage: OpenAIUsage? = null
)

@Serializable
data class OpenAIUsage(
    @SerialName("prompt_tokens") val promptTokens: Int? = null,
    @SerialName("completion_tokens") val completionTokens: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null
) {
    val tokenUsage: TokenUsage
        get() = TokenUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens
        )
}

@Serializable
data class OpenAIChoice(
    val delta: OpenAIDelta
)

@Serializable
data class OpenAIDelta(
    val content: String? = null
)

// MARK: - Local Conversation Entities (replacing SwiftData)

@Serializable
data class Conversation(
    val id: String = generateUUID(),
    var title: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var modelName: String,
    var systemPrompt: String = "",
    var persistedMessages: List<PersistedMessage> = emptyList()
)

@Serializable
data class PersistedMessage(
    val id: String = generateUUID(),
    val role: String,
    var content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sortOrder: Int,
    val conversationId: String
)

// MARK: - Model Presets

@Serializable
data class ModelPreset(
    var id: String = generateUUID(),
    var name: String,
    var summary: String,
    var temperature: Double,
    var topK: Int,
    var topP: Double,
    var minP: Double = 0.0,
    var presencePenalty: Double = 0.0,
    var numCtx: Int? = null,
    var isBuiltIn: Boolean = false
) {
    val shortLabel: String
        get() {
            return "T ${format(temperature)} · K $topK · P ${format(topP)}" +
                    (if (minP > 0) " · MinP ${format(minP)}" else "") +
                    (if (presencePenalty != 0.0) " · Pres ${format(presencePenalty)}" else "")
        }

    private fun format(v: Double): String {
        val floorVal = kotlin.math.floor(v)
        return if (v == floorVal) {
            formatDecimal(v, 1)
        } else {
            formatDecimal(v, 2)
        }
    }
}

// MARK: - Format Helper for commonMain (pure Kotlin)

private fun formatDecimal(v: Double, digits: Int): String {
    val isNeg = v < 0
    val absV = if (isNeg) -v else v
    var factor = 1.0
    repeat(digits) { factor *= 10 }
    val roundedVal = kotlin.math.round(absV * factor) / factor
    val str = roundedVal.toString()
    val dotIndex = str.indexOf('.')
    val res = if (dotIndex == -1) {
        if (digits > 0) {
            str + "." + "0".repeat(digits)
        } else {
            str
        }
    } else {
        val intPart = str.substring(0, dotIndex)
        val decPart = str.substring(dotIndex + 1)
        if (decPart.length < digits) {
            intPart + "." + decPart + "0".repeat(digits - decPart.length)
        } else if (decPart.length > digits) {
            if (digits > 0) {
                intPart + "." + decPart.substring(0, digits)
            } else {
                intPart
            }
        } else {
            str
        }
    }
    return if (isNeg) "-$res" else res
}
