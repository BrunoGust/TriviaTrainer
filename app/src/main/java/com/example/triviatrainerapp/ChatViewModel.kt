package com.example.triviatrainerapp

import ChatMessage
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val messages = mutableListOf<ChatMessage>()


    fun addMessage(message: ChatMessage) {
        messages.add(message)
    }

    fun clearMessages() {
        messages.clear()
    }
}
