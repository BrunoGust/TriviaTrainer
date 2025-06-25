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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.result.contract.ActivityResultContracts
import android.speech.tts.TextToSpeech
import java.util.Locale



class AssistantDialogFragment : DialogFragment() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter
    private lateinit var speechLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var mensajePendiente: String? = null
    private var ttsEnabled = true
    private lateinit var toggleVoiceButton: ImageButton
    private var vozActiva = true
    private val PREFS_NAME = "assistant_prefs"
    private val PREFS_KEY_VOZ = "voz_activada"






    val systemInstructionAsistente = Content(
        role = "system",
        parts = listOf(
            TextPart("""
Eres un asistente inteligente dentro de una aplicación educativa para crear quizzes. Tu objetivo es ayudar al usuario a usar la aplicación de manera efectiva, explicando las funciones disponibles y guiándolo según la pantalla en la que se encuentre.

Tu tono debe ser claro, amigable, directo y didáctico. Utiliza lenguaje sencillo y evita respuestas excesivamente técnicas. Si el usuario tiene dudas sobre cómo generar un quiz, interpretar el resumen, o responder preguntas, debes explicarlo con ejemplos simples. Si no sabes algo con certeza, di que no estás seguro.

Nunca salgas del rol de asistente de la app. No des información no relacionada con el funcionamiento de la aplicación.

Tus respuestas deben ser breves (1 o 2 párrafos como máximo) y útiles.

""".trimIndent())
        )
    )


    private val assistantModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.API_KEY,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 32
                topP = 1.0f
                maxOutputTokens = 512
            },
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE)
            ),
            systemInstruction = systemInstructionAsistente
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_assistant_dialog, container, false)

    //  Primero inicializas el botón
        toggleVoiceButton = view.findViewById(R.id.buttonToggleVoice)

    //  Luego recuperas preferencias
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        vozActiva = prefs.getBoolean(PREFS_KEY_VOZ, true)

    //  Y actualizas su icono
        actualizarIconoVoz()



        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "ES")
                ttsReady = true

                // Si había un mensaje pendiente, hablarlo ahora
                mensajePendiente?.let {
                    tts.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
                    mensajePendiente = null
                }
            }
        }



        //  Obtener el ViewModel compartido entre actividades
        val app = requireActivity().application as MyApplication

        speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val recognizedText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                recognizedText?.firstOrNull()?.let { spokenText ->
                    addMessage(spokenText, isUser = true)
                    responderConContexto(spokenText)
                }
            } else {
                Toast.makeText(requireContext(), "No se pudo reconocer voz.", Toast.LENGTH_SHORT).show()
            }
        }


        chatViewModel = ViewModelProvider(
            app, // ViewModelStoreOwner global
            ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        ).get(ChatViewModel::class.java)


        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        val input = view.findViewById<EditText>(R.id.userInput)
        val sendButton = view.findViewById<ImageButton>(R.id.sendButton)
        val closeButton = view.findViewById<ImageButton>(R.id.btnCloseAssistant)
        val voiceButton = view.findViewById<ImageButton>(R.id.voiceButton)

        input.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hayTexto = !s.isNullOrBlank()
                sendButton.visibility = if (hayTexto) View.VISIBLE else View.GONE
                voiceButton.visibility = if (hayTexto) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        toggleVoiceButton.setOnClickListener {
            vozActiva = !vozActiva
            prefs.edit().putBoolean(PREFS_KEY_VOZ, vozActiva).apply()
            actualizarIconoVoz()
        }



        voiceButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla tu mensaje para el asistente")
            }

            if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
                speechLauncher.launch(intent)
            } else {
                Toast.makeText(requireContext(), "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show()
            }
        }



        //  Enlazar adaptador con la lista del ViewModel
        adapter = ChatAdapter(chatViewModel.messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sendButton.setOnClickListener {
            val userMessage = input.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, isUser = true)
                input.text.clear()
                //simulateAssistantResponse(userMessage)
                responderConContexto(userMessage)
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        recyclerView.scrollToPosition(chatViewModel.messages.size - 1)

        mostrarMensajeInicial()
        val mostrarBienvenida = arguments?.getBoolean("autoBienvenida", false) == true
        if (mostrarBienvenida && chatViewModel.messages.isEmpty()) {
            val mensaje = "Hola, soy tu asistente virtual. Estoy aquí para ayudarte a usar la aplicación. ¿En qué puedo ayudarte?"
            addMessage(mensaje, isUser = false)

        }



        return view
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val chatMessage = ChatMessage(message, isUser)
        chatViewModel.addMessage(chatMessage)
        adapter.notifyItemInserted(chatViewModel.messages.size - 1)

        // Desplazar al último mensaje
        view?.findViewById<RecyclerView>(R.id.chatRecyclerView)?.scrollToPosition(chatViewModel.messages.size - 1)

        // Si es mensaje del asistente, hablarlo
        if (!isUser && ttsEnabled && vozActiva) {
            if (ttsReady && ::tts.isInitialized) {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                mensajePendiente = message
            }
        }

    }

    private fun actualizarIconoVoz() {
        if (::toggleVoiceButton.isInitialized) {
            val icon = if (vozActiva) R.drawable.ic_volume_on else R.drawable.ic_volume_off
            toggleVoiceButton.setImageResource(icon)
        }
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

    private fun mostrarMensajeInicial() {
        val pantalla = arguments?.getString("pantalla") ?: "general"
        val mensajeInicial = arguments?.getBoolean("mensajeInicial", false) == true
        if(mensajeInicial){
            if (pantalla=="quiz") {
                val mensaje = "Selecciona una respuesta y presiona 'Siguiente Pregunta'. Puedes salir en cualquier momento."
                addMessage(mensaje, isUser = false)
            }
        }

    }

    private fun responderConContexto(userInput: String) {
        val pantalla = arguments?.getString("pantalla") ?: "general"

        val contexto = when (pantalla) {
            "inicio" -> "Estás ayudando al usuario desde la pantalla de Inicio. El usuario escribe o dice un tema para generar información para repasar antes de empezar el quiz y luego tiene hacer clic en el botón 'GENERAR QUIZ' ."
            "repaso" -> "Estás ayudando al usuario desde la pantalla de Repaso. El usuario ya tiene un resumen de un tema y pronto podrá empezar el quiz haciendo clic en el botón 'Empezar quiz'."
            "quiz" -> "Estás en la pantalla del Quiz. El usuario está respondiendo preguntas de trivia."
            "resultados" -> "Estás en la pantalla de resultados. El usuario ya terminó de responder las preguntas. Ofrece volver a la pantalla de inicio para generar un nuevo quiz"
            else -> "Estás asistiendo al usuario en la aplicación TriviaTrainer."
        }

        val prompt = """
        $contexto
        
        El usuario dijo: "$userInput"
        
        Brinda una respuesta clara, útil y directa. Si es una solicitud sobre funciones, explica cómo usar la aplicación desde esta pantalla.
    """.trimIndent()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = assistantModel.generateContent(prompt)
                val respuesta = result.text ?: "Lo siento, no tengo una respuesta ahora."
                addMessage(respuesta, isUser = false)


            } catch (e: Exception) {
                addMessage("Hubo un error al responder con el asistente.", isUser = false)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.BOTTOM)

    }

    override fun onDestroyView() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroyView()
    }

}
