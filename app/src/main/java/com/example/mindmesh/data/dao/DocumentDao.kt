package com.example.mindmesh.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.example.mindmesh.data.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?
    
    @Query("SELECT * FROM documents WHERE isProcessed = 0")
    suspend fun getUnprocessedDocuments(): List<Document>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long
    
    @Update
    suspend fun updateDocument(document: Document)
    
    @Delete
    suspend fun deleteDocument(document: Document)
    
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
    
    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int
}