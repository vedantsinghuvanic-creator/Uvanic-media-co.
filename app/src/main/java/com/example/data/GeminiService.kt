package com.example.data

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

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun getCompletion(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Returning fallback response.")
            return@withContext getFallbackResponse(prompt)
        }

        try {
            val rootJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // System Instruction
            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                rootJson.put("systemInstruction", sysInstObj)
            }

            // Generation Config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            rootJson.put("generationConfig", genConfig)

            val requestBodyStr = rootJson.toString()
            val requestBody = requestBodyStr.toRequestBody("application/json".toMediaType())

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Code=${response.code}, Body=$errBody")
                    return@withContext getFallbackResponse(prompt)
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response"
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                        }
                    }
                }
                "No text content found in Gemini response."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            getFallbackResponse(prompt)
        }
    }

    /**
     * Highly intelligent simulated backup fallback responses for rich offline/no-key usage.
     */
    private fun getFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("recommend") || lower.contains("category") -> {
                when {
                    lower.contains("wire") || lower.contains("spark") || lower.contains("switch") || lower.contains("light") || lower.contains("electricity") -> "Electrician"
                    lower.contains("pipe") || lower.contains("leak") || lower.contains("drain") || lower.contains("water") || lower.contains("tap") -> "Plumber"
                    lower.contains("sofa") || lower.contains("table") || lower.contains("chair") || lower.contains("wood") || lower.contains("door") -> "Carpenter"
                    lower.contains("ac") || lower.contains("cool") || lower.contains("filter") || lower.contains("air conditioner") -> "AC Technician"
                    lower.contains("paint") || lower.contains("wall") || lower.contains("wallpapers") -> "Painter"
                    lower.contains("fridge") || lower.contains("washing") || lower.contains("microwave") || lower.contains("tv") -> "Appliance Repair"
                    lower.contains("clean") || lower.contains("dust") || lower.contains("sweeping") || lower.contains("wash") -> "Cleaner"
                    lower.contains("cockroach") || lower.contains("pest") || lower.contains("bugs") || lower.contains("ant") -> "Pest Control"
                    else -> "Electrician"
                }
            }
            lower.contains("estimate") || lower.contains("repair cost") -> {
                val service = when {
                    lower.contains("electric") -> "electrical service"
                    lower.contains("plumb") -> "plumbing repair"
                    lower.contains("carpenter") -> "carpentry repair"
                    lower.contains("ac") -> "AC servicing"
                    lower.contains("paint") -> "wall touch-up"
                    lower.contains("appliance") -> "appliance troubleshooting"
                    lower.contains("clean") -> "home deep cleaning"
                    else -> "general technician check-up"
                }
                """
                {
                    "explanation": "This looks like a standard issue with your $service. The diagnostic includes a standard inspection and immediate minor parts replacement.",
                    "range": "₹450 - ₹950"
                }
                """.trimIndent()
            }
            lower.contains("coupon") || lower.contains("discount") -> {
                "You can use the promo code **FIXNOW50** to get flat ₹50 off on your first booking! We also offer seasonal discounts which are automatically applied at checkout."
            }
            lower.contains("track") || lower.contains("where") -> {
                "Once a technician is assigned, you can view their real-time location and estimated arrival time on the Live Tracking screen map."
            }
            else -> {
                "Hello! How can I assist you today with FixNow? You can ask me about repair estimations, recommended service categories, or our booking process."
            }
        }
    }
}
