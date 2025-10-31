package com.example.mindmesh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindmesh.data.database.MindMeshDatabase
import com.example.mindmesh.data.model.CognitiveMap
import com.example.mindmesh.data.model.MapNode
import com.example.mindmesh.data.model.MapEdge
import com.example.mindmesh.repository.MindMeshRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.runtime.collectAsState
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CognitiveMapsScreen() {
    val context = LocalContext.current
    val database = MindMeshDatabase.getDatabase(context)
    val repository = MindMeshRepository(context, database)
    
    val maps by repository.getAllMaps().collectAsState(initial = emptyList())
    var selectedMap by remember { mutableStateOf<CognitiveMap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cognitive Maps",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (selectedMap != null) {
                IconButton(onClick = { selectedMap = null }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back to list")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedMap == null) {
            // Maps list
            if (maps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.AccountTree,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No cognitive maps yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upload documents to generate cognitive maps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(maps) { map ->
                        MapCard(
                            map = map,
                            onClick = { selectedMap = map }
                        )
                    }
                }
            }
        } else {
            // Map viewer
            MapViewer(map = selectedMap!!)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapCard(
    map: CognitiveMap,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = map.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val gson = Gson()
            val nodeType = object : TypeToken<List<MapNode>>() {}.type
            val nodes: List<MapNode> = try {
                gson.fromJson(map.nodes, nodeType)
            } catch (e: Exception) {
                emptyList()
            }
            
            Text(
                text = "${nodes.size} concepts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Created: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(map.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MapViewer(map: CognitiveMap) {
    val gson = Gson()
    
    val nodes: List<MapNode> = try {
        val nodeType = object : TypeToken<List<MapNode>>() {}.type
        gson.fromJson(map.nodes, nodeType)
    } catch (e: Exception) {
        emptyList()
    }
    
    val edges: List<MapEdge> = try {
        val edgeType = object : TypeToken<List<MapEdge>>() {}.type
        gson.fromJson(map.edges, edgeType)
    } catch (e: Exception) {
        emptyList()
    }
    
    Column {
        Text(
            text = map.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Interactive Map
        InteractiveCognitiveMap(
            nodes = nodes,
            edges = edges,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enhanced Legend and Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Statistics Card
            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Concepts: ${nodes.size}")
                    Text("Connections: ${edges.size}")
                    Text("Categories: ${nodes.map { it.category }.distinct().size}")
                }
            }
            
            // Legend Card
            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Legend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem("Concept", Color.Blue)
                    LegendItem("Entity", Color.Green)
                    LegendItem("Data", Color(0xFFFFA500))
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun InteractiveCognitiveMap(
    nodes: List<MapNode>,
    edges: List<MapEdge>,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<MapNode?>(null) }
    var highlightedNodes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showNodeDetails by remember { mutableStateOf(false) }
    
    // Animation states
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val animatedOffset by animateOffsetAsState(
        targetValue = offset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-size.width.toFloat(), size.width.toFloat()),
                                y = (offset.y + pan.y).coerceIn(-size.height.toFloat(), size.height.toFloat())
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val tappedNode = findNodeAtPosition(
                                tapOffset, nodes, size.width.toFloat(), size.height.toFloat(),
                                animatedScale, animatedOffset
                            )
                            
                            if (tappedNode != null) {
                                selectedNode = tappedNode
                                showNodeDetails = true
                                
                                // Highlight connected nodes
                                val connectedNodeIds = edges.filter { edge ->
                                    edge.sourceId == tappedNode.id || edge.targetId == tappedNode.id
                                }.flatMap { edge ->
                                    listOf(edge.sourceId, edge.targetId)
                                }.toSet()
                                
                                highlightedNodes = connectedNodeIds + tappedNode.id
                            } else {
                                selectedNode = null
                                highlightedNodes = emptySet()
                            }
                        }
                    }
            ) {
                drawInteractiveCognitiveMap(
                    nodes = nodes,
                    edges = edges,
                    canvasWidth = size.width,
                    canvasHeight = size.height,
                    scale = animatedScale,
                    offset = animatedOffset,
                    selectedNode = selectedNode,
                    highlightedNodes = highlightedNodes
                )
            }
            
            // Control buttons
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In
                FloatingActionButton(
                    onClick = { scale = (scale * 1.2f).coerceAtMost(3f) },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In")
                }
                
                // Zoom Out
                FloatingActionButton(
                    onClick = { scale = (scale / 1.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out")
                }
                
                // Reset View
                FloatingActionButton(
                    onClick = { 
                        scale = 1f
                        offset = Offset.Zero
                        selectedNode = null
                        highlightedNodes = emptySet()
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Filled.CenterFocusStrong, contentDescription = "Reset View")
                }
            }
            
            // Node count indicator
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Zoom: ${(scale * 100).toInt()}%",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    
    // Node Details Dialog
    if (showNodeDetails && selectedNode != null) {
        NodeDetailsDialog(
            node = selectedNode!!,
            connectedNodes = nodes.filter { it.id in highlightedNodes && it.id != selectedNode!!.id },
            edges = edges.filter { edge ->
                edge.sourceId == selectedNode!!.id || edge.targetId == selectedNode!!.id
            },
            onDismiss = { 
                showNodeDetails = false
                selectedNode = null
                highlightedNodes = emptySet()
            }
        )
    }
}

@Composable
fun NodeDetailsDialog(
    node: MapNode,
    connectedNodes: List<MapNode>,
    edges: List<MapEdge>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = node.text,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Node Properties
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Properties",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PropertyRow("Category", node.category.replaceFirstChar { it.uppercase() })
                        PropertyRow("Importance", "${(node.importance * 100).toInt()}%")
                        PropertyRow("Connections", "${edges.size}")
                    }
                }
                
                if (connectedNodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Connected Concepts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(connectedNodes.take(10)) { connectedNode ->
                            val edge = edges.find { 
                                (it.sourceId == node.id && it.targetId == connectedNode.id) ||
                                (it.targetId == node.id && it.sourceId == connectedNode.id)
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                when (connectedNode.category) {
                                                    "concept" -> Color.Blue
                                                    "entity" -> Color.Green
                                                    "data" -> Color(0xFFFFA500)
                                                    else -> Color.Gray
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = connectedNode.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (edge != null && edge.label.isNotEmpty()) {
                                            Text(
                                                text = "Relationship: ${edge.label}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

fun findNodeAtPosition(
    tapOffset: Offset,
    nodes: List<MapNode>,
    canvasWidth: Float,
    canvasHeight: Float,
    scale: Float,
    offset: Offset
): MapNode? {
    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2
    val mapScale = minOf(canvasWidth, canvasHeight) / 600f * scale
    
    return nodes.find { node ->
        val nodeX = centerX + (node.x * mapScale) + offset.x
        val nodeY = centerY + (node.y * mapScale) + offset.y
        val radius = (25f + node.importance * 25f) * scale
        
        val distance = sqrt((tapOffset.x - nodeX).pow(2) + (tapOffset.y - nodeY).pow(2))
        distance <= radius
    }
}

fun DrawScope.drawInteractiveCognitiveMap(
    nodes: List<MapNode>,
    edges: List<MapEdge>,
    canvasWidth: Float,
    canvasHeight: Float,
    scale: Float,
    offset: Offset,
    selectedNode: MapNode?,
    highlightedNodes: Set<String>
) {
    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2
    val mapScale = minOf(canvasWidth, canvasHeight) / 600f * scale
    
    // Draw background grid for better spatial reference
    drawGrid(canvasWidth, canvasHeight, scale, offset)
    
    // Draw edges first (so they appear behind nodes)
    edges.forEach { edge ->
        val sourceNode = nodes.find { it.id == edge.sourceId }
        val targetNode = nodes.find { it.id == edge.targetId }
        
        if (sourceNode != null && targetNode != null) {
            val startX = centerX + (sourceNode.x * mapScale) + offset.x
            val startY = centerY + (sourceNode.y * mapScale) + offset.y
            val endX = centerX + (targetNode.x * mapScale) + offset.x
            val endY = centerY + (targetNode.y * mapScale) + offset.y
            
            // Highlight edges connected to selected node
            val isHighlighted = highlightedNodes.contains(sourceNode.id) && highlightedNodes.contains(targetNode.id)
            val alpha = if (isHighlighted) 1.0f else if (highlightedNodes.isNotEmpty()) 0.3f else 0.7f
            
            // Edge color based on relationship type
            val baseColor = when (edge.label) {
                "is-a" -> Color.Blue
                "has-a" -> Color.Green
                "causes" -> Color.Red
                "part-of" -> Color.Magenta
                "similar" -> Color.Cyan
                "opposite" -> Color.Yellow
                else -> Color.Gray
            }
            
            val edgeColor = baseColor.copy(alpha = alpha)
            val strokeWidth = ((edge.weight * 4f + 1f) * scale).coerceIn(1f, 8f)
            
            drawLine(
                color = edgeColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth
            )
            
            // Draw arrowhead for directed relationships
            if (edge.label in listOf("is-a", "has-a", "causes", "part-of")) {
                drawArrowhead(startX, startY, endX, endY, edgeColor, scale)
            }
            
            // Draw edge label for highlighted edges
            if (isHighlighted && edge.label.isNotEmpty()) {
                val midX = (startX + endX) / 2
                val midY = (startY + endY) / 2
                drawEdgeLabel(edge.label, midX, midY, edgeColor)
            }
        }
    }
    
    // Draw nodes
    nodes.forEach { node ->
        val x = centerX + (node.x * mapScale) + offset.x
        val y = centerY + (node.y * mapScale) + offset.y
        val baseRadius = 25f * scale
        val radius = baseRadius + (node.importance * 25f * scale)
        
        // Highlight logic
        val isSelected = selectedNode?.id == node.id
        val isHighlighted = highlightedNodes.contains(node.id)
        val alpha = when {
            isSelected -> 1.0f
            isHighlighted -> 0.9f
            highlightedNodes.isNotEmpty() -> 0.4f
            else -> 0.8f
        }
        
        // Node color based on category and state
        val baseColor = when (node.category) {
            "concept" -> Color.Blue
            "entity" -> Color.Green
            "data" -> Color(0xFFFFA500)
            else -> Color.Gray
        }
        
        val nodeColor = baseColor.copy(alpha = alpha)
        
        // Draw selection ring for selected node
        if (isSelected) {
            drawCircle(
                color = Color.White,
                radius = radius + 8f,
                center = Offset(x, y),
                style = Stroke(width = 4f)
            )
            drawCircle(
                color = baseColor,
                radius = radius + 8f,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
        }
        
        // Draw node shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.2f * alpha),
            radius = radius,
            center = Offset(x + 3f * scale, y + 3f * scale)
        )
        
        // Draw main node circle
        drawCircle(
            color = nodeColor,
            radius = radius,
            center = Offset(x, y)
        )
        
        // Draw node border
        val borderWidth = (2f + node.importance * 2f) * scale
        val borderColor = if (isSelected) Color.White else Color.Black
        drawCircle(
            color = borderColor.copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y),
            style = Stroke(width = borderWidth)
        )
        
        // Draw importance indicator
        if (node.importance > 0.7f) {
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius * 0.3f,
                center = Offset(x, y)
            )
        }
        
        // Draw node text for larger nodes or selected nodes
        if (radius > 30f || isSelected || isHighlighted) {
            drawNodeText(node.text, x, y, radius, scale, alpha)
        }
    }
}

fun DrawScope.drawGrid(
    canvasWidth: Float,
    canvasHeight: Float,
    scale: Float,
    offset: Offset
) {
    val gridSize = 50f * scale
    val gridColor = Color.Gray.copy(alpha = 0.1f)
    
    // Vertical lines
    var x = (offset.x % gridSize) - gridSize
    while (x < canvasWidth) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, canvasHeight),
            strokeWidth = 1f
        )
        x += gridSize
    }
    
    // Horizontal lines
    var y = (offset.y % gridSize) - gridSize
    while (y < canvasHeight) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = 1f
        )
        y += gridSize
    }
}

