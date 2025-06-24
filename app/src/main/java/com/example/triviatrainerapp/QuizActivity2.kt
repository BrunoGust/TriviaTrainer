package com.example.triviatrainerapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File
import android.view.accessibility.AccessibilityManager


class QuizActivity2 : AppCompatActivity() {
    private val respuestasUsuario = mutableListOf<RespuestaUsuario>()
    private var opcionSeleccionada: Button? = null
    private lateinit var preguntas: List<Pregunta>
    private var indicePreguntaActual = 0
    private var countDownTimer: CountDownTimer? = null // VARIABLE PARA ALMACENAR CONTADOR
    private val tiempoLimite: Long = 45000  // 45 segundos en milisegundos
    private var mensajeInicial: Boolean = true
    private lateinit var tts: TextToSpeech // variable para el manejo del voz a texto
    private var talkBackActivo: Boolean = false // inicialmente considera que el talkback esta desactivado
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz2)
        //Cargar Preguntas de archivo json
        preguntas = cargarPreguntasDesdeJSON()

        // 1ro verificamos que el talkback esta activo
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        talkBackActivo = am.isEnabled && am.isTouchExplorationEnabled
        // si esta activo inicializamos el servicio de texto a voz
        if (talkBackActivo) {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts.language = java.util.Locale.getDefault()
                    leerPreguntaYOpciones()
                }
            }
        }
        mostrarPregunta(preguntas[indicePreguntaActual])
        // Configurar UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar botones
        val btnSalir = findViewById<Button>(R.id.buttonSalirQuiz)
        val btnSiguiente = findViewById<Button>(R.id.buttonSiguientePregunta)
        val btnHelp = findViewById<ImageButton>(R.id.buttonHelp)

        val btnsOpciones = listOf(
            findViewById<Button>(R.id.buttonRespuesta1),
            findViewById<Button>(R.id.buttonRespuesta2),
            findViewById<Button>(R.id.buttonRespuesta3),
            findViewById<Button>(R.id.buttonRespuesta4)
        )

        btnSalir.setOnClickListener {
            finishAffinity() // Cierra completamente la app
        }

        btnHelp.setOnClickListener {
            //mostrarDialogoAyuda()
            val dialog = AssistantDialogFragment()
            dialog.arguments = Bundle().apply {
                putString("pantalla", "quiz")
                putBoolean("mensajeInicial", mensajeInicial)
            }
            dialog.show(supportFragmentManager, "AssistantDialog")
            mensajeInicial=false
        }

        btnsOpciones.forEach { btn ->
            btn.setOnClickListener {
                opcionSeleccionada?.setBackgroundResource(R.drawable.generic_button_selector)
                btn.setBackgroundColor(Color.parseColor("#00FF41"))
                opcionSeleccionada = btn
            }
        }

        btnSiguiente.setOnClickListener {
            if (opcionSeleccionada != null) {
                countDownTimer?.cancel()
                avanzarASiguientePregunta()
            }
            else {
                Toast.makeText(this, "Selecciona una respuesta", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun mostrarDialogoAyuda() {
        AlertDialog.Builder(this)
            .setTitle("Ayuda")
            .setMessage("Selecciona una respuesta y presiona 'Siguiente Pregunta'. Puedes salir en cualquier momento.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostrarPregunta(preguntaData: Pregunta) {
        val pregunta = findViewById<TextView>(R.id.textViewPregunta)
        val botones = listOf(
            findViewById<Button>(R.id.buttonRespuesta1),
            findViewById<Button>(R.id.buttonRespuesta2),
            findViewById<Button>(R.id.buttonRespuesta3),
            findViewById<Button>(R.id.buttonRespuesta4)
        )
        val textViewTiempo = findViewById<TextView>(R.id.textViewTiempoNumero)

        pregunta.text = "P${preguntaData.id}: ${preguntaData.pregunta}"
        botones.forEachIndexed { i, btn ->
            btn.text = "${'a' + i}) ${preguntaData.opciones[i]}"
            btn.setBackgroundResource(R.drawable.generic_button_selector)
        }

        opcionSeleccionada = null

        // Cancelar si hay temporizador activo
        countDownTimer?.cancel()

        // Iniciar temporizador
        countDownTimer = object : CountDownTimer(tiempoLimite, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                val minutos = segundosRestantes / 60
                val segundos = segundosRestantes % 60
                textViewTiempo.text = String.format("%02d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                textViewTiempo.text = "00:00"
                // Si no se seleccionó respuesta, continuar
                if (opcionSeleccionada == null) {
                    avanzarASiguientePregunta()
                }
            }
        }.start()
        // Leeremos la pregunta y respuestas una vez que cambie a la siguiente pregunta
        if(talkBackActivo){
            leerPreguntaYOpciones()
        }
    }
    private fun avanzarASiguientePregunta() {
        val botones = listOf(
            findViewById<Button>(R.id.buttonRespuesta1),
            findViewById<Button>(R.id.buttonRespuesta2),
            findViewById<Button>(R.id.buttonRespuesta3),
            findViewById<Button>(R.id.buttonRespuesta4)
        )

        val preguntaActual = preguntas[indicePreguntaActual]

        val indiceSeleccionado = if (opcionSeleccionada != null) {
            botones.indexOf(opcionSeleccionada)
        } else {
            -1 // No respondió
        }

        respuestasUsuario.add(
            RespuestaUsuario(
                idPregunta = preguntaActual.id,
                indiceSeleccionado = indiceSeleccionado,
                indiceCorrecto = preguntaActual.respuesta_correcta
            )
        )

        indicePreguntaActual++
        if (indicePreguntaActual < preguntas.size) {
            mostrarPregunta(preguntas[indicePreguntaActual])
        } else {
            val intent = Intent(this, ResultadosQuizActivity::class.java).apply {
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "CALCULANDO RESULTADOS...")
                putParcelableArrayListExtra("respuestas_usuario", ArrayList(respuestasUsuario))
            }
            startActivity(intent)
        }
    }


    // carga de la carpeta assets
    private fun cargarPreguntasDesdeJSON(): List<Pregunta> {
        val archivo = File(filesDir, "questions.json")
        if (!archivo.exists()) {
            Toast.makeText(this, "No se encontró el archivo de preguntas.", Toast.LENGTH_LONG).show()
            return emptyList()
        }
        val json = archivo.readText()
        val tipo = object : TypeToken<List<Pregunta>>() {}.type
        return Gson().fromJson(json, tipo)
    }
    // funcion para leer la pregunta y sus opciones
    private fun leerPreguntaYOpciones() {
        val preguntaActual = preguntas[indicePreguntaActual]
        val enunciado = StringBuilder()
        enunciado.append("Pregunta ${preguntaActual.id}: ${preguntaActual.pregunta}. ")
        preguntaActual.opciones.forEachIndexed { i, opcion ->
            enunciado.append("Opción ${'A' + i}: $opcion. ")
        }
        hablar(enunciado.toString())
    }
    private fun hablar(texto: String) {
        if (::tts.isInitialized) {
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    // Liberamos el texto a voz una vez que concluyo la actividad
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
