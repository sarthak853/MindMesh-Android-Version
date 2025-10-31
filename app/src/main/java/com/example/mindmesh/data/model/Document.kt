package com.example.mindmesh.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val filePath: String? = null,
    val fileType: DocumentType,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false
) : Parcelable

enum class DocumentType {
    PDF, DOCX, VIDEO, YOUTUBE, TEXT
}