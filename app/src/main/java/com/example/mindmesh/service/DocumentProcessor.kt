package com.example.mindmesh.service

import android.content.Context
import android.net.Uri
import com.example.mindmesh.data.model.Document
import com.example.mindmesh.data.model.DocumentType
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentProcessor(private val context: Context) {
    
    suspend fun processDocument(uri: Uri, title: String): Document? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val mimeType = contentResolver.getType(uri)
            
            val (content, documentType) = when {
                mimeType?.contains("pdf") == true -> {
                    extractPdfText(inputStream!!) to DocumentType.PDF
                }
                mimeType?.contains("document") == true || 
                mimeType?.contains("wordprocessingml") == true -> {
                    extractDocxText(inputStream!!) to DocumentType.DOCX
                }
                else -> {
                    inputStream?.bufferedReader()?.use { it.readText() } to DocumentType.TEXT
                }
            }
            
            inputStream?.close()
            
            if (content != null) {
                Document(
                    title = title,
                    content = content,
                    filePath = uri.toString(),
                    fileType = documentType,
                    isProcessed = false
                )
            } else null
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun extractPdfText(inputStream: InputStream): String? {
        return try {
            val pdfDocument = PdfDocument(PdfReader(inputStream))
            val text = StringBuilder()
            
            for (i in 1..pdfDocument.numberOfPages) {
                text.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)))
                text.append("\n")
            }
            
            pdfDocument.close()
            text.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun extractDocxText(inputStream: InputStream): String? {
        return try {
            val document = XWPFDocument(inputStream)
            val text = StringBuilder()
            
            document.paragraphs.forEach { paragraph ->
                text.append(paragraph.text)
                text.append("\n")
            }
            
            document.close()
            text.toString().trim()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun processYouTubeUrl(url: String): Document? = withContext(Dispatchers.IO) {
        // Placeholder for YouTube processing
        // In a real implementation, you would use YouTube API or extraction library
        try {
            val title = extractYouTubeTitle(url)
            Document(
                title = title,
                content = "YouTube video processing not implemented yet",
                filePath = url,
                fileType = DocumentType.YOUTUBE,
                isProcessed = false
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractYouTubeTitle(url: String): String {
        // Extract video ID and create a basic title
        val videoId = url.substringAfter("v=").substringBefore("&")
        return "YouTube Video: $videoId"
    }
}