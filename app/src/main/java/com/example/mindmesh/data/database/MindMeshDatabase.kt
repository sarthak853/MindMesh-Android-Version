package com.example.mindmesh.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.mindmesh.data.dao.*
import com.example.mindmesh.data.model.*

@Database(
    entities = [
        Document::class,
        CognitiveMap::class,
        Flashcard::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MindMeshDatabase : RoomDatabase() {
    
    abstract fun documentDao(): DocumentDao
    abstract fun cognitiveMapDao(): CognitiveMapDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun chatMessageDao(): ChatMessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: MindMeshDatabase? = null
        
        fun getDatabase(context: Context): MindMeshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindMeshDatabase::class.java,
                    "mindmesh_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}