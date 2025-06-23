package com.example.triviatrainerapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AssistantDialogFragment : DialogFragment() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_dialog, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        val input = view.findViewById<EditText>(R.id.userInput)
        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)
        val closeButton = view.findViewById<ImageButton>(R.id.btnCloseAssistant)

        adapter = ChatAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        messages.clear()
        //messages.addAll(loadMessages())
        adapter.notifyDataSetChanged()


        sendButton.setOnClickListener {
            val userMessage = input.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, isUser = true)
                input.text.clear()
                simulateAssistantResponse(userMessage)
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }




        return view
    }

    private fun addMessage(message: String, isUser: Boolean) {
        messages.add(ChatMessage(message, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        //saveMessages()

    }

    private val PREFS_NAME = "assistant_prefs"
    private val KEY_MESSAGES = "chat_messages"

    private fun saveMessages() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(messages)
        prefs.edit().putString(KEY_MESSAGES, json).apply()
    }

    private fun loadMessages(): MutableList<ChatMessage> {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MESSAGES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
        return Gson().fromJson(json, type)
    }


    private fun simulateAssistantResponse(userInput: String) {
        val response = when {
            userInput.contains("tema", ignoreCase = true) -> "Puedes escribir un tema como historia, ciencia o cualquier cosa que te interese."
            userInput.contains("ayuda", ignoreCase = true) -> "Estoy aquí para ayudarte a crear tu quiz. ¿Qué necesitas?"
            else -> "Interesante. ¿Puedes decirme más sobre eso?"
        }

        Handler(Looper.getMainLooper()).postDelayed({
            addMessage(response, isUser = false)
        }, 800)
    }

    private fun borrarMensajesGuardados() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()  // Elimina todos los datos guardados en este SharedPreferences
        messages.clear()              // Limpia también la lista en memoria
        adapter.notifyDataSetChanged()
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }
}

