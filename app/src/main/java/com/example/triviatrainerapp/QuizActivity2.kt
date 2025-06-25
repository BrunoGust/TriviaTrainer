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
import android.content.ActivityNotFoundException
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import java.util.Locale


class QuizActivity2 : AppCompatActivity() {
    private val respuestasUsuario = mutableListOf<RespuestaUsuario>()
    private var opcionSeleccionada: Button? = null
    private lateinit var preguntas: List<Pregunta>
    private var indicePreguntaActual = 0
    private var countDownTimer: CountDownTimer? = null // VARIABLE PARA ALMACENAR CONTADOR
    private var tiempoLimite: Long = 30000  // 30 segundos en milisegundos
    private var mensajeInicial: Boolean = true
    private lateinit var tts: TextToSpeech // variable para el manejo del voz a texto
    private var talkBackActivo: Boolean = false // inicialmente considera que el talkback esta desactivado
    private val VOZ_REQUEST_CODE = 100// CONSTANTE RECONOCIMIENTO DE VOZ
    private var tema = ""
    //propiedades del mediaplayer
    private var mediaPlayerCorrecto: MediaPlayer? = null
    private var mediaPlayerIncorrecto: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz2)
        // Cargamos la variable del tema para enviarla a los resultados
        tema = intent.getStringExtra("TEMA") ?: ""
        Log.d("TEMA_DEBUG_2","El tema es $tema!")
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
            tiempoLimite = 45000
        }
        mostrarPregunta(preguntas[indicePreguntaActual])
        // Configurar UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Inicializamos los mediaPlayer
        mediaPlayerCorrecto = MediaPlayer.create(this, R.raw.correcto)
        mediaPlayerIncorrecto = MediaPlayer.create(this, R.raw.incorrecto)

        // Configurar botones
        val botonVoz = findViewById<ImageButton>(R.id.voz_btn_quiz2)
        botonVoz.setOnClickListener {
            iniciarReconocimientoDeVoz()
        }
        val btnSalir = findViewById<Button>(R.id.buttonSalirQuiz)
        // hacer clic en salir nos lleva a elegir tema del quiz
        btnSalir.setOnClickListener {
            countDownTimer?.cancel() // Detenemos el contador antes de terminar el activity
            val exitIntent = Intent(this, LoadingScreenActivity::class.java).apply {
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "Volviendo a elegir tema para quiz")

            }
            startActivity(exitIntent)
            finish()

        }
        val btnSiguiente = findViewById<Button>(R.id.buttonSiguientePregunta)

        val btnHelp = findViewById<ImageButton>(R.id.buttonHelp)

        val btnsOpciones = listOf(
            findViewById<Button>(R.id.buttonRespuesta1),
            findViewById<Button>(R.id.buttonRespuesta2),
            findViewById<Button>(R.id.buttonRespuesta3),
            findViewById<Button>(R.id.buttonRespuesta4)
        )
        // Desactivamos los botones para que no cometa errores de clic si el talkback esta activado
        if (talkBackActivo) {
            // Desactivar botón de siguiente pregunta
            btnSiguiente.isEnabled = false
            btnSiguiente.alpha = 0.5f // opcional: para que se vea desactivado
            btnSiguiente.isFocusable = false
            btnSiguiente.isFocusableInTouchMode = false
            btnSiguiente.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            // Desactivar botones de opciones
            btnsOpciones.forEach { it.isEnabled = false; it.alpha = 0.5f;it.isFocusable = false;
                it.isFocusableInTouchMode = false;
                it.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        }

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
        val textViewTiempoNumero = findViewById<TextView>(R.id.textViewTiempoNumero)
        val textViewTiempo = findViewById<TextView>(R.id.textViewTiempo)
        pregunta.text = "P${preguntaData.id}: ${preguntaData.pregunta}"
        botones.forEachIndexed { i, btn ->
            btn.text = "${'a' + i}) ${preguntaData.opciones[i]}"
            btn.setBackgroundResource(R.drawable.generic_button_selector)
        }
        textViewTiempo.text = "Pregunta ${indicePreguntaActual + 1} de ${preguntas.size}"

        opcionSeleccionada = null

        // Cancelar si hay temporizador activo
        countDownTimer?.cancel()

        // Iniciar temporizador
        countDownTimer = object : CountDownTimer(tiempoLimite, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                val minutos = segundosRestantes / 60
                val segundos = segundosRestantes % 60
                textViewTiempoNumero.text = String.format("%02d:%02d", minutos, segundos)
                if(segundosRestantes in 1..9){

                        vibrarLevemente()
                    }

            }

            override fun onFinish() {
                textViewTiempoNumero.text = "00:00"

                    countDownTimer?.cancel()
                    avanzarASiguientePregunta()


            }
        }.start()
        // Leeremos la pregunta y respuestas una vez que cambie a la siguiente pregunta
        if(talkBackActivo){
            leerPreguntaYOpciones()
            /*
            // Enfocar el botón de voz al cambiar de pregunta para que no lea el nombre de la app
            val botonVoz = findViewById<ImageButton>(R.id.voz_btn_quiz2)
            botonVoz.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED)
            */
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
        if (indiceSeleccionado == preguntaActual.respuesta_correcta) {
            mediaPlayerCorrecto?.start()
        } else {
            vibrarLevemente()
            mediaPlayerIncorrecto?.start()
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
                putExtra("TEMA",tema) // ENVIAMOS EL TEMA A LA PAGINA DE RESULTADOS
            }
            startActivity(intent)
            finish()// Terminamos el activity
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

    // enviaremos el intent para pedirle al usuario que envie el comando de voz
    private fun iniciarReconocimientoDeVoz() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el número de la opción que deseas responder o siguiente para pasar a la siguiente pregunta")

        try {
            startActivityForResult(intent, VOZ_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Reconocimiento de voz no soportado", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VOZ_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val resultados = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoReconocido = resultados?.get(0)?.lowercase() ?: return

            procesarComandoDeVoz(textoReconocido)
        }
    }
    private val opcionAExpresiones = listOf(
        "opcion a", "opción a", "primera opción", "opción 1", "opcion 1",
        "primera respuesta", "respuesta 1", "respuesta número 1","respuesta numero 1"
    )
    private val opcionBExpresiones = listOf(
        "opcion b", "opción b", "segunda opción", "opción 2","opcion 2",
        "segunda respuesta", "respuesta 2", "respuesta número 2","respuesta numero 2","opcion be"
    )
    private val opcionCExpresiones = listOf(
        "opcion c", "opción c", "tercera opción", "opción 3", "opcion 3",
        "tercera respuesta", "respuesta 3", "respuesta número 3","respuesta numero 3", "opcion ce"
    )
    private val opcionDExpresiones = listOf(
        "opcion d", "opción d", "cuarta opción", "opción 4", "opcion 4",
        "cuarta respuesta", "respuesta 4", "respuesta número 4","respuesta numero 4","opcion de"
    )

    private val continuarExpresiones = listOf(
        "siguiente", "continuar", "avanzar", "próxima", "siguiente pregunta"
    )
    private val salirExpresiones = listOf(
        "salir", "nuevo quiz", "iniciar de nuevo", "volver al inicio"
    )

    private fun normalizarTexto(texto: String): String {
        return texto.lowercase()
    }

    // Verificamos que los comandos se encuentren en la lista de opciones
    private fun procesarComandoDeVoz(texto: String) {
        Toast.makeText(this, "Reconocido: $texto", Toast.LENGTH_LONG).show()

        val textoNormalizado = normalizarTexto(texto)
        Log.d("VOZ_DEBUG", "Texto crudo: '$texto'")
        Log.d("VOZ_DEBUG", "Texto normalizado: '${normalizarTexto(texto)}'")
        Log.d("VOZ_DEBUG", "Comparando con expresiones A: $opcionAExpresiones")

        val botones = listOf(
            findViewById<Button>(R.id.buttonRespuesta1),
            findViewById<Button>(R.id.buttonRespuesta2),
            findViewById<Button>(R.id.buttonRespuesta3),
            findViewById<Button>(R.id.buttonRespuesta4)
        )

        when {
            opcionAExpresiones.any { textoNormalizado.contains(it) } -> {
                seleccionarOpcion(botones[0])
                avanzarTrasSeleccion()
            }
            opcionBExpresiones.any { textoNormalizado.contains(it) } -> {
                seleccionarOpcion(botones[1])
                avanzarTrasSeleccion()
            }
            opcionCExpresiones.any { textoNormalizado.contains(it) } -> {
                seleccionarOpcion(botones[2])
                avanzarTrasSeleccion()
            }
            opcionDExpresiones.any { textoNormalizado.contains(it) } -> {
                seleccionarOpcion(botones[3])
                avanzarTrasSeleccion()
            }
            continuarExpresiones.any { textoNormalizado.contains(it) } -> {
                // Comando para saltar pregunta sin responder
                if (opcionSeleccionada == null) {
                    countDownTimer?.cancel()
                    hablar("Pregunta omitida.")
                    avanzarASiguientePregunta()
                } else {
                    hablar("Ya has respondido esta pregunta.")
                }
            }
            // verificar comandos para salir
            salirExpresiones.any { textoNormalizado.contains(it) } -> {
                val exitIntent = Intent(this, LoadingScreenActivity::class.java).apply {
                    putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                    putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "Volviendo a elegir tema para quiz")

                }
                hablar("Regresando al inicio.")
                startActivity(exitIntent)
                finish()
            }

            else -> {
                hablar("No se reconoció el comando.")
            }
        }
    }
    // funcion para omitir pregunta y pasar a la siguiente por voz
    private fun avanzarTrasSeleccion() {
        countDownTimer?.cancel()
        hablar("Respuesta registrada. Avanzando.")
        avanzarASiguientePregunta()
    }


    private fun seleccionarOpcion(boton: Button) {
        opcionSeleccionada?.setBackgroundResource(R.drawable.generic_button_selector)
        boton.setBackgroundColor(Color.parseColor("#00FF41"))
        opcionSeleccionada = boton
        hablar("Opción seleccionada")
    }
    private fun vibrarLevemente() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            ) // 200ms de vibración
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    // Liberamos el texto a voz una vez que concluyo la actividad
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        //limpiamos los recursos de media player
        mediaPlayerCorrecto?.release()
        mediaPlayerIncorrecto?.release()
        mediaPlayerCorrecto = null
        mediaPlayerIncorrecto = null
        super.onDestroy()
    }
}
