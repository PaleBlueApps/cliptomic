package com.paleblueapps.cliptomic.services

import com.paleblueapps.cliptomic.presentation.TrayManager
import com.paleblueapps.cliptomic.presentation.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CliptomicService(
    private val settingsViewModel: SettingsViewModel,
    private val trayManager: TrayManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val openRouterService = OpenRouterService()
    private val clipboardService = ClipboardService()
    private val notificationService = NotificationService()
    private val globalHotkeyService = GlobalHotkeyService()
    
    fun initialize() {
        // Initialize global hotkey service
        val hotkeyInitialized = globalHotkeyService.initialize()
        if (!hotkeyInitialized) {
            trayManager.updateStatus("Hotkey registration failed")
            scope.launch {
                notificationService.showErrorNotification(
                    "Failed to register global hotkey. Please run as administrator or check permissions."
                )
            }
            return
        }
        
        // Listen for hotkey presses
        scope.launch {
            globalHotkeyService.hotkeyPressed.collectLatest {
                handleHotkeyPressed()
            }
        }
        
        trayManager.updateStatus("Ready - ${globalHotkeyService.getHotkeyDescription()}")
    }
    
    private suspend fun handleHotkeyPressed() {
        // Validate settings first
        if (!settingsViewModel.validateSettings()) {
            trayManager.updateStatus("Configuration required")
            notificationService.showWarningNotification(
                "Please configure your API key and model in settings"
            )
            trayManager.showSettings.value = true
            return
        }
        
        trayManager.updateStatus("Processing...", isProcessing = true)
        
        try {
            // Get text from clipboard
            val clipboardResult = clipboardService.getClipboardText()
            if (clipboardResult.isFailure) {
                throw Exception("Failed to read clipboard: ${clipboardResult.exceptionOrNull()?.message}")
            }
            
            val originalText = clipboardResult.getOrThrow()
            if (originalText.isBlank()) {
                throw Exception("Clipboard is empty or contains no text")
            }
            
            // Limit text length to prevent excessive API usage
            if (originalText.length > 5000) {
                throw Exception("Text is too long (max 5000 characters)")
            }
            
            // Send to OpenRouter for rewriting
            val apiKey = settingsViewModel.getCurrentApiKey()
            val model = settingsViewModel.getCurrentModel()
            val systemPrompt = settingsViewModel.getCurrentSystemPrompt()
            val userPromptTemplate = settingsViewModel.getCurrentUserPromptTemplate()
            
            val rewriteResult = openRouterService.rewriteText(
                text = originalText,
                apiKey = apiKey,
                model = model,
                systemPrompt = systemPrompt,
                userPromptTemplate = userPromptTemplate
            )
            
            if (rewriteResult.isFailure) {
                throw Exception("Failed to rewrite text: ${rewriteResult.exceptionOrNull()?.message}")
            }
            
            val rewrittenText = rewriteResult.getOrThrow()
            
            // Update clipboard with rewritten text
            val setClipboardResult = clipboardService.setClipboardText(rewrittenText)
            if (setClipboardResult.isFailure) {
                throw Exception("Failed to update clipboard: ${setClipboardResult.exceptionOrNull()?.message}")
            }
            
            // Show success notification
            val charCount = rewrittenText.length
            notificationService.showSuccessNotification(
                "Text rewritten successfully! ($charCount characters)"
            )
            
            trayManager.updateStatus("Ready - ${globalHotkeyService.getHotkeyDescription()}")
            
        } catch (e: Exception) {
            // Show error notification
            notificationService.showErrorNotification(
                e.message ?: "Unknown error occurred"
            )
            
            trayManager.updateStatus("Error - ${globalHotkeyService.getHotkeyDescription()}")
            
            // Log error for debugging
            println("CliptomicService error: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun cleanup() {
        globalHotkeyService.cleanup()
        openRouterService.close()
    }
    
    fun getStatus(): String {
        return when {
            !settingsViewModel.validateSettings() -> "Configuration required"
            globalHotkeyService.hotkeyPressed != null -> "Ready - ${globalHotkeyService.getHotkeyDescription()}"
            else -> "Initializing..."
        }
    }
}