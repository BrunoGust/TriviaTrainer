package com.example.triviatrainerapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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

class QuizActivity2 : AppCompatActivity() {
    private val respuestasUsuario = mutableListOf<RespuestaUsuario>()
    private var opcionSeleccionada: Button? = null
    private lateinit var preguntas: List<Pregunta>
    private var indicePreguntaActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz2)
        //Cargar Preguntas de archivo json
        preguntas = cargarPreguntasDesdeJSON()
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
            mostrarDialogoAyuda()
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
                val botones = listOf(
                    findViewById<Button>(R.id.buttonRespuesta1),
                    findViewById<Button>(R.id.buttonRespuesta2),
                    findViewById<Button>(R.id.buttonRespuesta3),
                    findViewById<Button>(R.id.buttonRespuesta4)
                )
                val indiceSeleccionado = botones.indexOf(opcionSeleccionada)
                val preguntaActual = preguntas[indicePreguntaActual]

                // Guardar respuesta
                respuestasUsuario.add(
                    RespuestaUsuario(
                        idPregunta = preguntaActual.id,
                        indiceSeleccionado = indiceSeleccionado,
                        indiceCorrecto = preguntaActual.respuesta_correcta
                    )
                )

                if (++indicePreguntaActual < preguntas.size) {
                    mostrarPregunta(preguntas[indicePreguntaActual])
                } else {
                    val intent = Intent(this, ResultadosQuizActivity::class.java).apply {
                        putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                        putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "CALCULANDO RESULTADOS...")
                        putParcelableArrayListExtra("respuestas_usuario", ArrayList(respuestasUsuario))
                    }
                    startActivity(intent)
                }
            } else {
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

        pregunta.text = "P${preguntaData.id}: ${preguntaData.pregunta}"
        botones.forEachIndexed { i, btn ->
            btn.text = "${'a' + i}) ${preguntaData.opciones[i]}"
            btn.setBackgroundResource(R.drawable.generic_button_selector)
        }

        opcionSeleccionada = null
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


}
