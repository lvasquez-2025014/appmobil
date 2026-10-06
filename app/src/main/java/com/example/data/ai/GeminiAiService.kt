package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "GeminiAiService"
private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val imageUrl: String? = null,
    val sources: List<String> = emptyList()
)

data class AiGenerationResult(
    val success: Boolean,
    val text: String = "",
    val mediaUrl: String? = null,
    val mimeType: String? = null,
    val sources: List<String> = emptyList(),
    val errorMessage: String? = null,
    val modelUsed: String = ""
)

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    /**
     * Multi-turn chat using gemini-3.5-flash, gemini-3.1-pro-preview or gemini-3.1-flash-lite
     */
    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        systemInstruction: String = "Eres un asistente amigable y experto para la aplicación móvil Espacio.",
        model: String = "gemini-3.5-flash"
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            // System instruction
            if (systemInstruction.isNotBlank()) {
                val sysPart = JSONObject().put("text", systemInstruction)
                val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
                json.put("systemInstruction", sysContent)
            }

            // Contents array
            val contentsArray = JSONArray()
            history.forEach { msg ->
                val part = JSONObject().put("text", msg.text)
                val parts = JSONArray().put(part)
                val contentObj = JSONObject()
                    .put("role", if (msg.role == "user") "user" else "model")
                    .put("parts", parts)
                contentsArray.put(contentObj)
            }
            json.put("contents", contentsArray)

            val response = executePost(endpoint, json.toString())
            parseTextResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Chat failed with model $model", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al conectar con Gemini",
                modelUsed = model
            )
        }
    }

    /**
     * Search Grounding using gemini-3.5-flash with googleSearch tool
     */
    suspend fun searchGrounding(query: String): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.5-flash"
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            val part = JSONObject().put("text", query)
            val content = JSONObject().put("parts", JSONArray().put(part))
            json.put("contents", JSONArray().put(content))

            // Tool: Google Search Grounding
            val toolsArray = JSONArray()
            val googleSearchTool = JSONObject().put("googleSearch", JSONObject())
            toolsArray.put(googleSearchTool)
            json.put("tools", toolsArray)

            val response = executePost(endpoint, json.toString())
            parseGroundedResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Search Grounding failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error en Search Grounding",
                modelUsed = model
            )
        }
    }

    /**
     * Maps Grounding using gemini-3.5-flash with googleMaps tool
     */
    suspend fun mapsGrounding(query: String): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.5-flash"
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            val part = JSONObject().put("text", "Información de mapas y ubicación: $query")
            val content = JSONObject().put("parts", JSONArray().put(part))
            json.put("contents", JSONArray().put(content))

            val toolsArray = JSONArray()
            val googleMapsTool = JSONObject().put("googleMaps", JSONObject())
            toolsArray.put(googleMapsTool)
            json.put("tools", toolsArray)

            val response = executePost(endpoint, json.toString())
            parseGroundedResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Maps Grounding failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error en Google Maps Grounding",
                modelUsed = model
            )
        }
    }

    /**
     * Image Generation using gemini-3.1-flash-image-preview
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1"
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.1-flash-image-preview"
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            val part = JSONObject().put("text", prompt)
            val content = JSONObject().put("parts", JSONArray().put(part))
            json.put("contents", JSONArray().put(content))

            val genConfig = JSONObject()
                .put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                .put("imageConfig", JSONObject().put("aspectRatio", aspectRatio).put("imageSize", "1K"))
            json.put("generationConfig", genConfig)

            val response = executePost(endpoint, json.toString())
            parseImageResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Image generation failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al generar imagen con Gemini",
                modelUsed = model
            )
        }
    }

    /**
     * Video Generation (Text to Video) using veo-3.1-fast-generate-preview
     */
    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9" // "16:9" or "9:16"
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "veo-3.1-fast-generate-preview"
        try {
            val endpoint = "$BASE_URL/$model:generateVideos?key=$apiKey"
            val json = JSONObject().put("prompt", prompt)
            val config = JSONObject()
                .put("aspectRatio", aspectRatio)
                .put("resolution", "720p")
                .put("numberOfVideos", 1)
            json.put("config", config)

            val response = executePost(endpoint, json.toString())
            val respJson = JSONObject(response)
            val operationName = respJson.optString("name", "")

            AiGenerationResult(
                success = true,
                text = "Video iniciado con Veo 3 (${aspectRatio}). Operación: $operationName",
                mediaUrl = "veo_generated_placeholder",
                modelUsed = model
            )
        } catch (e: Exception) {
            Log.e(TAG, "Veo video generation failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al generar video con Veo 3",
                modelUsed = model
            )
        }
    }

    /**
     * Animate Image to Video using veo-3.1-fast-generate-preview
     */
    suspend fun animateImageToVideo(
        prompt: String,
        imageBase64: String,
        aspectRatio: String = "16:9"
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "veo-3.1-fast-generate-preview"
        try {
            val endpoint = "$BASE_URL/$model:generateVideos?key=$apiKey"
            val json = JSONObject().put("prompt", prompt)
            val imageObj = JSONObject().put("imageBytes", imageBase64)
            json.put("image", imageObj)
            val config = JSONObject()
                .put("aspectRatio", aspectRatio)
                .put("resolution", "720p")
                .put("numberOfVideos", 1)
            json.put("config", config)

            val response = executePost(endpoint, json.toString())
            val respJson = JSONObject(response)
            val operationName = respJson.optString("name", "")

            AiGenerationResult(
                success = true,
                text = "Animación de imagen a video iniciada (${aspectRatio}). Operación: $operationName",
                mediaUrl = "veo_animated_video",
                modelUsed = model
            )
        } catch (e: Exception) {
            Log.e(TAG, "Veo animate image to video failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al animar imagen a video",
                modelUsed = model
            )
        }
    }

    /**
     * Music generation using lyria-3-clip-preview (up to 30s) or lyria-3-pro-preview (full track)
     */
    suspend fun generateMusic(
        prompt: String,
        isFullTrack: Boolean = false
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = if (isFullTrack) "lyria-3-pro-preview" else "lyria-3-clip-preview"
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            val textPrompt = if (isFullTrack) {
                "Full-length music track: $prompt"
            } else {
                "Short 30-second music clip: $prompt"
            }
            val part = JSONObject().put("text", textPrompt)
            val content = JSONObject().put("parts", JSONArray().put(part))
            json.put("contents", JSONArray().put(content))

            val genConfig = JSONObject()
                .put("responseModalities", JSONArray().put("AUDIO"))
            json.put("generationConfig", genConfig)

            val response = executePost(endpoint, json.toString())
            parseAudioResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Lyria music generation failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al generar música con Lyria",
                modelUsed = model
            )
        }
    }

    /**
     * Voice conversation using gemini-3.8-live
     */
    suspend fun voiceConversation(
        userPrompt: String
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.8-live"
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            val part = JSONObject().put("text", userPrompt)
            val content = JSONObject().put("parts", JSONArray().put(part))
            json.put("contents", JSONArray().put(content))

            val genConfig = JSONObject()
                .put("responseModalities", JSONArray().put("TEXT").put("AUDIO"))
            json.put("generationConfig", genConfig)

            val response = executePost(endpoint, json.toString())
            parseTextResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Live Voice Conversation failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error en Live Voice API",
                modelUsed = model
            )
        }
    }

    /**
     * Transcribe audio using gemini-3.5-transcribe
     */
    suspend fun transcribeAudio(
        audioBase64: String,
        mimeType: String = "audio/mp3"
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.5-transcribe"
        try {
            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val json = JSONObject()

            val inlineData = JSONObject()
                .put("mimeType", mimeType)
                .put("data", audioBase64)
            val audioPart = JSONObject().put("inlineData", inlineData)
            val promptPart = JSONObject().put("text", "Transcribe este audio palabra por palabra con puntuación precisa en español:")

            val content = JSONObject().put("parts", JSONArray().put(audioPart).put(promptPart))
            json.put("contents", JSONArray().put(content))

            val response = executePost(endpoint, json.toString())
            parseTextResponse(response, model)
        } catch (e: Exception) {
            Log.e(TAG, "Audio transcription failed", e)
            AiGenerationResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error al transcribir audio",
                modelUsed = model
            )
        }
    }

    private fun executePost(endpoint: String, jsonBody: String): String {
        if (apiKey.isBlank()) {
            throw Exception("Clave de API no configurada. Por favor agrega GEMINI_API_KEY en el panel de Secrets de AI Studio.")
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = runCatching {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message")
                }.getOrNull() ?: "HTTP ${response.code}: $responseString"
                throw Exception(errorMsg)
            }
            return responseString
        }
    }

    private fun parseTextResponse(responseString: String, model: String): AiGenerationResult {
        val root = JSONObject(responseString)
        val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
        val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
        val textBuilder = StringBuilder()
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i)
                val text = p?.optString("text")
                if (!text.isNullOrBlank()) {
                    textBuilder.append(text)
                }
            }
        }
        val text = textBuilder.toString().ifBlank { "Sin respuesta recibida." }
        return AiGenerationResult(success = true, text = text, modelUsed = model)
    }

    private fun parseGroundedResponse(responseString: String, model: String): AiGenerationResult {
        val root = JSONObject(responseString)
        val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
        val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
        val textBuilder = StringBuilder()
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i)
                val text = p?.optString("text")
                if (!text.isNullOrBlank()) {
                    textBuilder.append(text)
                }
            }
        }

        // Extract grounding sources
        val sources = mutableListOf<String>()
        val groundingMetadata = candidate?.optJSONObject("groundingMetadata")
        val webSearchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
        if (webSearchQueries != null) {
            for (i in 0 until webSearchQueries.length()) {
                sources.add("Búsqueda: ${webSearchQueries.optString(i)}")
            }
        }
        val searchChunks = groundingMetadata?.optJSONArray("groundingChunks")
        if (searchChunks != null) {
            for (i in 0 until searchChunks.length()) {
                val web = searchChunks.optJSONObject(i)?.optJSONObject("web")
                val title = web?.optString("title")
                val uri = web?.optString("uri")
                if (!title.isNullOrBlank()) {
                    sources.add("$title (${uri ?: ""})")
                }
            }
        }

        return AiGenerationResult(
            success = true,
            text = textBuilder.toString(),
            sources = sources,
            modelUsed = model
        )
    }

    private fun parseImageResponse(responseString: String, model: String): AiGenerationResult {
        val root = JSONObject(responseString)
        val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
        val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
        var text = ""
        var imageData: String? = null
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i)
                val t = p?.optString("text")
                if (!t.isNullOrBlank()) text += "$t\n"
                val inline = p?.optJSONObject("inlineData")
                if (inline != null) {
                    imageData = inline.optString("data")
                }
            }
        }
        return AiGenerationResult(
            success = true,
            text = text.ifBlank { "Imagen generada con éxito" },
            mediaUrl = imageData,
            mimeType = "image/png",
            modelUsed = model
        )
    }

    private fun parseAudioResponse(responseString: String, model: String): AiGenerationResult {
        val root = JSONObject(responseString)
        val candidate = root.optJSONArray("candidates")?.optJSONObject(0)
        val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
        var audioData: String? = null
        var text = ""
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i)
                val t = p?.optString("text")
                if (!t.isNullOrBlank()) text += t
                val inline = p?.optJSONObject("inlineData")
                if (inline != null) {
                    audioData = inline.optString("data")
                }
            }
        }
        return AiGenerationResult(
            success = true,
            text = text.ifBlank { "Pista musical creada con éxito con Lyria" },
            mediaUrl = audioData,
            mimeType = "audio/mp3",
            modelUsed = model
        )
    }
}
