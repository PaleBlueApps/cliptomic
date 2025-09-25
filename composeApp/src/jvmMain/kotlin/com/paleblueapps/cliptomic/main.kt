package com.paleblueapps.cliptomic

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.paleblueapps.cliptomic.presentation.TrayManager
import com.paleblueapps.cliptomic.presentation.settings.SettingsScreen
import com.paleblueapps.cliptomic.presentation.settings.SettingsViewModel
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
        
        // Settings window
        if (trayManager.showSettings.value) {
            Window(
                onCloseRequest = { trayManager.showSettings.value = false },
                title = "Cliptomic Settings",
                state = WindowState(width = 600.dp, height = 800.dp)
            ) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onClose = { trayManager.showSettings.value = false }
                )
            }
        }
        
        // Cleanup on exit
        DisposableEffect(Unit) {
            onDispose {
                cliptomicService.cleanup()
                trayManager.cleanup()
            }
        }
    }
}