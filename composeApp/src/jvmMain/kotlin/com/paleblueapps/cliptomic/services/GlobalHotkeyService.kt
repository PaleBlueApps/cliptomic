package com.paleblueapps.cliptomic.services

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.logging.Level
import java.util.logging.Logger

class GlobalHotkeyService : NativeKeyListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _hotkeyPressed = MutableSharedFlow<Unit>()
    val hotkeyPressed: SharedFlow<Unit> = _hotkeyPressed.asSharedFlow()
    
    private var isShiftPressed = false
    private var isControlPressed = false
    private var isInitialized = false
    
    init {
        // Disable JNativeHook logging to reduce console noise
        val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
        logger.level = Level.WARNING
        logger.useParentHandlers = false
    }
    
    fun initialize(): Boolean {
        if (isInitialized) return true
        
        return try {
            GlobalScreen.registerNativeHook()
            GlobalScreen.addNativeKeyListener(this)
            isInitialized = true
            println("Global hotkey service initialized successfully")
            true
        } catch (ex: NativeHookException) {
            println("Failed to register native hook: ${ex.message}")
            false
        }
    }
    
    fun cleanup() {
        if (!isInitialized) return
        
        try {
            GlobalScreen.removeNativeKeyListener(this)
            GlobalScreen.unregisterNativeHook()
            isInitialized = false
            println("Global hotkey service cleaned up")
        } catch (ex: NativeHookException) {
            println("Failed to unregister native hook: ${ex.message}")
        }
    }
    
    override fun nativeKeyPressed(e: NativeKeyEvent) {
        when (e.keyCode) {
            42, 54 -> { // Left Shift, Right Shift
                isShiftPressed = true
            }
            29, 157 -> { // Left Control, Right Control
                isControlPressed = true
            }
            57 -> { // Space key
                if (isShiftPressed && isControlPressed) {
                    scope.launch {
                        _hotkeyPressed.emit(Unit)
                    }
                }
            }
        }
    }
    
    override fun nativeKeyReleased(e: NativeKeyEvent) {
        when (e.keyCode) {
            42, 54 -> { // Left Shift, Right Shift
                isShiftPressed = false
            }
            29, 157 -> { // Left Control, Right Control
                isControlPressed = false
            }
        }
    }
    
    override fun nativeKeyTyped(e: NativeKeyEvent) {
        // Not used for hotkey detection
    }
    
    fun getHotkeyDescription(): String {
        return "Shift+Control+Space"
    }
}