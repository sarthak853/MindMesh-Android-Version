package com.example.mindmesh.service

import com.example.mindmesh.data.model.CognitiveMap
import com.example.mindmesh.data.model.MapNode
import com.example.mindmesh.data.model.MapEdge
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*
import kotlin.random.Random

class CognitiveMapGenerator(
    private val textProcessor: TextProcessor
) {
    
    suspend fun generateMap(documentId: Long, title: String, content: String): CognitiveMap = withContext(Dispatchers.Default) {
        val keyPhrases = textProcessor.extractKeyPhrases(content)
        val entities = textProcessor.extractEntities(content)
        val sentences = textProcessor.extractSentences(content)
        
        // Combine and rank all concepts
        val allConcepts = mutableListOf<ConceptNode>()
        
        // Add key phrases as concepts
        keyPhrases.forEach { phrase ->
            allConcepts.add(
                ConceptNode(
                    text = phrase.text,
                    importance = phrase.importance,
                    type = "concept",
                    frequency = calculateFrequency(phrase.text, content)
                )
            )
        }
        
        // Add entities as concepts
        entities.forEach { entity ->
            allConcepts.add(
                ConceptNode(
                    text = entity.text,
                    importance = entity.confidence,
                    type = when (entity.type) {
                        EntityType.PERSON_OR_PLACE -> "entity"
                        EntityType.CONCEPT -> "concept"
                        EntityType.NUMBER -> "data"
                        else -> "other"
                    },
                    frequency = calculateFrequency(entity.text, content)
                )
            )
        }
        
        // Remove duplicates and select top concepts
        val uniqueConcepts = allConcepts
            .distinctBy { it.text.lowercase() }
            .sortedByDescending { it.importance * it.frequency }
            .take(20)
        
        // Create hierarchical structure
        val hierarchicalNodes = createHierarchicalStructure(uniqueConcepts, content)
        
        // Generate semantic layout
        val nodes = generateSemanticLayout(hierarchicalNodes)
        
        // Create meaningful edges
        val edges = generateSemanticEdges(nodes, content, sentences)
        
        val gson = Gson()
        CognitiveMap(
            documentId = documentId,
            title = title,
            nodes = gson.toJson(nodes),
            edges = gson.toJson(edges)
        )
    }
    
    private fun createHierarchicalStructure(concepts: List<ConceptNode>, content: String): List<HierarchicalNode> {
        val hierarchical = mutableListOf<HierarchicalNode>()
        
        // Identify main topics (highest importance concepts)
        val mainTopics = concepts.take(5)
        
        // Group related concepts under main topics
        mainTopics.forEach { topic ->
            val relatedConcepts = concepts.filter { concept ->
                concept != topic && areConceptsRelated(topic.text, concept.text, content)
            }.take(4)
            
            hierarchical.add(
                HierarchicalNode(
                    concept = topic,
                    level = 0, // Main topic
                    children = relatedConcepts.map { 
                        HierarchicalNode(it, 1, emptyList()) 
                    }
                )
            )
        }
        
        // Add remaining concepts as independent nodes
        val usedConcepts = hierarchical.flatMap { listOf(it.concept) + it.children.map { child -> child.concept } }
        val remainingConcepts = concepts.filter { it !in usedConcepts }.take(5)
        
        remainingConcepts.forEach { concept ->
            hierarchical.add(HierarchicalNode(concept, 0, emptyList()))
        }
        
        return hierarchical
    }
    
    private fun generateSemanticLayout(hierarchicalNodes: List<HierarchicalNode>): List<MapNode> {
        val nodes = mutableListOf<MapNode>()
        val centerX = 0f
        val centerY = 0f
        
        hierarchicalNodes.forEachIndexed { index, hierarchicalNode ->
            // Position main topics in a circle
            val mainAngle = (2 * PI * index) / hierarchicalNodes.size
            val mainRadius = if (hierarchicalNode.children.isNotEmpty()) 150f else 200f
            val mainX = centerX + (cos(mainAngle) * mainRadius).toFloat()
            val mainY = centerY + (sin(mainAngle) * mainRadius).toFloat()
            
            // Create main node
            nodes.add(
                MapNode(
                    id = "node_${nodes.size}",
                    text = hierarchicalNode.concept.text,
                    x = mainX,
                    y = mainY,
                    importance = hierarchicalNode.concept.importance,
                    category = hierarchicalNode.concept.type
                )
            )
            
            // Position child nodes around their parent
            hierarchicalNode.children.forEachIndexed { childIndex, child ->
                val childAngle = mainAngle + (childIndex - hierarchicalNode.children.size / 2.0) * 0.5
                val childRadius = 80f
                val childX = mainX + (cos(childAngle) * childRadius).toFloat()
                val childY = mainY + (sin(childAngle) * childRadius).toFloat()
                
                nodes.add(
                    MapNode(
                        id = "node_${nodes.size}",
                        text = child.concept.text,
                        x = childX,
                        y = childY,
                        importance = child.concept.importance * 0.8f, // Slightly lower importance for children
                        category = child.concept.type
                    )
                )
            }
        }
        
        return nodes
    }
    
    private fun generateSemanticEdges(nodes: List<MapNode>, content: String, sentences: List<String>): List<MapEdge> {
        val edges = mutableListOf<MapEdge>()
        
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val node1 = nodes[i]
                val node2 = nodes[j]
                
                // Calculate multiple relationship metrics
                val cooccurrence = calculateAdvancedCooccurrence(node1.text, node2.text, sentences)
                val proximity = calculateProximity(node1.text, node2.text, content)
                val semantic = calculateSemanticSimilarity(node1.text, node2.text)
                
                // Combine metrics for edge weight
                val combinedWeight = (cooccurrence * 0.4f + proximity * 0.4f + semantic * 0.2f)
                
                if (combinedWeight > 0.15f) {
                    val relationshipType = determineRelationshipType(node1.text, node2.text, content)
                    
                    edges.add(
                        MapEdge(
                            id = "edge_${i}_$j",
                            sourceId = node1.id,
                            targetId = node2.id,
                            weight = combinedWeight,
                            label = relationshipType
                        )
                    )
                }
            }
        }
        
        return edges.sortedByDescending { it.weight }.take(30) // Limit edges to avoid clutter
    }
    
    private fun calculateFrequency(text: String, content: String): Float {
        val pattern = Regex("\\b${Regex.escape(text)}\\b", RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(content).count()
        return minOf(1.0f, matches / 10.0f)
    }
    
    private fun areConceptsRelated(concept1: String, concept2: String, content: String): Boolean {
        val sentences = content.split(Regex("[.!?]+"))
        var relatedSentences = 0
        
        sentences.forEach { sentence ->
            val lowerSentence = sentence.lowercase()
            if (lowerSentence.contains(concept1.lowercase()) && lowerSentence.contains(concept2.lowercase())) {
                relatedSentences++
            }
        }
        
        return relatedSentences > 0
    }
    
    private fun calculateAdvancedCooccurrence(text1: String, text2: String, sentences: List<String>): Float {
        var cooccurrences = 0
        var proximityScore = 0f
        
        sentences.forEach { sentence ->
            val lowerSentence = sentence.lowercase()
            val pos1 = lowerSentence.indexOf(text1.lowercase())
            val pos2 = lowerSentence.indexOf(text2.lowercase())
            
            if (pos1 != -1 && pos2 != -1) {
                cooccurrences++
                // Closer terms get higher scores
                val distance = abs(pos1 - pos2)
                proximityScore += 1.0f / (1.0f + distance / 50.0f)
            }
        }
        
        return minOf(1.0f, (cooccurrences + proximityScore) / 10.0f)
    }
    
    private fun calculateProximity(text1: String, text2: String, content: String): Float {
        val words = content.lowercase().split(Regex("\\W+"))
        val positions1 = words.mapIndexedNotNull { index, word -> 
            if (word.contains(text1.lowercase())) index else null 
        }
        val positions2 = words.mapIndexedNotNull { index, word -> 
            if (word.contains(text2.lowercase())) index else null 
        }
        
        if (positions1.isEmpty() || positions2.isEmpty()) return 0f
        
        val minDistance = positions1.minOf { pos1 ->
            positions2.minOf { pos2 -> abs(pos1 - pos2) }
        }
        
        return 1.0f / (1.0f + minDistance / 10.0f)
    }
    
    private fun calculateSemanticSimilarity(text1: String, text2: String): Float {
        // Simple semantic similarity based on word overlap and length
        val words1 = text1.lowercase().split(Regex("\\W+")).toSet()
        val words2 = text2.lowercase().split(Regex("\\W+")).toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        val jaccard = if (union > 0) intersection.toFloat() / union else 0f
        
        // Boost similarity for similar length terms
        val lengthSimilarity = 1.0f - abs(text1.length - text2.length) / maxOf(text1.length, text2.length).toFloat()
        
        return (jaccard + lengthSimilarity * 0.3f) / 1.3f
    }
    
    private fun determineRelationshipType(text1: String, text2: String, content: String): String {
        val context = findContextBetweenTerms(text1, text2, content)
        
        return when {
            context.contains(Regex("\\b(is|are|was|were)\\b", RegexOption.IGNORE_CASE)) -> "is-a"
            context.contains(Regex("\\b(has|have|contains|includes)\\b", RegexOption.IGNORE_CASE)) -> "has-a"
            context.contains(Regex("\\b(causes?|leads? to|results? in)\\b", RegexOption.IGNORE_CASE)) -> "causes"
            context.contains(Regex("\\b(part of|component of|element of)\\b", RegexOption.IGNORE_CASE)) -> "part-of"
            context.contains(Regex("\\b(similar to|like|resembles)\\b", RegexOption.IGNORE_CASE)) -> "similar"
            context.contains(Regex("\\b(opposite|different|unlike)\\b", RegexOption.IGNORE_CASE)) -> "opposite"
            else -> "related"
        }
    }
    
    private fun findContextBetweenTerms(text1: String, text2: String, content: String): String {
        val sentences = content.split(Regex("[.!?]+"))
        
        sentences.forEach { sentence ->
            val lowerSentence = sentence.lowercase()
            if (lowerSentence.contains(text1.lowercase()) && lowerSentence.contains(text2.lowercase())) {
                return sentence
            }
        }
        
        return ""
    }
}

data class ConceptNode(
    val text: String,
    val importance: Float,
    val type: String,
    val frequency: Float
)

data class HierarchicalNode(
    val concept: ConceptNode,
    val level: Int,
    val children: List<HierarchicalNode>
)