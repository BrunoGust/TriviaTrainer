package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RepasoActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
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

        val button: Button = findViewById(R.id.btnEmpezarQuiz)
        welcomeTextView = findViewById(R.id.textView4)
        val username = intent.getStringExtra(MainActivity.EXTRA_USERNAME)
        if (username != null && username.isNotEmpty()) {
            welcomeTextView.text = "Saludos, $username!" // Actualizar el texto
        } else {
            welcomeTextView.text = "Saludos, USUARIO!" // O mantener el por defecto si no hay nombre
        }

        button.setOnClickListener{

            val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, QuizActivity2::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "GENERANDO QUIZ...")
            }
            //Toast.makeText(this,"Empezando carga..", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }



    }
}