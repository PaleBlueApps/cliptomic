package com.paleblueapps.cliptomic

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Color as AwtColor
import com.paleblueapps.cliptomic.presentation.TrayManager
import com.paleblueapps.cliptomic.presentation.settings.SettingsScreen
import com.paleblueapps.cliptomic.presentation.settings.SettingsViewModel
import com.paleblueapps.cliptomic.presentation.chatbot.ChatbotScreen
import com.paleblueapps.cliptomic.presentation.chatbot.ChatbotViewModel
import com.paleblueapps.cliptomic.services.CliptomicService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    // Hide from macOS dock - must be set before creating any UI components
    System.setProperty("apple.awt.UIElement", "true")
    
    application(exitProcessOnExit = false) {
        val trayManager = remember { TrayManager() }
        val settingsViewModel = remember { SettingsViewModel() }
        val cliptomicService = remember { CliptomicService(settingsViewModel, trayManager) }
        val chatbotOpenRouterService = remember { com.paleblueapps.cliptomic.services.OpenRouterService() }
        val chatbotViewModel = remember { ChatbotViewModel(chatbotOpenRouterService) }
        
        // Initialize services on startup
        LaunchedEffect(Unit) {
            GlobalScope.launch {
                trayManager.initialize()
                cliptomicService.initialize()
            }
        }
        
        // Keep the application alive
        LaunchedEffect(Unit) {
            // This keeps the Compose application context alive
            while (true) {
                kotlinx.coroutines.delay(1000)
            }
        }
        
        // Window states for dragging
        val settingsWindowState = remember { WindowState(width = 600.dp, height = 800.dp) }
        val chatbotWindowState = remember { WindowState(width = 400.dp, height = 600.dp) }
        
        // Settings window
        if (trayManager.showSettings.value) {
            Window(
                onCloseRequest = { trayManager.showSettings.value = false },
                title = "Cliptomic Settings",
                state = settingsWindowState,
                undecorated = true,
                transparent = true
            ) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onClose = { trayManager.showSettings.value = false }
                )
            }
        }
        
        // Chatbot window
        if (trayManager.showChatbot.value) {
            Window(
                onCloseRequest = { trayManager.showChatbot.value = false },
                title = "AI Chat",
                state = chatbotWindowState,
                alwaysOnTop = true,
                undecorated = true,
                transparent = true
            ) {
                ChatbotScreen(
                    viewModel = chatbotViewModel,
                    apiKey = settingsViewModel.getCurrentApiKey(),
                    model = settingsViewModel.getCurrentModel(),
                    onClose = { trayManager.showChatbot.value = false }
                )
            }
        }
        
        // Cleanup on exit
        DisposableEffect(Unit) {
            onDispose {
                cliptomicService.cleanup()
                chatbotOpenRouterService.close()
                trayManager.cleanup()
            }
        }
    }
}