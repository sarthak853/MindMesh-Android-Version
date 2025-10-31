package com.example.mindmesh.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mindmesh.data.database.MindMeshDatabase
import com.example.mindmesh.data.model.Document
import com.example.mindmesh.repository.MindMeshRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: MindMeshRepository
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        val database = MindMeshDatabase.getDatabase(application)
        repository = MindMeshRepository(application, database)
    }
    
    val documents: Flow<List<Document>> = repository.getAllDocuments()
    
    fun uploadDocument(uri: Uri, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val document = repository.processDocumentFromUri(uri, title)
                if (document != null) {
                    // Process document content in background
                    repository.processDocumentContent(document)
                } else {
                    _error.value = "Failed to process document"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addYouTubeVideo(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val document = repository.processYouTubeUrl(url)
                if (document != null) {
                    // Process document content in background
                    repository.processDocumentContent(document)
                } else {
                    _error.value = "Failed to process YouTube URL"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            try {
                repository.deleteDocument(document)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}