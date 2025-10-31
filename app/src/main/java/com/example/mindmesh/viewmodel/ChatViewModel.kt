package com.example.mindmesh.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mindmesh.data.database.MindMeshDatabase
import com.example.mindmesh.data.model.ChatMessage
import com.example.mindmesh.repository.MindMeshRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: MindMeshRepository
    private val sharedPreferences = application.getSharedPreferences("mindmesh_prefs", Context.MODE_PRIVATE)
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        val database = MindMeshDatabase.getDatabase(application)
        repository = MindMeshRepository(application, database)
    }
    
    val chatMessages: Flow<List<ChatMessage>> = repository.getAllChatMessages()
    
    fun sendMessage(message: String, context: String? = null) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            _error.value = "Please set your Gemini API key in settings"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = repository.sendChatMessage(message, apiKey, context)
                if (response == null) {
                    _error.value = "Failed to get AI response. Check your internet connection and API key."
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                repository.clearChatHistory()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun setApiKey(apiKey: String) {
        sharedPreferences.edit()
            .putString("gemini_api_key", apiKey)
            .apply()
    }
    
    fun getApiKey(): String {
        return sharedPreferences.getString("gemini_api_key", "") ?: ""
    }
    
    fun clearError() {
        _error.value = null
    }
}