fun DrawScope.drawNodeText(
    text: String,
    x: Float,
    y: Float,
    radius: Float,
    scale: Float,
    alpha: Float
) {
    val textSize = (12f * scale).coerceIn(8f, 16f)
    val maxWidth = (radius * 1.8f).coerceAtLeast(60f)
    
    // Truncate text if too long
    val displayText = if (text.length > 15) "${text.take(12)}..." else text
    
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            this.textSize = textSize
            this.color = Color.White.copy(alpha = alpha).toArgb()
            this.textAlign = android.graphics.Paint.Align.CENTER
            this.isFakeBoldText = true
        }
        
        // Draw text background
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(displayText, 0, displayText.length, textBounds)
        
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f * alpha),
            topLeft = Offset(
                x - textBounds.width() / 2f - 4f,
                y - textBounds.height() / 2f - 2f
            ),
            size = androidx.compose.ui.geometry.Size(
                textBounds.width() + 8f,
                textBounds.height() + 4f
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
        
        // Draw text
        canvas.nativeCanvas.drawText(
            displayText,
            x,
            y + textBounds.height() / 2f,
            paint
        )
    }
}

fun DrawScope.drawEdgeLabel(
    label: String,
    x: Float,
    y: Float,
    color: Color
) {
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            textSize = 10f
            this.color = color.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(label, 0, label.length, textBounds)
        
        // Draw background
        drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(
                x - textBounds.width() / 2f - 3f,
                y - textBounds.height() / 2f - 1f
            ),
            size = androidx.compose.ui.geometry.Size(
                textBounds.width() + 6f,
                textBounds.height() + 2f
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
        )
        
        canvas.nativeCanvas.drawText(
            label,
            x,
            y + textBounds.height() / 2f,
            paint
        )
    }
}

fun DrawScope.drawArrowhead(
    startX: Float, startY: Float,
    endX: Float, endY: Float,
    color: Color,
    scale: Float = 1f
) {
    val arrowLength = 15f * scale
    val arrowAngle = 0.5f
    
    val angle = atan2(endY - startY, endX - startX)
    val arrowX1 = endX - arrowLength * cos(angle - arrowAngle)
    val arrowY1 = endY - arrowLength * sin(angle - arrowAngle)
    val arrowX2 = endX - arrowLength * cos(angle + arrowAngle)
    val arrowY2 = endY - arrowLength * sin(angle + arrowAngle)
    
    // Draw arrowhead lines
    val strokeWidth = (2f * scale).coerceAtLeast(1f)
    drawLine(
        color = color,
        start = Offset(endX, endY),
        end = Offset(arrowX1, arrowY1),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(endX, endY),
        end = Offset(arrowX2, arrowY2),
        strokeWidth = strokeWidth
    )
}

// Animation helper functions
@Composable
fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset> = spring()
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec
    )
}