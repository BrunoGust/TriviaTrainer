package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoadingScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_screen)


        val loadingTimeMillis: Long = 3000

        Handler(Looper.getMainLooper()).postDelayed({
            // Crea un Intent para iniciar InicioActivity
            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)

            // Cierra esta LoadingScreenActivity para que el usuario no pueda volver con el botón 'Atrás'
            finish()
        }, loadingTimeMillis)
    }
}