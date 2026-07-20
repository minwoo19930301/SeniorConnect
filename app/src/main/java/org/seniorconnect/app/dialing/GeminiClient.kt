package org.seniorconnect.app.dialing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Thin wrapper around the Gemini 2.0 Flash REST API.
 * All calls are synchronous — run them on a background thread.
 */
object GeminiClient {

    // ⚠️  API key is here for hackathon convenience only.
    //     Do NOT commit a production key to a public repo.
    private const val API_KEY = "AIzaSyALAl3WbHASCw7bewimfCrg7AraYklMmkY"
    private const val MODEL   = "gemini-2.0-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$API_KEY"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json".toMediaType()

    /**
     * Send a single-turn prompt and return the text response.
     * Returns null on any network or parsing error.
     */
    fun ask(prompt: String): String? {
        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            // Keep replies short and fast
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 100)
                put("temperature", 0.2)
            })
        }.toString()

        val request = Request.Builder()
            .url(ENDPOINT)
            .post(body.toRequestBody(JSON))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val json = JSONObject(response.body?.string() ?: return null)
                json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
            }
        } catch (_: Exception) {
            null
        }
    }
}
