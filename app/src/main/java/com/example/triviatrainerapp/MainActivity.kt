package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        val button: Button = findViewById(R.id.ingresar_btn)

        button.setOnClickListener{

            val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                // Pasa el nombre de la clase de destino como un extra
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "INICIANDO SESIÓN...")
            }
            //Toast.makeText(this,"Empezando carga..",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }
}