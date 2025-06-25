package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson
import java.io.File
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.google.gson.reflect.TypeToken
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.launch


class RepasoActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView

    private lateinit var btnAssistant: ImageButton

    val systemInstructionPreguntas = Content(
        role = "system",
        parts = listOf(
            TextPart("""
Eres un generador automático de preguntas tipo trivia para educación. 
Dado un tema o resumen, tu tarea es crear **8 preguntas tipo trivia** (3 fáciles, 3 medias, 2 difíciles) sobre ese tema.

Cada pregunta debe:
- Tener una sola respuesta correcta
- Tener 4 opciones (A–D)
- Marcar cuál es la correcta
- Incluir un enlace a la fuente confiable de donde fue obtenida la pregunta
Usa este formato exacto para cada sección:

[PREGUNTAS - FÁCIL]
1. <Enunciado>
A) ...
B) ...
C) ...
D) ...
Respuesta: <Letra>
Fuente: <URL>
...

[PREGUNTAS - MEDIO]
...

[PREGUNTAS - DIFÍCIL]
...

No incluyas ninguna explicación, ni resumen, ni texto adicional. Solo preguntas con opciones y respuestas. No inventes enlaces. Usa conocimiento verificado y sin ambigüedad.
""".trimIndent())
        )
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_repaso)
/*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/

        val resumen = intent.getStringExtra("RESUMEN_GENERADO") ?: "Resumen no disponible"
        val tema = intent.getStringExtra("TEMA_ORIGINAL") ?: ""
        //Log.d("TEMA_DEBUG","El tema es $tema!")

        // Nuevo modelo para preguntas

        val config = generationConfig {
            temperature = 0.0f
            topK = 32
            topP = 1.0f
            maxOutputTokens = 1024
        }

        val modeloPreguntas = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = com.example.triviatrainerapp.BuildConfig.API_KEY,
            generationConfig = config,
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE)
            ),
            systemInstruction = systemInstructionPreguntas
        )


        val editText = findViewById<EditText>(R.id.editTextText)
        editText.setText(resumen)
        editText.setTextColor(Color.WHITE) // la fuente del resumen sera blanco para consistencia y buen contraste

        val button: Button = findViewById(R.id.btnEmpezarQuiz)
        welcomeTextView = findViewById(R.id.textView4)
        val username = intent.getStringExtra(MainActivity.EXTRA_USERNAME)
        if (username != null && username.isNotEmpty()) {
            welcomeTextView.text = "Saludos, $username!" // Actualizar el texto
        } else {
            welcomeTextView.text = "Saludos, USUARIO!" // O mantener el por defecto si no hay nombre
        }

        button.setOnClickListener {
            Toast.makeText(this, "Generando preguntas, espera un momento...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                try {
                    val response = modeloPreguntas.generateContent("Genera 8 preguntas de trivia sobre el siguiente resumen:\n\n$resumen")
                    val textoPreguntas = response.text ?: ""

                    val preguntasParseadas = extraerTodasLasPreguntas(textoPreguntas)
                    Log.d("PREGUNTAS_EXTRAIDAS", preguntasParseadas.joinToString("\n"))

                    guardarPreguntasEnJSON(this@RepasoActivity, preguntasParseadas)

                    Toast.makeText(this@RepasoActivity, "Preguntas generadas y guardadas", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@RepasoActivity, LoadingScreenActivity::class.java).apply {
                        putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, QuizActivity2::class.java.name)
                        putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "GENERANDO QUIZ...")
                        putExtra("TEMA",tema)
                    }
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RepasoActivity, "Error generando preguntas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnAssistant = findViewById(R.id.buttonHelpRepaso)

        btnAssistant.setOnClickListener {
            val dialog = AssistantDialogFragment()
            dialog.arguments = Bundle().apply {
                putString("pantalla", "repaso")
            }
            dialog.show(supportFragmentManager, "AssistantDialog")
        }




    }
}


data class PreguntaParseada(
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int,
    val fuente: String
)


fun extraerTodasLasPreguntas(texto: String): List<PreguntaParseada> {
    val regex = Regex(
        """\d+\.\s*(.*?)\s*A\)\s*(.*?)\s*B\)\s*(.*?)\s*C\)\s*(.*?)\s*D\)\s*(.*?)\s*Respuesta:\s*([A-D])\s*Fuente:\s*(https?://\S+)""",
        RegexOption.DOT_MATCHES_ALL
    )

    return regex.findAll(texto).map { match ->
        val enunciado = match.groupValues[1].trim()
        val opciones = (2..5).map { match.groupValues[it].trim() }
        val respuesta = match.groupValues[6].trim()[0] - 'A'
        val fuente = match.groupValues[7].trim()
        PreguntaParseada(enunciado, opciones, respuesta, fuente)
    }.toList()
}



fun guardarPreguntasEnJSON(
    context: Context,
    nuevasPreguntas: List<PreguntaParseada>,
    nombreArchivo: String = "questions.json"
) {
    val gson = Gson()
    val file = File(context.filesDir, nombreArchivo)

    val nuevas = nuevasPreguntas.mapIndexed { index, p ->
        Pregunta(index + 1, p.pregunta, p.opciones, p.respuestaCorrecta, p.fuente)
    }


    file.writeText(gson.toJson(nuevas)) // Sobrescribe completamente
}
