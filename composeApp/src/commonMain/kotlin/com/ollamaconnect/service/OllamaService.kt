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

class OllamaService(
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
        val url = "$baseURL/api/tags"
        val response = client.get(url) {
            applyAuth()
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception(getString(Res.string.error_server_http, response.status.value))
        }
        val text = response.bodyAsText()
        val tagsResponse = Json { ignoreUnknownKeys = true }.decodeFromString<OllamaTagsResponse>(text)
        return tagsResponse.models
    }

    override fun streamChat(
        model: String,
        messages: List<ChatMessage>,
        options: OllamaOptions?
    ): Flow<ChatStreamEvent> = flow {
        val url = "$baseURL/api/chat"
        val payload = OllamaChatRequest(
            model = model,
            messages = messages.map { msg ->
                OllamaMessagePayload(role = msg.role, content = msg.content)
            },
            stream = true,
            options = options
        )

        val json = Json { encodeDefaults = false; ignoreUnknownKeys = true }
        val bodyString = json.encodeToString(payload)

        try {
            client.preparePost(url) {
                contentType(ContentType.Application.Json)
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

                    val chunk = try {
                        Json { ignoreUnknownKeys = true }.decodeFromString<OllamaChatChunk>(line)
                    } catch (e: Exception) {
                        null
                    }

                    if (chunk != null) {
                        if (chunk.error != null) {
                            throw Exception(chunk.error)
                        }
                        val content = chunk.message?.content
                        if (!content.isNullOrEmpty()) {
                            emit(ChatStreamEvent.Text(content))
                        }
                        if (chunk.done) {
                            val usage = TokenUsage(
                                promptTokens = chunk.promptEvalCount,
                                completionTokens = chunk.evalCount,
                                totalTokens = null
                            )
                            if (usage.resolvedTotal != null) {
                                emit(ChatStreamEvent.Usage(usage))
                            }
                            break
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
