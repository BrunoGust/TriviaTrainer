package com.example.triviatrainerapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ResultadosQuizActivity : AppCompatActivity() {

    private lateinit var preguntas: List<Pregunta>

    private lateinit var btnAssistant: ImageButton
    val fuentes: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_quiz)

        preguntas = cargarPreguntasDesdeFile()
        btnAssistant = findViewById(R.id.buttonHelpResultados)
        val tema  =intent.getStringExtra("TEMA")

        val layoutResultados = findViewById<LinearLayout>(R.id.layoutResultados)
        val textViewCantidadCorrectas = findViewById<TextView>(R.id.textViewCantidadCorrectas)
        val textViewSaludosUsuario = findViewById<TextView>(R.id.textViewsaludoQuizResultados)
        textViewSaludosUsuario.text = "RESULTADOS"
        val textViewTema = findViewById<TextView>(R.id.textViewTituloQuiz)
        textViewTema.text = "Tema: $tema"
        val respuestasUsuario = intent.getParcelableArrayListExtra<RespuestaUsuario>("respuestas_usuario")

        if (respuestasUsuario == null || respuestasUsuario.isEmpty()) {
            textViewCantidadCorrectas.text = "No se recibieron respuestas."
            return
        }

        var correctas = 0
        layoutResultados.removeAllViews()

        for (respuesta in respuestasUsuario) {
            val preguntaObj = preguntas.find { it.id == respuesta.idPregunta }
            val preguntaTexto = preguntaObj?.pregunta ?: "Pregunta desconocida"
            val opciones = preguntaObj?.opciones ?: emptyList()
            val indiceCorrecto = preguntaObj?.respuesta_correcta ?: -1
            val fuente = preguntaObj?.fuente ?: "Fuente no disponible"
            val seleccionUsuario = respuesta.indiceSeleccionado
            val textoRespuestaUsuario = opciones.getOrNull(seleccionUsuario) ?: "Respuesta desconocida"

            val esCorrecta = seleccionUsuario == indiceCorrecto
            if (esCorrecta) correctas++

            // Creamos el resultado para las preguntas incorrectas
            val textoResultadoHtml = buildString {
                append("<b>P${respuesta.idPregunta}:</b> $preguntaTexto<br>")
                append("<b>Tu respuesta:</b> $textoRespuestaUsuario<br>")
                if (!esCorrecta) {

                    val textoCorrecto = opciones.getOrNull(indiceCorrecto) ?: "No disponible"
                    append("<b><font color='#B00020'>Respuesta correcta:</font></b> $textoCorrecto<br>")
                    //append("<b><font color='#B00020'>Fuente:</font></b> <a href=\"$fuente\"><font color='#000000'>$fuente</font></a>")
                }

            }


            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24 }

                setPadding(12, 12, 12, 12)
                text = HtmlCompat.fromHtml(textoResultadoHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                movementMethod = LinkMovementMethod.getInstance() // Hace clicable el link si es URL
                textSize = 18f
                setTextColor(if (esCorrecta) Color.parseColor("#004D00") else Color.parseColor("#B00020"))
                setBackgroundColor(if (esCorrecta) Color.parseColor("#C8E6C9") else Color.parseColor("#FFCDD2"))
            }
            if (fuente.isNotBlank()) { // Agregamos todas las fuentes utilizadas en las preguntas
                fuentes.add(fuente) // solo se agrega si no existe
            }
            layoutResultados.addView(textView)
        }



        textViewCantidadCorrectas.text = "OBTUVISTE $correctas DE ${respuestasUsuario.size} CORRECTAS!"
        // esto enviara el focus del talkback para que lea el numero de preguntas correctas
        textViewCantidadCorrectas.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)

        textViewCantidadCorrectas.setTextColor(if (correctas == respuestasUsuario.size) Color.parseColor("#90EE90") else Color.RED)

        btnAssistant.setOnClickListener {
            val dialog = AssistantDialogFragment()
            dialog.arguments = Bundle().apply {
                putString("pantalla", "resultados")
            }
            dialog.show(supportFragmentManager, "AssistantDialog")
        }
        val botonVoz = findViewById<ImageButton>(R.id.voz_btn_resultados_quiz)
        botonVoz.setOnClickListener {
            iniciarReconocimientoDeVoz()
        }
        val botonProfundizarMas = findViewById<Button>(R.id.buttonNuevoQuiz)
        botonProfundizarMas.setOnClickListener{
            val dialog = FuentesDialogFragment()
            dialog.arguments = Bundle().apply {
                putString("tema", tema)
                putStringArrayList("fuentes", ArrayList(fuentes))
            }
            dialog.show(supportFragmentManager, "FuentesDialog")
        }
        // Nuevo Quiz y Salir nos llevan al mismo lugar?
        val botonSalirQuizResultados = findViewById<Button>(R.id.buttonSalirQuizResultados)
        botonSalirQuizResultados.setOnClickListener{
            val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "Volviendo al inicio para elegir tema")
            }
            startActivity(intent)
            finish()
        }
        val botonNuevoQuiz = findViewById<Button>(R.id.buttonEmpezarNuevoQuiz)
        botonNuevoQuiz.setOnClickListener{
            val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "Volviendo al inicio para elegir tema")
            }
            startActivity(intent)
            finish()
        }

    }
    private fun iniciarReconocimientoDeVoz() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "¿Qué deseas hacer?")
        }
        try {
            startActivityForResult(intent, VOZ_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarPreguntasDesdeAssets(): List<Pregunta> {
        val jsonString = assets.open("questions.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val tipoLista = object : TypeToken<List<Pregunta>>() {}.type
        return gson.fromJson(jsonString, tipoLista)
    }

    private fun cargarPreguntasDesdeFile(): List<Pregunta> {
        val file = File(filesDir, "questions.json")
        if (!file.exists()) return emptyList()

        val json = file.readText()
        val tipo = object : TypeToken<List<Pregunta>>() {}.type
        return Gson().fromJson(json, tipo)
    }
    private val comandosProfundizar = listOf("profundizar más", "quiero saber más", "más información","quiero saber mas","mas informacion")
    private val comandosSalir = listOf("salir", "nuevo quiz", "intentar de nuevo", "volver al inicio")
    private val VOZ_REQUEST_CODE = 200
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOZ_REQUEST_CODE && resultCode == RESULT_OK) {
            val resultados = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val texto = resultados?.get(0)?.lowercase() ?: return
            procesarComandoDeVoz(texto)
        }
    }
    private fun procesarComandoDeVoz(texto: String) {
        when {
            comandosProfundizar.any { texto.contains(it) } -> {
                findViewById<TextView>(R.id.buttonNuevoQuiz)?.performClick()
            }
            comandosSalir.any { texto.contains(it) } -> {
                val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                    putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                    putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "Volviendo al inicio para elegir tema")
                }
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(this, "No se reconoció el comando", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
