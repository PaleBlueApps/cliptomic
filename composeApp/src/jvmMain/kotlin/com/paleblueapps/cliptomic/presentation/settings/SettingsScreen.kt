package com.paleblueapps.cliptomic.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.WindowScope
import java.awt.MouseInfo
import kotlin.math.roundToInt
import com.paleblueapps.cliptomic.services.OpenRouterService

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WindowScope.SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onClose: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showApiKey by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val dragData = remember { floatArrayOf(0f, 0f, 0f, 0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF0000000)) // More opaque dark background
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Custom title bar with just X button - draggable area
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
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

        Spacer(modifier = Modifier.height(8.dp))

        // API Key Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "OpenRouter API Key",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::updateApiKey,
                    label = { Text("API Key", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Enter your OpenRouter API key", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick = { showApiKey = !showApiKey }
                        ) {
                            Text(
                                if (showApiKey) "Hide" else "Show",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Text(
                    text = "Get your API key from https://openrouter.ai/keys",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Model Selection Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Model Selection",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Custom model toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.useCustomModel,
                        onCheckedChange = viewModel::updateUseCustomModel,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White.copy(alpha = 0.8f),
                            uncheckedColor = Color.White.copy(alpha = 0.3f),
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Use custom model",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }

                if (uiState.useCustomModel) {
                    // Custom model input
                    OutlinedTextField(
                        value = uiState.customModel,
                        onValueChange = viewModel::updateCustomModel,
                        label = { Text("Custom Model", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("e.g., openai/gpt-4o", color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedBorderColor = Color.White.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Text(
                        text = "Enter the exact model identifier from OpenRouter",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                } else {
                    // Predefined model dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedModel,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Select Model", color = Color.White.copy(alpha = 0.7f)) },
                            trailingIcon = { 
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedDropdown,
                                    modifier = Modifier
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedBorderColor = Color.White.copy(alpha = 0.3f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                cursorColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(Color(0xFF2B2B2B))
                        ) {
                            // Free models section
                            Text(
                                text = "Free Models",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            
                            OpenRouterService.FREE_MODELS.forEach { model ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = model,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateSelectedModel(model)
                                        expandedDropdown = false
                                    }
                                )
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            )
                            
                            // Paid models section
                            Text(
                                text = "Premium Models",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            
                            OpenRouterService.PAID_MODELS.forEach { model ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = model,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateSelectedModel(model)
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prompt Configuration Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Prompt Configuration",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = uiState.systemPrompt,
                    onValueChange = viewModel::updateSystemPrompt,
                    label = { Text("System Prompt", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Enter the system prompt for the AI assistant", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Text(
                    text = "This prompt defines the AI's role and behavior when rewriting text.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                
                OutlinedTextField(
                    value = uiState.userPromptTemplate,
                    onValueChange = viewModel::updateUserPromptTemplate,
                    label = { Text("User Prompt Template", color = Color.White.copy(alpha = 0.7f)) },
                    placeholder = { Text("Please rewrite the following text: {text}", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Text(
                    text = "Use {text} as a placeholder for the clipboard content. If your clipboard starts with [prompt], that prompt will be used instead of this template.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Usage Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "How to Use",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Copy Re-write Mode
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Copy Re-write Mode (Shift+Control+Command)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "1. Copy text to your clipboard",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "2. Press Shift+Control+Command",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "3. The rewritten text will replace your clipboard content",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "4. Paste the improved text wherever you need it",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tip: Start your copied text with [instruction] to override the default template. E.g., '[translate to greek] hello world'",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
                
                // AI Chat Mode
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "AI Chat Mode (Shift+Control+Space)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Press Shift+Control+Space to open the AI chat window where you can have interactive conversations with the AI assistant.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Error display
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x40FF6B6B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFFF9999),
                    fontSize = 14.sp
                )
            }
        }

        // Status
        val isValid = viewModel.validateSettings()
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isValid) 
                    Color(0x4000FF00)
                else 
                    Color(0x40FFA500)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isValid) "✓ Configuration is valid" else "⚠ Please configure API key and model",
                modifier = Modifier.padding(16.dp),
                color = if (isValid) 
                    Color(0xFF99FF99)
                else 
                    Color(0xFFFFCC99),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}