package com.example.mindmesh.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "cognitive_maps",
    foreignKeys = [ForeignKey(
        entity = Document::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CognitiveMap(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val title: String,
    val nodes: String, // JSON string of nodes
    val edges: String, // JSON string of edges
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class MapNode(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val importance: Float = 0.5f,
    val category: String = "concept"
) : Parcelable

@Parcelize
data class MapEdge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val weight: Float = 1.0f,
    val label: String = ""
) : Parcelable