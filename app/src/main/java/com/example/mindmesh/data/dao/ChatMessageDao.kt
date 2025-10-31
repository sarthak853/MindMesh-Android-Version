package com.example.mindmesh.data.dao

import androidx.room.*
import com.example.mindmesh.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>
    
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int = 50): List<ChatMessage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long
    
    @Delete
    suspend fun deleteMessage(message: ChatMessage)
    
    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
    
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}