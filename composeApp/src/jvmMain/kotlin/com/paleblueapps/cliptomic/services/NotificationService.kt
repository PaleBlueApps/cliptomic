package com.paleblueapps.cliptomic.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import java.io.IOException

class NotificationService {
    
    private val isMacOS = System.getProperty("os.name").lowercase().contains("mac")
    
    suspend fun showSuccessNotification(message: String = "Text rewritten successfully!") = withContext(Dispatchers.IO) {
        showNotification("Cliptomic", message, MessageType.INFO)
    }
    
    suspend fun showErrorNotification(message: String) = withContext(Dispatchers.IO) {
        showNotification("Cliptomic Error", message, MessageType.ERROR)
    }
    
    suspend fun showWarningNotification(message: String) = withContext(Dispatchers.IO) {
        showNotification("Cliptomic Warning", message, MessageType.WARNING)
    }
    
    private fun showNotification(title: String, message: String, messageType: MessageType) {
        try {
            if (isMacOS) {
                showMacOSNotification(title, message, messageType)
            } else {
                showAWTNotification(title, message, messageType)
            }
        } catch (e: Exception) {
            // Fallback to console output if notification fails
            println("Notification failed: $title - $message")
            e.printStackTrace()
        }
    }
    
    private fun showMacOSNotification(title: String, message: String, messageType: MessageType) {
        try {
            // Escape quotes and special characters for AppleScript
            val escapedTitle = title.replace("\"", "\\\"").replace("\\", "\\\\")
            val escapedMessage = message.replace("\"", "\\\"").replace("\\", "\\\\")
            
            // Determine the appropriate sound based on message type
            val sound = when (messageType) {
                MessageType.ERROR -> "Basso"
                MessageType.WARNING -> "Sosumi"
                else -> "Glass"
            }
            
            // Create AppleScript command for native macOS notification
            val script = """
                display notification "$escapedMessage" with title "$escapedTitle" sound name "$sound"
            """.trimIndent()
            
            // Execute the AppleScript command
            val process = ProcessBuilder("osascript", "-e", script)
                .redirectErrorStream(true)
                .start()
            
            // Wait for the process to complete with a timeout
            val completed = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                throw IOException("Notification command timed out")
            }
            
            if (process.exitValue() != 0) {
                throw IOException("Notification command failed with exit code: ${process.exitValue()}")
            }
        } catch (e: Exception) {
            // Fallback to AWT notification if macOS notification fails
            println("macOS notification failed, falling back to AWT: ${e.message}")
            showAWTNotification(title, message, messageType)
        }
    }
    
    private fun showAWTNotification(title: String, message: String, messageType: MessageType) {
        try {
            if (SystemTray.isSupported()) {
                val systemTray = SystemTray.getSystemTray()
                val trayIcons = systemTray.trayIcons
                
                // Use existing tray icon if available, otherwise create a temporary one
                val trayIcon = if (trayIcons.isNotEmpty()) {
                    trayIcons[0]
                } else {
                    // Create a simple 16x16 transparent image for the notification
                    val image = java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB)
                    val graphics = image.createGraphics()
                    graphics.color = java.awt.Color.BLUE
                    graphics.fillOval(2, 2, 12, 12)
                    graphics.dispose()
                    
                    val tempTrayIcon = TrayIcon(image, "Cliptomic")
                    systemTray.add(tempTrayIcon)
                    tempTrayIcon
                }
                
                trayIcon.displayMessage(title, message, messageType)
                
                // Remove temporary tray icon after showing notification
                if (trayIcons.isEmpty()) {
                    // Wait a bit for the notification to show, then remove the temporary icon
                    Thread.sleep(100)
                    systemTray.remove(trayIcon)
                }
            } else {
                // Fallback for systems without system tray support
                println("$title: $message")
            }
        } catch (e: Exception) {
            // Final fallback to console output
            println("AWT notification failed: $title - $message")
            e.printStackTrace()
        }
    }
    
    fun isSystemTraySupported(): Boolean {
        return SystemTray.isSupported()
    }
}