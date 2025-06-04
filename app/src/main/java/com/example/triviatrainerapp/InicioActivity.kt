package com.example.triviatrainerapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class InicioActivity : AppCompatActivity() {

    private lateinit var editTextTheme: EditText
    private lateinit var voiceButton: ImageButton
    private lateinit var generateQuizButton: Button
    private lateinit var welcomeTextView: TextView
    private lateinit var speechRecognizerLauncher: ActivityResultLauncher<Intent>
    private lateinit var salirBtn: Button


    val config = generationConfig {
        temperature = 0.0f
        topK = 32
        topP = 1.0f
        maxOutputTokens = 1024
    }

    val mysystemInstruction = Content(
        role = "system",
        parts = listOf(
            TextPart(
                """
            Eres un asistente experto en educación y generación de contenido informativo accesible.
            Cuando un usuario proporciona un tema, debes devolver un resumen claro, conciso y didáctico del tema, adecuado para personas de distintos niveles de conocimiento, incluyendo estudiantes, autodidactas y personas con bajo nivel de alfabetización digital.
            El resumen debe tener una extensión máxima de 250 palabras y estructurarse con lenguaje claro y comprensible, pero sin sacrificar la precisión del contenido.
            Al final del resumen, proporciona al menos 3 enlaces confiables y actualizados (por ejemplo: Wikipedia, sitios educativos oficiales, enciclopedias académicas, portales de divulgación científica o técnica) que sirvan como fuentes y material de estudio complementario para que el usuario pueda profundizar en el tema.

            Reglas adicionales:
            No inventes enlaces. Usa solo fuentes verificables y confiables.
            Evita tecnicismos innecesarios a menos que sean relevantes al tema.
            Si el tema es muy amplio, prioriza los conceptos esenciales y generales.
            Si el tema no es válido o es ambiguo, solicita al usuario que lo reformule de manera más específica.
            
            Formato de respuesta:
            
            [Título del tema]
            [Resumen claro y accesible del tema en 1-2 párrafos]
            
            Fuentes para repasar:
            1. [Nombre del sitio o fuente] - [URL]
            2. ...
            3. ...
        
            """.trimIndent()
            )
        )
    )

    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "TuApiKEY",
        generationConfig = config,
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.LOW_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.LOW_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE)
        ),
        systemInstruction = mysystemInstruction
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)


        editTextTheme = findViewById(R.id.input_tema)
        voiceButton = findViewById(R.id.voz_btn)
        generateQuizButton = findViewById(R.id.generar_quiz_btn)
        welcomeTextView = findViewById(R.id.textView4)
        salirBtn = findViewById(R.id.salir_btn)

        val username = intent.getStringExtra(MainActivity.EXTRA_USERNAME)
        if (username != null && username.isNotEmpty()) {
            welcomeTextView.text = "Saludos, $username!" // Actualizar el texto
        } else {
            welcomeTextView.text = "Saludos, USUARIO!" // O mantener el por defecto si no hay nombre
        }



        // 2. Inicializar el lanzador de resultados de reconocimiento de voz
        speechRecognizerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val recognizedText: ArrayList<String>? = data?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )
                    if (!recognizedText.isNullOrEmpty()) {
                        val spokenText = recognizedText[0]
                        editTextTheme.setText(spokenText) // Coloca el texto reconocido en el EditText
                        Toast.makeText(
                            this,
                            "Tema ingresado por voz: $spokenText",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Reconocimiento de voz cancelado.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Error en el reconocimiento de voz.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        // 3. Configurar el listener para el botón de voz
        voiceButton.setOnClickListener {
            startSpeechToText()
        }

        // 4. Configurar el listener para el botón Generar Quiz (ejemplo)
        generateQuizButton.setOnClickListener {
            val theme = editTextTheme.text.toString().trim()
            if (theme.isNotEmpty()) {

                lifecycleScope.launch {
                    try {
                        val response = generativeModel.generateContent("El tema es: $theme")
                        val resumen = response.text
/*
                        val intent = Intent(this@InicioActivity, RepasoActivity::class.java).apply {
                            putExtra("RESUMEN_GENERADO", resumen)
                            putExtra("TEMA_ORIGINAL", theme)
                        }
                        startActivity(intent)
                        */

                        //Toast.makeText(this, "Generando quiz sobre: $theme", Toast.LENGTH_SHORT).show()
                        // Aquí es donde llamarías a tu pantalla de carga
                        val loadingIntent = Intent(this@InicioActivity, LoadingScreenActivity::class.java).apply {
                            putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, RepasoActivity::class.java.name)
                            // También podrías pasar el 'theme' si lo necesitas en la siguiente Activity
                            putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "GENERANDO INFORMACION...")
                            putExtra(MainActivity.EXTRA_USERNAME,username)
                            putExtra("RESUMEN_GENERADO", resumen)
                            putExtra("TEMA_ORIGINAL", theme)
                        } // Asegúrate que esta clase exista y esté en el paquete correcto
                        startActivity(loadingIntent)
                        // No llames finish() aquí si quieres que InicioActivity permanezca en la pila

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@InicioActivity, "Error al generar el resumen", Toast.LENGTH_SHORT).show()
                    }
                }



            } else {
                Toast.makeText(this, "Por favor, ingresa o indica un tema.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        salirBtn.setOnClickListener {
            val exitIntent = Intent(this, LoadingScreenActivity::class.java).apply {
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, MainActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "Te extrañaremos $username :(\nCERRANDO SESION...")
            }
            startActivity(exitIntent)
            finish()

        }


    }

    private fun startSpeechToText() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "es-ES"
                ) // Especifica el idioma (Español de España)
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Habla el tema para tu quiz"
                ) // Mensaje al usuario
            }
            speechRecognizerLauncher.launch(speechIntent)
        } else {
            Toast.makeText(
                this,
                "El reconocimiento de voz no está disponible en este dispositivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}