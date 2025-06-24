package com.example.triviatrainerapp

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ResultadosQuizActivity : AppCompatActivity() {

    private lateinit var preguntas: List<Pregunta>

    private lateinit var btnAssistant: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_quiz)

        preguntas = cargarPreguntasDesdeFile()


        val layoutResultados = findViewById<LinearLayout>(R.id.layoutResultados)
        val textViewCantidadCorrectas = findViewById<TextView>(R.id.textViewCantidadCorrectas)

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

            // 📄 Construir contenido con formato HTML
            val textoResultadoHtml = buildString {
                append("<b>P${respuesta.idPregunta}:</b> $preguntaTexto<br>")
                append("<b>Tu respuesta:</b> $textoRespuestaUsuario<br>")
                if (!esCorrecta) {
                    val textoCorrecto = opciones.getOrNull(indiceCorrecto) ?: "No disponible"
                    append("<b><font color='#B00020'>Respuesta correcta:</font></b> $textoCorrecto<br>")
                    append("<b><font color='#B00020'>Fuente:</font></b> <a href=\"$fuente\"><font color='#000000'>$fuente</font></a>")
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

            layoutResultados.addView(textView)
        }



        textViewCantidadCorrectas.text = "OBTUVISTE $correctas DE ${respuestasUsuario.size} CORRECTAS!"
        textViewCantidadCorrectas.setTextColor(if (correctas == respuestasUsuario.size) Color.parseColor("#90EE90") else Color.RED)

        btnAssistant.setOnClickListener {
            val dialog = AssistantDialogFragment()
            dialog.arguments = Bundle().apply {
                putString("pantalla", "resultados")
            }
            dialog.show(supportFragmentManager, "AssistantDialog")
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

}
