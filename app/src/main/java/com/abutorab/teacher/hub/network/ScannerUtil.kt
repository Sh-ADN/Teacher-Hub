package com.abutorab.teacher.hub.network

import android.graphics.Bitmap
import android.util.Base64
import com.abutorab.teacher.hub.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@JsonClass(generateAdapter = true)
data class ScanResultItem(
    val roll: Int,
    val mcq: Int?,
    val written: Int?,
    val practical: Int?
)

@JsonClass(generateAdapter = true)
data class ScanResponse(
    val results: List<ScanResultItem>
)

object ScannerUtil {
    fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun scanMarksheet(bitmap: Bitmap): List<ScanResultItem>? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext emptyList() // Or throw

        val prompt = """
            You are a helpful assistant that scans handwritten marks sheets. 
            I will provide an image of a marks sheet. Extract the roll number and marks for each row. 
            The language is Bengali, but the numbers might be in Bengali or English.
            The marks typically follow this order: MCQ (নৈর্ব্য), Written (রচনা), Practical (ব্যঃ). 
            If any field is missing or has a dash (-), use null for that field.
            The output should be JSON in this format:
            {
              "results": [
                {
                  "roll": 101,
                  "mcq": 15,
                  "written": 25,
                  "practical": null
                }
              ]
            }
            Return ONLY raw JSON, with no markdown formatting.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(
                parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = bitmap.toBase64()))
                )
            ))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return@withContext null
            
            // Clean up text if it has markdown
            val cleanedText = text.replace("```json", "").replace("```", "").trim()
            
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(ScanResponse::class.java)
            val scanResponse = adapter.fromJson(cleanedText)
            scanResponse?.results
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
