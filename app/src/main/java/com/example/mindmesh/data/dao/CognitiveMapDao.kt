package com.example.mindmesh.data.dao

import androidx.room.*
import com.example.mindmesh.data.model.CognitiveMap
import kotlinx.coroutines.flow.Flow

@Dao
interface CognitiveMapDao {
    @Query("SELECT * FROM cognitive_maps ORDER BY updatedAt DESC")
    fun getAllMaps(): Flow<List<CognitiveMap>>
    
    @Query("SELECT * FROM cognitive_maps WHERE documentId = :documentId")
    suspend fun getMapsByDocumentId(documentId: Long): List<CognitiveMap>
    
    @Query("SELECT * FROM cognitive_maps WHERE id = :id")
    suspend fun getMapById(id: Long): CognitiveMap?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMap(map: CognitiveMap): Long
    
    @Update
    suspend fun updateMap(map: CognitiveMap)
    
    @Delete
    suspend fun deleteMap(map: CognitiveMap)
    
    @Query("DELETE FROM cognitive_maps WHERE documentId = :documentId")
    suspend fun deleteMapsByDocumentId(documentId: Long)
}