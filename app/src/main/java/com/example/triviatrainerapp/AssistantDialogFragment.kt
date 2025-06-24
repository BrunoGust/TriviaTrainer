package com.example.triviatrainerapp

import ChatMessage
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AssistantDialogFragment : DialogFragment() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_assistant_dialog, container, false)

        //  Obtener el ViewModel compartido entre actividades
        val app = requireActivity().application as MyApplication

        chatViewModel = ViewModelProvider(
            app, // ViewModelStoreOwner global
            ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        ).get(ChatViewModel::class.java)



        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        val input = view.findViewById<EditText>(R.id.userInput)
        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)
        val closeButton = view.findViewById<ImageButton>(R.id.btnCloseAssistant)

        //  Enlazar adaptador con la lista del ViewModel
        adapter = ChatAdapter(chatViewModel.messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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

        recyclerView.scrollToPosition(chatViewModel.messages.size - 1)


        return view
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val chatMessage = ChatMessage(message, isUser)
        chatViewModel.addMessage(chatMessage)
        adapter.notifyItemInserted(chatViewModel.messages.size - 1)

        // Desplazar al último mensaje
        view?.findViewById<RecyclerView>(R.id.chatRecyclerView)?.scrollToPosition(chatViewModel.messages.size - 1)
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }
}
