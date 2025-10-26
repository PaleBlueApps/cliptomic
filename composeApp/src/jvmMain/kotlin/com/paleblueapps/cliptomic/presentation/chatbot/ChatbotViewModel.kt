package com.paleblueapps.cliptomic.presentation.chatbot

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.paleblueapps.cliptomic.services.OpenRouterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String? = null
)

class ChatbotViewModel(
    private val openRouterService: OpenRouterService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    val messages = mutableStateListOf<ChatMessage>()
    val currentInput = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    
    fun sendMessage(
        message: String,
        apiKey: String,
        model: String
    ) {
        if (message.isBlank() || isLoading.value) return
        
        // Add user message
        messages.add(ChatMessage(content = message.trim(), isUser = true, model = model))
        currentInput.value = ""
        isLoading.value = true
        error.value = null
        
        scope.launch {
            try {
                // Prepare conversation history for API
                val conversationMessages = messages.takeLast(10).map { chatMessage ->
                    com.paleblueapps.cliptomic.services.Message(
                        role = if (chatMessage.isUser) "user" else "assistant",
                        content = chatMessage.content
                    )
                }
                
                val result = openRouterService.chatWithHistory(
                    messages = conversationMessages,
                    apiKey = apiKey,
                    model = model
                )
                
                if (result.isSuccess) {
                    val response: String = result.getOrThrow()
                    messages.add(ChatMessage(content = response, isUser = false, model = model))
                } else {
                    error.value = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Unknown error occurred"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun clearConversation() {
        messages.clear()
        error.value = null
    }
    
    fun updateInput(input: String) {
        currentInput.value = input
    }
}