package com.example.triviatrainerapp

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ResultadosQuizActivity : AppCompatActivity() {

    private lateinit var preguntas: List<Pregunta>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados_quiz)

        preguntas = cargarPreguntasDesdeAssets()

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
            // Buscamos la pregunta por id en vez de index directo
            val preguntaObj = preguntas.find { it.id == respuesta.idPregunta }
            val preguntaTexto = preguntaObj?.pregunta ?: "Pregunta desconocida"
            val opciones = preguntaObj?.opciones ?: emptyList()
            val indiceCorrecto = preguntaObj?.respuesta_correcta ?: -1
            val seleccionUsuario = respuesta.indiceSeleccionado

            val textoResultado = StringBuilder()
            textoResultado.append("P${respuesta.idPregunta}: $preguntaTexto\n")
            textoResultado.append("Tu respuesta: ")

            val textoRespuestaUsuario = opciones.getOrNull(seleccionUsuario) ?: "Respuesta desconocida"
            textoResultado.append(textoRespuestaUsuario)

            val esCorrecta = seleccionUsuario == indiceCorrecto
            if (esCorrecta) correctas++

            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }

                setPadding(12, 12, 12, 12)
                text = textoResultado.toString()
                textSize = 18f
                setTextColor(if (esCorrecta) Color.parseColor("#004D00") else Color.parseColor("#B00020"))
                setBackgroundColor(if (esCorrecta) Color.parseColor("#C8E6C9") else Color.parseColor("#FFCDD2"))
            }

            layoutResultados.addView(textView)
        }

        textViewCantidadCorrectas.text = "OBTUVISTE $correctas DE ${respuestasUsuario.size} CORRECTAS!"
        textViewCantidadCorrectas.setTextColor(if (correctas == respuestasUsuario.size) Color.parseColor("#90EE90") else Color.RED)
    }

    private fun cargarPreguntasDesdeAssets(): List<Pregunta> {
        val jsonString = assets.open("questions.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val tipoLista = object : TypeToken<List<Pregunta>>() {}.type
        return gson.fromJson(jsonString, tipoLista)
    }
}
