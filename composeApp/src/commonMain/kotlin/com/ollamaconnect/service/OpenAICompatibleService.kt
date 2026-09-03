package com.ollamaconnect.service

import com.ollamaconnect.models.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import ollama_connect.composeapp.generated.resources.Res
import ollama_connect.composeapp.generated.resources.error_connection_prefix
import ollama_connect.composeapp.generated.resources.error_server_http
import org.jetbrains.compose.resources.getString

/**
 * Backend for every server with an OpenAI-compatible API
 * (llama-server, oMLX, Rapid-MLX): `/v1/models` + `/v1/chat/completions`.
 */
class OpenAICompatibleService(
    private val host: String,
    private val port: Int,
    private val client: HttpClient,
    private val apiKey: String = ""
) : ChatService {

    private val baseURL: String
        get() = "http://$host:$port"

    private fun HttpRequestBuilder.applyAuth() {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isNotEmpty()) {
            header(HttpHeaders.Authorization, "Bearer $trimmedKey")
        }
    }

    override suspend fun fetchModels(): List<OllamaModelInfo> {
        val url = "$baseURL/v1/models"
        val response = client.get(url) {
            applyAuth()
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception(getString(Res.string.error_server_http, response.status.value))
        }
        val text = response.bodyAsText()
        val modelsResponse = Json { ignoreUnknownKeys = true }.decodeFromString<OpenAIModelsResponse>(text)
        return modelsResponse.data.map { modelEntry ->
            OllamaModelInfo(name = modelEntry.id, size = null, modifiedAt = null)
        }
    }

    override fun streamChat(
        model: String,
        messages: List<ChatMessage>,
        options: OllamaOptions?
    ): Flow<ChatStreamEvent> = flow {
        val url = "$baseURL/v1/chat/completions"
        val payload = OpenAIChatRequest(
            model = model,
            messages = messages.map { msg ->
                OpenAIChatMessage(role = msg.role, content = msg.content)
            },
            stream = true,
            streamOptions = OpenAIStreamOptions(includeUsage = true),
            temperature = options?.temperature,
            top_k = options?.topK,
            top_p = options?.topP,
            min_p = options?.minP,
            presence_penalty = options?.presencePenalty
        )

        val json = Json { encodeDefaults = false; ignoreUnknownKeys = true }
        val bodyString = json.encodeToString(payload)

        try {
            client.preparePost(url) {
                contentType(ContentType.Application.Json)
                header("Accept", "text/event-stream")
                applyAuth()
                setBody(bodyString)
            }.execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    throw Exception(getString(Res.string.error_server_http, response.status.value))
                }
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.trim().isEmpty()) continue

                    if (line.startsWith("data:")) {
                        val payloadString = line.substring(5).trim()
                        if (payloadString == "[DONE]") {
                            break
                        }
                        if (payloadString.isEmpty()) continue

                        val chunk = try {
                            Json { ignoreUnknownKeys = true }.decodeFromString<OpenAIChatChunk>(payloadString)
                        } catch (e: Exception) {
                            null
                        }

                        val usage = chunk?.usage?.tokenUsage
                        if (usage != null && usage.resolvedTotal != null) {
                            emit(ChatStreamEvent.Usage(usage))
                        }

                        val content = chunk?.choices?.firstOrNull()?.delta?.content
                        if (!content.isNullOrEmpty()) {
                            emit(ChatStreamEvent.Text(content))
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw Exception(getString(Res.string.error_connection_prefix, e.message ?: ""))
        }
    }
}
