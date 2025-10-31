package com.example.mindmesh.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(
        entity = Document::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long,
    val front: String,
    val back: String,
    val difficulty: Int = 0, // 0-5 scale
    val nextReviewDate: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class ReviewResult {
    AGAIN, HARD, GOOD, EASY
}