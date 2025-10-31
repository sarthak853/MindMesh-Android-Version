package com.example.mindmesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mindmesh.data.database.MindMeshDatabase
import com.example.mindmesh.data.model.Flashcard
import com.example.mindmesh.data.model.ReviewResult
import com.example.mindmesh.repository.MindMeshRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: MindMeshRepository
    
    private val _currentFlashcard = MutableLiveData<Flashcard?>()
    val currentFlashcard: LiveData<Flashcard?> = _currentFlashcard
    
    private val _showAnswer = MutableLiveData<Boolean>()
    val showAnswer: LiveData<Boolean> = _showAnswer
    
    private val _dueCount = MutableLiveData<Int>()
    val dueCount: LiveData<Int> = _dueCount
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private var dueFlashcards = listOf<Flashcard>()
    private var currentIndex = 0
    
    init {
        val database = MindMeshDatabase.getDatabase(application)
        repository = MindMeshRepository(application, database)
        loadDueFlashcards()
    }
    
    val allFlashcards: Flow<List<Flashcard>> = repository.getAllFlashcards()
    
    private fun loadDueFlashcards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dueFlashcards = repository.getDueFlashcards()
                _dueCount.value = dueFlashcards.size
                currentIndex = 0
                loadCurrentFlashcard()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadCurrentFlashcard() {
        if (currentIndex < dueFlashcards.size) {
            _currentFlashcard.value = dueFlashcards[currentIndex]
            _showAnswer.value = false
        } else {
            _currentFlashcard.value = null
        }
    }
    
    fun showAnswer() {
        _showAnswer.value = true
    }
    
    fun reviewFlashcard(result: ReviewResult) {
        val flashcard = _currentFlashcard.value ?: return
        
        viewModelScope.launch {
            try {
                repository.reviewFlashcard(flashcard, result)
                currentIndex++
                loadCurrentFlashcard()
                
                // Update due count
                val newDueCount = repository.getDueFlashcardCount()
                _dueCount.value = newDueCount
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun refreshDueFlashcards() {
        loadDueFlashcards()
    }
    
    fun hasMoreFlashcards(): Boolean {
        return currentIndex < dueFlashcards.size
    }
    
    fun getProgress(): Pair<Int, Int> {
        return Pair(currentIndex + 1, dueFlashcards.size)
    }
}