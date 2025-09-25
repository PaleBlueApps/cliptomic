package com.paleblueapps.cliptomic.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.rememberTrayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.SwingUtilities

class TrayManager {
    private var trayIcon: TrayIcon? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    val showSettings = mutableStateOf(false)
    val isProcessing = mutableStateOf(false)
    
    fun initialize() {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported")
            return
        }
        
        SwingUtilities.invokeLater {
            setupTrayIcon()
        }
    }
    
    private fun setupTrayIcon() {
        val systemTray = SystemTray.getSystemTray()
        
        // Create tray icon image
        val trayIconImage = createTrayIconImage()
        
        // Create popup menu
        val popup = PopupMenu()
        
        // Settings menu item
        val settingsItem = MenuItem("Settings")
        settingsItem.addActionListener {
            showSettings.value = true
        }
        popup.add(settingsItem)
        
        popup.addSeparator()
        
        // Status menu item (non-clickable)
        val statusItem = MenuItem("Ready")
        statusItem.isEnabled = false
        popup.add(statusItem)
        
        popup.addSeparator()
        
        // Exit menu item
        val exitItem = MenuItem("Exit")
        exitItem.addActionListener {
            cleanup()
            System.exit(0)
        }
        popup.add(exitItem)
        
        // Create tray icon
        trayIcon = TrayIcon(trayIconImage, "Cliptomic - Text Rewriter", popup).apply {
            isImageAutoSize = true
            
            // Handle tray icon click (show settings)
            addActionListener {
                showSettings.value = true
            }
        }
        
        try {
            systemTray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added: ${e.message}")
        }
    }
    
    private fun createTrayIconImage(): Image {
        // Try to load PNG icon first, generate if not exists
        val iconFile = File("composeApp/src/jvmMain/composeResources/drawable/tray_icon_small.png")
        if (iconFile.exists()) {
            try {
                return ImageIO.read(iconFile)
            } catch (e: Exception) {
                println("Failed to load PNG icon, generating programmatically: ${e.message}")
            }
        }
        
        // Generate modern icon programmatically
        return generateModernTrayIcon(16)
    }
    
    private fun generateModernTrayIcon(size: Int): BufferedImage {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        // Enable antialiasing for smooth edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        
        // Modern dark-theme compatible colors
        val primaryColor = Color(108, 117, 125) // Neutral gray that works in both themes
        val accentColor = Color(255, 255, 255, 200) // Semi-transparent white
        
        // Draw clipboard outline (modern minimal style)
        val clipboardWidth = size * 0.7
        val clipboardHeight = size * 0.8
        val x = (size - clipboardWidth) / 2
        val y = (size - clipboardHeight) / 2
        
        g2d.color = primaryColor
        g2d.stroke = BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2d.drawRoundRect(x.toInt(), y.toInt(), clipboardWidth.toInt(), clipboardHeight.toInt(), 2, 2)
        
        // Draw clip at top
        val clipWidth = size * 0.3
        val clipHeight = size * 0.15
        val clipX = (size - clipWidth) / 2
        val clipY = y - clipHeight * 0.3
        g2d.fillRoundRect(clipX.toInt(), clipY.toInt(), clipWidth.toInt(), clipHeight.toInt(), 2, 2)
        
        // Draw text lines inside clipboard
        g2d.color = accentColor
        g2d.stroke = BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val lineY1 = y + clipboardHeight * 0.3
        val lineY2 = y + clipboardHeight * 0.5
        val lineY3 = y + clipboardHeight * 0.7
        val lineStartX = x + clipboardWidth * 0.2
        val lineEndX = x + clipboardWidth * 0.8
        
        g2d.drawLine(lineStartX.toInt(), lineY1.toInt(), lineEndX.toInt(), lineY1.toInt())
        g2d.drawLine(lineStartX.toInt(), lineY2.toInt(), (lineEndX * 0.7).toInt(), lineY2.toInt())
        g2d.drawLine(lineStartX.toInt(), lineY3.toInt(), (lineEndX * 0.9).toInt(), lineY3.toInt())
        
        g2d.dispose()
        
        // Save as PNG for future use
        saveTrayIconPng(image, size)
        
        return image
    }
    
    private fun saveTrayIconPng(image: BufferedImage, size: Int) {
        try {
            val resourcesDir = File("composeApp/src/jvmMain/composeResources/drawable")
            resourcesDir.mkdirs()
            
            val filename = if (size <= 16) "tray_icon_small.png" else "tray_icon.png"
            val iconFile = File(resourcesDir, filename)
            
            ImageIO.write(image, "PNG", iconFile)
            println("Saved tray icon: ${iconFile.absolutePath}")
        } catch (e: Exception) {
            println("Failed to save PNG icon: ${e.message}")
        }
    }
    
    private fun createProcessingTrayIconImage(): Image {
        // Try to load PNG processing icon first, generate if not exists
        val iconFile = File("composeApp/src/jvmMain/composeResources/drawable/tray_icon_processing.png")
        if (iconFile.exists()) {
            try {
                return ImageIO.read(iconFile)
            } catch (e: Exception) {
                println("Failed to load PNG processing icon, generating programmatically: ${e.message}")
            }
        }
        
        // Generate modern processing icon programmatically
        return generateModernProcessingIcon(16)
    }
    
    private fun generateModernProcessingIcon(size: Int): BufferedImage {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        // Enable antialiasing for smooth edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        
        // Processing state colors - warmer orange/amber for activity
        val primaryColor = Color(255, 152, 0) // Warm orange that works in both themes
        val accentColor = Color(255, 255, 255, 220) // Slightly more opaque white
        
        // Draw clipboard outline (same modern minimal style)
        val clipboardWidth = size * 0.7
        val clipboardHeight = size * 0.8
        val x = (size - clipboardWidth) / 2
        val y = (size - clipboardHeight) / 2
        
        g2d.color = primaryColor
        g2d.stroke = BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2d.drawRoundRect(x.toInt(), y.toInt(), clipboardWidth.toInt(), clipboardHeight.toInt(), 2, 2)
        
        // Draw clip at top
        val clipWidth = size * 0.3
        val clipHeight = size * 0.15
        val clipX = (size - clipWidth) / 2
        val clipY = y - clipHeight * 0.3
        g2d.fillRoundRect(clipX.toInt(), clipY.toInt(), clipWidth.toInt(), clipHeight.toInt(), 2, 2)
        
        // Draw animated-looking text lines (slightly different positions for processing feel)
        g2d.color = accentColor
        g2d.stroke = BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val lineY1 = y + clipboardHeight * 0.3
        val lineY2 = y + clipboardHeight * 0.5
        val lineY3 = y + clipboardHeight * 0.7
        val lineStartX = x + clipboardWidth * 0.2
        val lineEndX = x + clipboardWidth * 0.8
        
        // Slightly different line lengths to suggest activity
        g2d.drawLine(lineStartX.toInt(), lineY1.toInt(), (lineEndX * 0.9).toInt(), lineY1.toInt())
        g2d.drawLine(lineStartX.toInt(), lineY2.toInt(), (lineEndX * 0.6).toInt(), lineY2.toInt())
        g2d.drawLine(lineStartX.toInt(), lineY3.toInt(), (lineEndX * 0.8).toInt(), lineY3.toInt())
        
        g2d.dispose()
        
        // Save as PNG for future use
        saveProcessingIconPng(image)
        
        return image
    }
    
    private fun saveProcessingIconPng(image: BufferedImage) {
        try {
            val resourcesDir = File("composeApp/src/jvmMain/composeResources/drawable")
            resourcesDir.mkdirs()
            
            val iconFile = File(resourcesDir, "tray_icon_processing.png")
            ImageIO.write(image, "PNG", iconFile)
            println("Saved processing tray icon: ${iconFile.absolutePath}")
        } catch (e: Exception) {
            println("Failed to save PNG processing icon: ${e.message}")
        }
    }
    
    fun updateStatus(status: String, isProcessing: Boolean = false) {
        this.isProcessing.value = isProcessing
        
        SwingUtilities.invokeLater {
            trayIcon?.let { icon ->
                icon.toolTip = "Cliptomic - $status"
                
                // Update icon based on processing state
                if (isProcessing) {
                    icon.image = createProcessingTrayIconImage()
                } else {
                    icon.image = createTrayIconImage()
                }
                
                // Update status menu item
                val popup = icon.popupMenu
                if (popup.itemCount > 2) {
                    val statusItem = popup.getItem(2) // Status item is at index 2
                    statusItem.label = status
                }
            }
        }
    }
    
    fun showNotification(title: String, message: String, type: Notification.Type = Notification.Type.Info) {
        SwingUtilities.invokeLater {
            trayIcon?.displayMessage(
                title, 
                message, 
                when (type) {
                    Notification.Type.None -> TrayIcon.MessageType.NONE
                    Notification.Type.Info -> TrayIcon.MessageType.INFO
                    Notification.Type.Warning -> TrayIcon.MessageType.WARNING
                    Notification.Type.Error -> TrayIcon.MessageType.ERROR
                }
            )
        }
    }
    
    fun cleanup() {
        trayIcon?.let { icon ->
            SystemTray.getSystemTray().remove(icon)
        }
        trayIcon = null
    }
}