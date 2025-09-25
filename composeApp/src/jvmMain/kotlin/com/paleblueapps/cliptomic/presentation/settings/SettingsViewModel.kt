package com.paleblueapps.cliptomic.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paleblueapps.cliptomic.services.OpenRouterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.prefs.Preferences

data class SettingsUiState(
    val apiKey: String = "",
    val selectedModel: String = OpenRouterService.FREE_MODELS.first(),
    val customModel: String = "",
    val useCustomModel: Boolean = false,
    val systemPrompt: String = "You are a helpful assistant that rewrites text to improve clarity, grammar, and style while maintaining the original meaning. Respond only with the rewritten text, no additional commentary, no additional formatting.",
    val userPromptTemplate: String = "Please rewrite the following text: {text}",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel : ViewModel() {
    private val preferences = Preferences.userNodeForPackage(SettingsViewModel::class.java)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val apiKey = preferences.get(PREF_API_KEY, "")
        val selectedModel = preferences.get(PREF_SELECTED_MODEL, OpenRouterService.FREE_MODELS.first())
        val customModel = preferences.get(PREF_CUSTOM_MODEL, "")
        val useCustomModel = preferences.getBoolean(PREF_USE_CUSTOM_MODEL, false)
        val systemPrompt = preferences.get(PREF_SYSTEM_PROMPT, "You are a helpful assistant that rewrites text to improve clarity, grammar, and style while maintaining the original meaning. Respond only with the rewritten text, no additional commentary, no additional formatting.")
        val userPromptTemplate = preferences.get(PREF_USER_PROMPT_TEMPLATE, "Please rewrite the following text: {text}")
        
        _uiState.value = _uiState.value.copy(
            apiKey = apiKey,
            selectedModel = selectedModel,
            customModel = customModel,
            useCustomModel = useCustomModel,
            systemPrompt = systemPrompt,
            userPromptTemplate = userPromptTemplate
        )
    }
    
    fun updateApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(apiKey = apiKey)
        preferences.put(PREF_API_KEY, apiKey)
    }
    
    fun updateSelectedModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        preferences.put(PREF_SELECTED_MODEL, model)
    }
    
    fun updateCustomModel(customModel: String) {
        _uiState.value = _uiState.value.copy(customModel = customModel)
        preferences.put(PREF_CUSTOM_MODEL, customModel)
    }
    
    fun updateUseCustomModel(useCustomModel: Boolean) {
        _uiState.value = _uiState.value.copy(useCustomModel = useCustomModel)
        preferences.putBoolean(PREF_USE_CUSTOM_MODEL, useCustomModel)
    }
    
    fun updateSystemPrompt(systemPrompt: String) {
        _uiState.value = _uiState.value.copy(systemPrompt = systemPrompt)
        preferences.put(PREF_SYSTEM_PROMPT, systemPrompt)
    }
    
    fun updateUserPromptTemplate(userPromptTemplate: String) {
        _uiState.value = _uiState.value.copy(userPromptTemplate = userPromptTemplate)
        preferences.put(PREF_USER_PROMPT_TEMPLATE, userPromptTemplate)
    }
    
    fun getCurrentModel(): String {
        val state = _uiState.value
        return if (state.useCustomModel && state.customModel.isNotBlank()) {
            state.customModel
        } else {
            state.selectedModel
        }
    }
    
    fun getCurrentApiKey(): String {
        return _uiState.value.apiKey
    }
    
    fun getCurrentSystemPrompt(): String {
        return _uiState.value.systemPrompt
    }
    
    fun getCurrentUserPromptTemplate(): String {
        return _uiState.value.userPromptTemplate
    }
    
    fun validateSettings(): Boolean {
        val state = _uiState.value
        return state.apiKey.isNotBlank() && 
               ((!state.useCustomModel && state.selectedModel.isNotBlank()) ||
                (state.useCustomModel && state.customModel.isNotBlank()))
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
    
    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
    
    companion object {
        private const val PREF_API_KEY = "api_key"
        private const val PREF_SELECTED_MODEL = "selected_model"
        private const val PREF_CUSTOM_MODEL = "custom_model"
        private const val PREF_USE_CUSTOM_MODEL = "use_custom_model"
        private const val PREF_SYSTEM_PROMPT = "system_prompt"
        private const val PREF_USER_PROMPT_TEMPLATE = "user_prompt_template"
    }
}