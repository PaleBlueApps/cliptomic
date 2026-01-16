package com.paleblueapps.cliptomic.presentation.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.*
import com.paleblueapps.cliptomic.services.OpenRouterService
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import java.awt.MouseInfo
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.text.SimpleDateFormat
import java.util.*

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WindowScope.ChatbotScreen(
    viewModel: ChatbotViewModel,
    apiKey: String,
    model: String,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var selectedModel by remember { mutableStateOf(model) }
    var expandedModelDropdown by remember { mutableStateOf(false) }
    
    val dragData = remember { floatArrayOf(0f, 0f, 0f, 0f) }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            delay(100) // Small delay to ensure layout is complete
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }
    
    // Auto-focus input field when window opens
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF0000000)) // More opaque dark background
            .padding(16.dp)
    ) {
        // Custom title bar with clear and close buttons - draggable area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            val mouseLoc = MouseInfo.getPointerInfo().location
                            dragData[0] = mouseLoc.x.toFloat()
                            dragData[1] = mouseLoc.y.toFloat()
                            dragData[2] = window.x.toFloat()
                            dragData[3] = window.y.toFloat()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val mouseLoc = MouseInfo.getPointerInfo().location
                            val nextX = (dragData[2] + (mouseLoc.x - dragData[0])).roundToInt()
                            val nextY = (dragData[3] + (mouseLoc.y - dragData[1])).roundToInt()
                            if (nextX != window.x || nextY != window.y) {
                                window.setLocation(nextX, nextY)
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear chat button
            IconButton(
                onClick = { viewModel.clearConversation() },
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    "🗑",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    "✕",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Messages area - only show when there are messages
        if (viewModel.messages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.messages) { message ->
                        MessageBubble(
                            message = message,
                            onCopyMessage = { content ->
                                try {
                                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                    val selection = StringSelection(content)
                                    clipboard.setContents(selection, null)
                                } catch (e: Exception) {
                                    // Handle clipboard error silently
                                }
                            }
                        )
                    }
                    
                    // Loading indicator
                    if (viewModel.isLoading.value) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(end = 48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "AI is thinking...",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            // When no messages, just add flexible space
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Error message
        if (viewModel.error.value != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x40FF6B6B)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = viewModel.error.value!!,
                    color = Color(0xFFFF9999),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Model selector with retry button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedModelDropdown,
                onExpandedChange = { expandedModelDropdown = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedModel.substringAfterLast("/").substringBefore(":"),
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            "Model",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelDropdown)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
                
                ExposedDropdownMenu(
                    expanded = expandedModelDropdown,
                    onDismissRequest = { expandedModelDropdown = false },
                    modifier = Modifier
                        .background(Color(0xFF2D2D2D))
                        .heightIn(max = 300.dp)
                ) {
                    OpenRouterService.ALL_PREDEFINED_MODELS.forEach { modelOption ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = modelOption.substringAfterLast("/").substringBefore(":"),
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (modelOption.contains(":free")) "Free" else "Paid",
                                        color = if (modelOption.contains(":free")) 
                                            Color(0xFF4CAF50) 
                                        else 
                                            Color(0xFFFF9800),
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            onClick = {
                                selectedModel = modelOption
                                expandedModelDropdown = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White
                            )
                        )
                    }
                }
            }
            
            // Retry last prompt button
            val lastUserMessage = viewModel.messages.filter { it.isUser }.lastOrNull()
            IconButton(
                onClick = {
                    lastUserMessage?.let { message ->
                        viewModel.sendMessage(
                            message = message.content,
                            apiKey = apiKey,
                            model = selectedModel
                        )
                    }
                },
                enabled = lastUserMessage != null && !viewModel.isLoading.value,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (lastUserMessage != null && !viewModel.isLoading.value) 
                            Color.White.copy(alpha = 0.1f) 
                        else 
                            Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Text(
                    text = "↻",
                    fontSize = 24.sp,
                    color = if (lastUserMessage != null && !viewModel.isLoading.value) 
                        Color.White.copy(alpha = 0.9f) 
                    else 
                        Color.White.copy(alpha = 0.3f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Chip buttons for quick replies
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reply to email chip
            Card(
                modifier = Modifier
                    .clickable {
                        try {
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            val clipboardContent = clipboard.getData(DataFlavor.stringFlavor) as? String
                            if (!clipboardContent.isNullOrBlank()) {
                                val newText = "Reply to this email with a complete email response (no subject line, just the email body): \"${clipboardContent.trim()}\" "
                                viewModel.updateInput(newText)
                                focusRequester.requestFocus()
                            }
                        } catch (e: Exception) {
                            // Handle clipboard access error silently
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Reply to email",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }
            
            // Reply to message chip
            Card(
                modifier = Modifier
                    .clickable {
                        try {
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            val clipboardContent = clipboard.getData(DataFlavor.stringFlavor) as? String
                            if (!clipboardContent.isNullOrBlank()) {
                                val newText = "Reply to this message: \"${clipboardContent.trim()}\" "
                                viewModel.updateInput(newText)
                                focusRequester.requestFocus()
                            }
                        } catch (e: Exception) {
                            // Handle clipboard access error silently
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Reply to message",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }

            // Rewrite chip
            Card(
                modifier = Modifier
                    .clickable {
                        try {
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            val clipboardContent = clipboard.getData(DataFlavor.stringFlavor) as? String
                            if (!clipboardContent.isNullOrBlank()) {
                                val newText = "Rewrite this text more clearly: \"${clipboardContent.trim()}\" "
                                viewModel.updateInput(newText)
                                focusRequester.requestFocus()
                            }
                        } catch (e: Exception) {
                            // Handle clipboard access error silently
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Rewrite",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }

            // Translate chip
            Card(
                modifier = Modifier
                    .clickable {
                        try {
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            val clipboardContent = clipboard.getData(DataFlavor.stringFlavor) as? String
                            if (!clipboardContent.isNullOrBlank()) {
                                val newText = "\"${clipboardContent.trim()}\" : Translate this text to [language]"
                                viewModel.updateInput(newText)
                                focusRequester.requestFocus()
                            }
                        } catch (e: Exception) {
                            // Handle clipboard access error silently
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Translate",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Input area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = viewModel.currentInput.value,
                onValueChange = { viewModel.updateInput(it) },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
                            if (keyEvent.isShiftPressed) {
                                // Shift+Enter for new line
                                viewModel.updateInput(viewModel.currentInput.value.text + "\n")
                                true
                            } else {
                                // Enter to send
                                if (viewModel.currentInput.value.text.isNotBlank()) {
                                    viewModel.sendMessage(
                                        viewModel.currentInput.value.text,
                                        apiKey,
                                        selectedModel
                                    )
                                }
                                true
                            }
                        } else {
                            false
                        }
                    },
                placeholder = {
                    Text(
                        "Ask AI anything...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedBorderColor = Color.White.copy(alpha = 0.3f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.sendMessage(
                            viewModel.currentInput.value.text,
                            apiKey,
                            selectedModel
                        )
                    }
                ),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Send button
            IconButton(
                onClick = {
                    viewModel.sendMessage(
                        viewModel.currentInput.value.text,
                        apiKey,
                        selectedModel
                    )
                },
                enabled = viewModel.currentInput.value.text.isNotBlank() && !viewModel.isLoading.value,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (viewModel.currentInput.value.text.isNotBlank() && !viewModel.isLoading.value) 
                            Color.White.copy(alpha = 0.2f)
                        else 
                            Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Text(
                    "→",
                    color = if (viewModel.currentInput.value.text.isNotBlank() && !viewModel.isLoading.value) 
                        Color.White 
                    else 
                        Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onCopyMessage: (String) -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF0078D4) else Color(0xFF404040)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .let { 
                    if (message.isUser) it.padding(start = 48.dp) 
                    else it.padding(end = 48.dp) 
                }
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = message.content,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Display model name if available
                    if (message.model != null) {
                        Text(
                            text = message.model.substringAfterLast("/").substringBefore(":"),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Copy button for all messages
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onCopyMessage(message.content) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}