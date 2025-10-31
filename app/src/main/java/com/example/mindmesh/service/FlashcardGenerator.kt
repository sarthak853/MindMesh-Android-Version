package com.example.mindmesh.service

import com.example.mindmesh.data.model.Flashcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlashcardGenerator(
    private val textProcessor: TextProcessor
) {
    
    suspend fun generateFlashcards(documentId: Long, content: String): List<Flashcard> = withContext(Dispatchers.Default) {
        val sentences = textProcessor.extractSentences(content)
        val keyPhrases = textProcessor.extractKeyPhrases(content)
        val entities = textProcessor.extractEntities(content)
        
        val flashcards = mutableListOf<Flashcard>()
        
        // Generate definition-based flashcards from key phrases
        keyPhrases.take(10).forEach { phrase ->
            val context = findContextForPhrase(phrase.text, sentences)
            if (context.isNotEmpty()) {
                flashcards.add(
                    Flashcard(
                        documentId = documentId,
                        front = "What is ${phrase.text}?",
                        back = context,
                        difficulty = 0
                    )
                )
            }
        }
        
        // Generate fill-in-the-blank flashcards
        sentences.take(5).forEach { sentence ->
            keyPhrases.forEach { phrase ->
                if (sentence.contains(phrase.text, ignoreCase = true)) {
                    val front = sentence.replace(phrase.text, "______", ignoreCase = true)
                    flashcards.add(
                        Flashcard(
                            documentId = documentId,
                            front = "Fill in the blank: $front",
                            back = phrase.text,
                            difficulty = 0
                        )
                    )
                    return@forEach // Only one replacement per sentence
                }
            }
        }
        
        // Generate entity-based flashcards
        entities.take(5).forEach { entity ->
            val context = findContextForPhrase(entity.text, sentences)
            if (context.isNotEmpty()) {
                flashcards.add(
                    Flashcard(
                        documentId = documentId,
                        front = "What do you know about ${entity.text}?",
                        back = context,
                        difficulty = 0
                    )
                )
            }
        }
        
        flashcards.distinctBy { it.front to it.back }
    }
    
    private fun findContextForPhrase(phrase: String, sentences: List<String>): String {
        return sentences.find { 
            it.contains(phrase, ignoreCase = true) && it.length > phrase.length + 10 
        } ?: ""
    }
    
    fun calculateNextReviewDate(flashcard: Flashcard, result: com.example.mindmesh.data.model.ReviewResult): Long {
        val baseInterval = when (result) {
            com.example.mindmesh.data.model.ReviewResult.AGAIN -> 1
            com.example.mindmesh.data.model.ReviewResult.HARD -> 2
            com.example.mindmesh.data.model.ReviewResult.GOOD -> 4
            com.example.mindmesh.data.model.ReviewResult.EASY -> 7
        }
        
        val multiplier = when (flashcard.difficulty) {
            0, 1 -> 1.0
            2 -> 1.5
            3 -> 2.0
            4 -> 3.0
            else -> 4.0
        }
        
        val daysToAdd = (baseInterval * multiplier).toLong()
        return System.currentTimeMillis() + (daysToAdd * 24 * 60 * 60 * 1000)
    }
}