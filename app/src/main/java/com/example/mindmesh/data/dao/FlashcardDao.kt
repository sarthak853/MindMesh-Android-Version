package com.example.mindmesh.data.dao

import androidx.room.*
import com.example.mindmesh.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY nextReviewDate ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>
    
    @Query("SELECT * FROM flashcards WHERE documentId = :documentId")
    suspend fun getFlashcardsByDocumentId(documentId: Long): List<Flashcard>
    
    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    suspend fun getDueFlashcards(currentTime: Long = System.currentTimeMillis()): List<Flashcard>
    
    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getFlashcardById(id: Long): Flashcard?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)
    
    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)
    
    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)
    
    @Query("DELETE FROM flashcards WHERE documentId = :documentId")
    suspend fun deleteFlashcardsByDocumentId(documentId: Long)
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewDate <= :currentTime")
    suspend fun getDueFlashcardCount(currentTime: Long = System.currentTimeMillis()): Int
}