package com.example.mindmesh.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*
import java.util.Locale

class TextProcessor(private val context: Context) {
    
    // Common stop words to filter out
    private val stopWords = setOf(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
        "from", "up", "about", "into", "through", "during", "before", "after", "above", "below",
        "between", "among", "within", "without", "against", "toward", "upon", "beneath", "beside",
        "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did",
        "will", "would", "could", "should", "may", "might", "must", "can", "shall", "this", "that",
        "these", "those", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them"
    )
    
    suspend fun extractSentences(text: String): List<String> = withContext(Dispatchers.Default) {
        text.split(Regex("[.!?]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 15 }
    }
    
    suspend fun extractKeyPhrases(text: String): List<KeyPhrase> = withContext(Dispatchers.Default) {
        val sentences = extractSentences(text)
        val phrases = mutableListOf<KeyPhrase>()
        
        // Extract noun phrases (2-4 words)
        val nounPhrasePattern = Regex("\\b(?:[A-Z][a-z]*\\s+){0,2}[A-Z][a-z]*\\b")
        sentences.forEach { sentence ->
            nounPhrasePattern.findAll(sentence).forEach { match ->
                val phrase = match.value.trim()
                if (phrase.split("\\s+".toRegex()).size <= 4 && phrase.length > 4) {
                    phrases.add(KeyPhrase(phrase, calculatePhraseImportance(phrase, text)))
                }
            }
        }
        
        // Extract important single words with TF-IDF-like scoring
        val words = text.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 4 && it !in stopWords }
        
        val wordFreq = words.groupBy { it }.mapValues { it.value.size }
        val totalWords = words.size
        
        wordFreq.forEach { (word, freq) ->
            val tf = freq.toDouble() / totalWords
            val importance = tf * ln(totalWords.toDouble() / freq)
            if (importance > 0.01) {
                phrases.add(KeyPhrase(word.replaceFirstChar { 
                    if (it.isLowerCase()) it.uppercaseChar() else it 
                }.toString(), importance.toFloat()))
            }
        }
        
        // Remove duplicates and sort by importance
        phrases.distinctBy { it.text.lowercase() }
            .sortedByDescending { it.importance }
            .take(25)
    }
    
    suspend fun extractEntities(text: String): List<Entity> = withContext(Dispatchers.Default) {
        val entities = mutableListOf<Entity>()
        
        // Extract proper nouns (capitalized sequences)
        val properNounPattern = Regex("\\b[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*\\b")
        properNounPattern.findAll(text).forEach { match ->
            val entity = match.value.trim()
            if (entity.split("\\s+".toRegex()).size <= 3) {
                entities.add(Entity(entity, EntityType.PERSON_OR_PLACE, calculateEntityConfidence(entity, text)))
            }
        }
        
        // Extract technical terms (words with specific patterns)
        val technicalPattern = Regex("\\b[a-z]+(?:-[a-z]+)*(?:ing|tion|sion|ment|ness|ity|ism)\\b", RegexOption.IGNORE_CASE)
        technicalPattern.findAll(text).forEach { match ->
            val term = match.value
            if (term.length > 6) {
                entities.add(Entity(term, EntityType.CONCEPT, 0.7f))
            }
        }
        
        // Extract numbers with context
        val numberPattern = Regex("\\b\\d+(?:\\.\\d+)?(?:\\s*(?:percent|%|million|billion|thousand|years?|days?|hours?|minutes?))?\\b", RegexOption.IGNORE_CASE)
        numberPattern.findAll(text).forEach { match ->
            entities.add(Entity(match.value, EntityType.NUMBER, 0.8f))
        }
        
        // Extract quoted terms
        val quotedPattern = Regex("\"([^\"]+)\"")
        quotedPattern.findAll(text).forEach { match ->
            val quoted = match.groupValues[1]
            if (quoted.length > 3 && quoted.split("\\s+".toRegex()).size <= 4) {
                entities.add(Entity(quoted, EntityType.CONCEPT, 0.9f))
            }
        }
        
        entities.distinctBy { it.text.lowercase() }
            .filter { it.confidence > 0.5f }
            .sortedByDescending { it.confidence }
            .take(20)
    }
    
    private fun calculatePhraseImportance(phrase: String, text: String): Float {
        val occurrences = text.split(Regex("\\W+")).count { 
            phrase.lowercase().contains(it.lowercase()) 
        }
        val length = phrase.split("\\s+".toRegex()).size
        val lengthBonus = when (length) {
            2 -> 1.2f
            3 -> 1.5f
            4 -> 1.3f
            else -> 1.0f
        }
        return minOf(1.0f, (occurrences / 10.0f) * lengthBonus)
    }
    
    private fun calculateEntityConfidence(entity: String, text: String): Float {
        val occurrences = Regex("\\b${Regex.escape(entity)}\\b", RegexOption.IGNORE_CASE)
            .findAll(text).count()
        val isAllCaps = entity.all { it.isUpperCase() || !it.isLetter() }
        val hasMultipleWords = entity.contains(" ")
        
        var confidence = minOf(1.0f, occurrences / 5.0f)
        if (isAllCaps) confidence += 0.2f
        if (hasMultipleWords) confidence += 0.3f
        
        return minOf(1.0f, confidence)
    }
    
    suspend fun generateEmbeddings(text: String): FloatArray = withContext(Dispatchers.Default) {
        val words = text.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() && it !in stopWords }
        val embedding = FloatArray(384)
        
        // Create a more sophisticated embedding based on word co-occurrence
        words.forEachIndexed { index, word ->
            val hash = word.hashCode()
            val position = abs(hash) % embedding.size
            
            // Add positional encoding
            val positionalWeight = 1.0f / (1.0f + index * 0.1f)
            embedding[position] += positionalWeight
            
            // Add neighboring positions for smoother representation
            if (position > 0) embedding[position - 1] += positionalWeight * 0.5f
            if (position < embedding.size - 1) embedding[position + 1] += positionalWeight * 0.5f
        }
        
        // Normalize
        val norm = sqrt(embedding.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] /= norm
            }
        }
        
        embedding
    }
}

data class KeyPhrase(
    val text: String,
    val importance: Float
)

data class Entity(
    val text: String,
    val type: EntityType,
    val confidence: Float = 1.0f
)

enum class EntityType {
    PERSON_OR_PLACE, NUMBER, CONCEPT, OTHER
}