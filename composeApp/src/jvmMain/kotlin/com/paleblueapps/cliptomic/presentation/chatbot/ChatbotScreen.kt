package com.paleblueapps.cliptomic.presentation.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatbotScreen(
    viewModel: ChatbotViewModel,
    apiKey: String,
    model: String,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    
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
            .background(Color(0xFF2B2B2B))
            .padding(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Chat",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row {
                // Clear conversation button
                TextButton(
                    onClick = { viewModel.clearConversation() },
                    modifier = Modifier.height(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Clear",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                // Close button
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.height(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "✕",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Messages area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Color(0xFF1E1E1E),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF404040),
                    RoundedCornerShape(8.dp)
                )
        ) {
            if (viewModel.messages.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Start a conversation with AI",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Type your message below",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF404040)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI is thinking...",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Error message
        if (viewModel.error.value != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = viewModel.error.value!!,
                color = Color(0xFFFF6B6B),
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Chip buttons for quick replies
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                                val newText = "Reply to this email: \"${clipboardContent.trim()}\" "
                                viewModel.updateInput(newText)
                            }
                        } catch (e: Exception) {
                            // Handle clipboard access error silently
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF404040)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Reply to this email",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
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
                            }
                        } catch (e: Exception) {
                            // Handle clipboard access error silently
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF404040)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Reply to this message",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input area
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = viewModel.currentInput.value,
                onValueChange = viewModel::updateInput,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
                            if (keyEvent.isMetaPressed || keyEvent.isCtrlPressed) {
                                // Cmd/Ctrl+Enter for new line
                                viewModel.updateInput(viewModel.currentInput.value + "\n")
                                true
                            } else {
                                // Enter to send
                                viewModel.sendMessage(
                                    viewModel.currentInput.value,
                                    apiKey,
                                    model
                                )
                                true
                            }
                        } else {
                            false
                        }
                    },
                placeholder = {
                    Text(
                        "Ask AI anything...",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedBorderColor = Color(0xFF0078D4),
                    unfocusedBorderColor = Color(0xFF404040),
                    cursorColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        viewModel.sendMessage(
                            viewModel.currentInput.value,
                            apiKey,
                            model
                        )
                    }
                ),
                maxLines = 3,
                shape = RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
            IconButton(
                onClick = {
                    viewModel.sendMessage(
                        viewModel.currentInput.value,
                        apiKey,
                        model
                    )
                },
                enabled = viewModel.currentInput.value.isNotBlank() && !viewModel.isLoading.value,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (viewModel.currentInput.value.isNotBlank() && !viewModel.isLoading.value) 
                            Color(0xFF0078D4) 
                        else 
                            Color(0xFF404040),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    "Send",
                    color = Color.White,
                    fontSize = 11.sp,
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
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Copy button for AI messages only
                if (!message.isUser) {
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
}