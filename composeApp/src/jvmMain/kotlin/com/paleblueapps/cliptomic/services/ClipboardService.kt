package com.paleblueapps.cliptomic.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class ClipboardService {
    
    suspend fun getClipboardText(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null)
            
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                val text = transferable.getTransferData(DataFlavor.stringFlavor) as String
                Result.success(text)
            } else {
                Result.failure(Exception("No text content in clipboard"))
            }
        } catch (e: UnsupportedFlavorException) {
            Result.failure(Exception("Unsupported clipboard content type: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(Exception("Failed to read clipboard: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error reading clipboard: ${e.message}"))
        }
    }
    
    suspend fun setClipboardText(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val stringSelection = StringSelection(text)
            clipboard.setContents(stringSelection, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to set clipboard text: ${e.message}"))
        }
    }
    
    suspend fun hasTextContent(): Boolean = withContext(Dispatchers.IO) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null)
            transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)
        } catch (e: Exception) {
            false
        }
    }
}