package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoadingScreenActivity : AppCompatActivity() {
    private lateinit var loadingTextView: TextView // Referencia al TextView de carga

    companion object {
        const val EXTRA_DESTINATION_ACTIVITY_CLASS = "destination_activity_class"
        // Nueva clave para el mensaje de carga personalizado
        const val EXTRA_LOADING_MESSAGE = "loading_message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)

        loadingTextView = findViewById(R.id.loading_text) // Enlazar el TextView

        // Obtener el mensaje personalizado del Intent, si existe
        val customMessage = intent.getStringExtra(EXTRA_LOADING_MESSAGE)
        if (customMessage != null) {
            loadingTextView.text = customMessage // Establecer el texto personalizado
        } else {
            loadingTextView.text = "CARGANDO..." // Texto por defecto si no se pasa un mensaje
        }

        val loadingTimeMillis: Long = 3000 // 3 segundos (ajusta esto)
        val destinationClassName = intent.getStringExtra(EXTRA_DESTINATION_ACTIVITY_CLASS)
        val username = intent.getStringExtra(MainActivity.EXTRA_USERNAME)

        val resumen = intent.getStringExtra("RESUMEN_GENERADO")
        val tema = intent.getStringExtra("TEMA_ORIGINAL")
        val mandar_tema = intent.getStringExtra("TEMA")
        Handler(Looper.getMainLooper()).postDelayed({
            if (destinationClassName != null) {
                try {
                    val destinationClass = Class.forName(destinationClassName)
                    val intent = Intent(this, destinationClass).apply {
                        // **Nuevo: Pasa el nombre de usuario al nuevo Intent para la Activity de destino**
                        if (username != null) {
                            putExtra(MainActivity.EXTRA_USERNAME, username)
                        }

                        if (resumen != null) {
                            putExtra("RESUMEN_GENERADO", resumen)
                            putExtra("TEMA_ORIGINAL", tema)
                        }
                        if (mandar_tema!= null){
                            putExtra("TEMA",mandar_tema)
                        }
                    }
                    startActivity(intent)
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: Destino de carga no encontrado.", Toast.LENGTH_LONG).show()
                }
            } else {
                // Comportamiento por defecto si no hay destino especificado
                val defaultIntent = Intent(this, InicioActivity::class.java) // O la Activity que sea por defecto
                startActivity(defaultIntent)
            }
            finish()
        }, loadingTimeMillis)
    }
}