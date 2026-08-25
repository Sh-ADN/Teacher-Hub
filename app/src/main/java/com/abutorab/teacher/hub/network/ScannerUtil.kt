package com.abutorab.teacher.hub.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
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
    fun Bitmap.scaleDown(maxSize: Int = 1024): Bitmap {
        val ratio = Math.min(maxSize.toFloat() / width, maxSize.toFloat() / height)
        if (ratio >= 1.0) return this
        val scaledWidth = (this.width * ratio).toInt()
        val scaledHeight = (this.height * ratio).toInt()
        return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
    }

    fun Bitmap.toBase64(): String {
        val scaled = scaleDown()
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun scanMarksheet(bitmap: Bitmap, apiKey: String): List<ScanResultItem>? = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("ScannerUtil", "API Key is missing!")
            return@withContext emptyList()
        }

        val prompt = """
            You are an expert assistant that extracts data from handwritten Bengali marks sheets. 
            I will provide an image of a marks sheet. Extract the roll number (রোল নং) and marks for each row. 
            The numbers might be written in Bengali or English digits (e.g., ১=1, ২=2, etc.). Please convert all numbers to standard English digits in the output.
            
            The columns typically include Roll Number and Marks.
            The marks column might be formatted as an equation: "(নৈঃ+রঃ+ব্যবঃ = মোট)" which means "(MCQ + Written + Practical = Total)".
            For example, a cell containing "১৫ + ৪৮ + ৬৩" means MCQ is 15, Written is 48, Practical is empty/null, and Total is 63. 
            A cell containing "১২ + ৩২ + ১৬" means MCQ is 12, Written is 32, Practical is 16.
            If any field is missing, empty, or has a dash (-), use null for that field.

            The output MUST be a valid JSON object with a single key "results" containing an array of objects.
            Format:
            {
              "results": [
                {
                  "roll": 1,
                  "mcq": 15,
                  "written": 48,
                  "practical": null
                }
              ]
            }
            Return ONLY raw JSON, do not wrap it in ```json or any markdown formatting.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(
                parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = bitmap.toBase64()))
                )
            )),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json"
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return@withContext null
            Log.d("ScannerUtil", "Raw API Response: ${text}")
            
            val cleanedText = text.replace("```json", "").replace("```", "").trim()
            
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(ScanResponse::class.java)
            val scanResponse = adapter.fromJson(cleanedText)
            scanResponse?.results
        } catch (e: Exception) {
            Log.e("ScannerUtil", "Error scanning marksheet: ${e.message}", e)
            null
        }
    }
}
