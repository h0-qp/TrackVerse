package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.network.ApiClient
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GeminiClient
import com.example.network.Part
import com.example.network.TmdbShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiMessage(val text: String, val isUser: Boolean, val shows: List<TmdbShow> = emptyList())

class AiChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<AiMessage>>(
        listOf(AiMessage("Hi! I'm your AI assistant. Tell me what kind of show or movie you're looking for.", false))
    )
    val messages: StateFlow<List<AiMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(query: String) {
        if (query.isBlank() || _isLoading.value) return
        
        val newMessages = _messages.value.toMutableList()
        newMessages.add(AiMessage(query, true))
        _messages.value = newMessages
        
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val rawApiKey = BuildConfig.GEMINI_API_KEY
                val apiKey = rawApiKey.removePrefix("\"").removeSuffix("\"").trim()
                
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages.add(AiMessage("عذراً، مفتاح Gemini API غير مهيأ. يرجى إضافته في إعدادات الأسرار (Secrets).", false))
                    _messages.value = updatedMessages
                    return@launch
                }
                
                val prompt = "User wants: $query. Recommend 3-5 TMDB movies or TV shows. Reply ONLY with a comma-separated list of EXACT titles, nothing else. You are a TV/Movie recommendation system. Always reply with just a comma-separated list of titles."

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)), role = "user"))
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(apiKey, request)
                }

                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                if (responseText.isNotEmpty()) {
                    val titles = responseText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    val recommendedShows = mutableListOf<TmdbShow>()
                    
                    withContext(Dispatchers.IO) {
                        for (title in titles) {
                            try {
                                val searchResponse = ApiClient.tmdbService.search(title)
                                val show = searchResponse.results.firstOrNull { 
                                    it.title.equals(title, ignoreCase = true) || it.name.equals(title, ignoreCase = true)
                                }
                                if (show != null) {
                                    recommendedShows.add(show)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages.add(AiMessage("Here are some recommendations based on your request:", false, recommendedShows))
                    _messages.value = updatedMessages
                } else {
                    val updatedMessages = _messages.value.toMutableList()
                    updatedMessages.add(AiMessage("Sorry, I couldn't find any recommendations for that.", false))
                    _messages.value = updatedMessages
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = try { e.response()?.errorBody()?.string() } catch(t: Throwable) { null }
                val updatedMessages = _messages.value.toMutableList()
                val errorMessage = if (e.code() == 400 && errorBody?.contains("API_KEY_INVALID") == true) {
                    "عذراً، مفتاح API الخاص بـ Gemini غير صالح. يرجى التحقق من إضافة المفتاح الصحيح في قسم الأسرار (Secrets)."
                } else if (e.code() == 403 || e.code() == 401) {
                    val isLeaked = errorBody?.contains("leaked", ignoreCase = true) == true
                    if (isLeaked) {
                        "عذراً، لقد قامت جوجل بإيقاف مفتاح API الخاص بك لأنه تم تسريبه (Leaked API Key). يرجى إنشاء مفتاح جديد من Google AI Studio ووضعه في الـ Secrets."
                    } else {
                        "عذراً، غير مصرح لك بالوصول. يرجى التأكد من صحة مفتاح API. التفاصيل: $errorBody"
                    }
                } else if (e.code() == 404) {
                    "عذراً، لم يتم العثور على النموذج المحدد في خوادم Gemini. يرجى التأكد من استخدام نموذج مدعوم."
                } else {
                    "حدث خطأ في الاتصال: HTTP ${e.code()} - $errorBody"
                }
                updatedMessages.add(AiMessage(errorMessage, false))
                _messages.value = updatedMessages
            } catch (e: Exception) {
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(AiMessage("حدث خطأ غير متوقع: ${e.message}", false))
                _messages.value = updatedMessages
            } finally {
                _isLoading.value = false
            }
        }
    }
}
