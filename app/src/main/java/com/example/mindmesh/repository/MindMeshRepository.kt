package com.example.mindmesh.repository

import android.content.Context
import android.net.Uri
import com.example.mindmesh.data.database.MindMeshDatabase
import com.example.mindmesh.data.model.*
import com.example.mindmesh.service.*
import kotlinx.coroutines.flow.Flow

class MindMeshRepository(
    private val context: Context,
    private val database: MindMeshDatabase
) {
    private val documentProcessor = DocumentProcessor(context)
    private val textProcessor = TextProcessor(context)
    private val cognitiveMapGenerator = CognitiveMapGenerator(textProcessor)
    private val flashcardGenerator = FlashcardGenerator(textProcessor)
    
    // Document operations
    fun getAllDocuments(): Flow<List<Document>> = database.documentDao().getAllDocuments()
    
    suspend fun getDocumentById(id: Long): Document? = database.documentDao().getDocumentById(id)
    
    suspend fun insertDocument(document: Document): Long = database.documentDao().insertDocument(document)
    
    suspend fun deleteDocument(document: Document) = database.documentDao().deleteDocument(document)
    
    suspend fun processDocumentFromUri(uri: Uri, title: String): Document? {
        val document = documentProcessor.processDocument(uri, title)
        return if (document != null) {
            val id = insertDocument(document)
            document.copy(id = id)
        } else null
    }
    
    suspend fun processYouTubeUrl(url: String): Document? {
        val document = documentProcessor.processYouTubeUrl(url)
        return if (document != null) {
            val id = insertDocument(document)
            document.copy(id = id)
        } else null
    }
    
    suspend fun processDocumentContent(document: Document) {
        // Generate cognitive map
        val cognitiveMap = cognitiveMapGenerator.generateMap(
            document.id, 
            document.title, 
            document.content
        )
        database.cognitiveMapDao().insertMap(cognitiveMap)
        
        // Generate flashcards
        val flashcards = flashcardGenerator.generateFlashcards(
            document.id, 
            document.content
        )
        database.flashcardDao().insertFlashcards(flashcards)
        
        // Mark document as processed
        database.documentDao().updateDocument(document.copy(isProcessed = true))
    }
    
    // Cognitive Map operations
    fun getAllMaps(): Flow<List<CognitiveMap>> = database.cognitiveMapDao().getAllMaps()
    
    suspend fun getMapsByDocumentId(documentId: Long): List<CognitiveMap> = 
        database.cognitiveMapDao().getMapsByDocumentId(documentId)
    
    suspend fun getMapById(id: Long): CognitiveMap? = database.cognitiveMapDao().getMapById(id)
    
    // Flashcard operations
    fun getAllFlashcards(): Flow<List<Flashcard>> = database.flashcardDao().getAllFlashcards()
    
    suspend fun getDueFlashcards(): List<Flashcard> = database.flashcardDao().getDueFlashcards()
    
    suspend fun getDueFlashcardCount(): Int = database.flashcardDao().getDueFlashcardCount()
    
    suspend fun updateFlashcard(flashcard: Flashcard) = database.flashcardDao().updateFlashcard(flashcard)
    
    suspend fun reviewFlashcard(flashcard: Flashcard, result: ReviewResult): Flashcard {
        val newDifficulty = when (result) {
            ReviewResult.AGAIN -> minOf(5, flashcard.difficulty + 1)
            ReviewResult.HARD -> flashcard.difficulty
            ReviewResult.GOOD -> maxOf(0, flashcard.difficulty - 1)
            ReviewResult.EASY -> maxOf(0, flashcard.difficulty - 2)
        }
        
        val nextReviewDate = flashcardGenerator.calculateNextReviewDate(flashcard, result)
        val isCorrect = result == ReviewResult.GOOD || result == ReviewResult.EASY
        
        val updatedFlashcard = flashcard.copy(
            difficulty = newDifficulty,
            nextReviewDate = nextReviewDate,
            reviewCount = flashcard.reviewCount + 1,
            correctCount = if (isCorrect) flashcard.correctCount + 1 else flashcard.correctCount,
            updatedAt = System.currentTimeMillis()
        )
        
        updateFlashcard(updatedFlashcard)
        return updatedFlashcard
    }
    
    // Chat operations
    fun getAllChatMessages(): Flow<List<ChatMessage>> = database.chatMessageDao().getAllMessages()
    
    suspend fun insertChatMessage(message: ChatMessage): Long = 
        database.chatMessageDao().insertMessage(message)
    
    suspend fun clearChatHistory() = database.chatMessageDao().clearAllMessages()
    
    suspend fun sendChatMessage(message: String, apiKey: String, context: String? = null): ChatMessage? {
        // Insert user message
        val userMessage = ChatMessage(message = message, isUser = true, documentContext = context)
        insertChatMessage(userMessage)
        
        // Get AI response
        val aiChatService = AIChatService(apiKey)
        val aiResponse = aiChatService.sendMessage(message, context)
        
        return if (aiResponse != null) {
            insertChatMessage(aiResponse)
            aiResponse
        } else null
    }
}