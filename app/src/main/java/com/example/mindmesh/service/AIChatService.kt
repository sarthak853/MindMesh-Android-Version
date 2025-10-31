package com.example.mindmesh.service

import com.example.mindmesh.data.model.ChatMessage
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIChatService(private val apiKey: String) {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(GeminiAPI::class.java)
    
    suspend fun sendMessage(
        message: String, 
        context: String? = null
    ): ChatMessage? = withContext(Dispatchers.IO) {
        try {
            val prompt = if (context != null) {
                "Context: $context\n\nUser question: $message"
            } else {
                message
            }
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            val response = api.generateContent(
                apiKey = apiKey,
                request = request
            )
            
            if (response.isSuccessful) {
                val geminiResponse = response.body()
                val responseText = geminiResponse?.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                
                if (responseText != null) {
                    ChatMessage(
                        message = responseText,
                        isUser = false,
                        documentContext = context
                    )
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

interface GeminiAPI {
    @POST("v1beta/models/gemini-1.5-flash-latest:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)