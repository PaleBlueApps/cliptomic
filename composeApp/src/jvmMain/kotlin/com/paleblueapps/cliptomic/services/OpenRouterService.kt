package com.paleblueapps.cliptomic.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 1000,
    val temperature: Double = 0.7
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

class OpenRouterService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun rewriteText(
        text: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPromptTemplate: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = OpenRouterRequest(
                model = model,
                messages = listOf(
                    Message(
                        role = "system",
                        content = systemPrompt
                    ),
                    Message(
                        role = "user",
                        content = userPromptTemplate.replace("{text}", text)
                    )
                )
            )

            val response: OpenRouterResponse = client.post("https://openrouter.ai/api/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "https://github.com/paleblueapps/cliptomic")
                header("X-Title", "Cliptomic")
                setBody(request)
            }.body()

            val rewrittenText = response.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("No response from OpenRouter"))

            Result.success(rewrittenText.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }

    companion object {
        // Free models available on OpenRouter
        val FREE_MODELS = listOf(
            "mistralai/mistral-small-3.2-24b-instruct:free",
            "mistralai/mistral-7b-instruct:free",
            "openai/gpt-oss-120b:free",
            "openai/gpt-oss-120b:free",
            "meta-llama/llama-3.3-8b-instruct:free",
            "meta-llama/llama-4-maverick:free",
            "meta-llama/llama-3.3-70b-instruct:free",
            "qwen/qwen3-30b-a3b:free",
            "google/gemma-3-27b-it:free",
            "google/gemini-2.0-flash-exp:free",
            "microsoft/phi-3-mini-128k-instruct:free",
            "huggingfaceh4/zephyr-7b-beta:free",
            "openchat/openchat-7b:free",
            "gryphe/mythomist-7b:free",
            "undi95/toppy-m-7b:free",
        )

        // Top paid models
        val PAID_MODELS = listOf(
            "anthropic/claude-sonnet-4",
            "google/gemini-2.5-flash",
            "google/gemini-2.5-pro",
            "openai/gpt-4.1-mini",
            "openai/gpt-5",
            "openai/gpt-4.1-mini",
            "deepseek/deepseek-chat-v3.1",
        )

        val ALL_PREDEFINED_MODELS = FREE_MODELS + PAID_MODELS
    }
}