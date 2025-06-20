package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }
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
        val usuarioInput: EditText = findViewById(R.id.usuario_input)
        val registrar_btn: Button = findViewById(R.id.registrar_btn)

        button.setOnClickListener{
            val username = usuarioInput.text.toString().trim() // Obtener el texto del EditText del usuario

            if (username.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un nombre de usuario.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Salir de la función si el campo está vacío
            }

            val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                // Pasa el nombre de la clase de destino como un extra
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "INICIANDO SESIÓN...")
                putExtra(EXTRA_USERNAME, username)
            }
            //Toast.makeText(this,"Empezando carga..",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
        registrar_btn.setOnClickListener{
            val intent = Intent(this, RegistroActivity::class.java)
            //Toast.makeText(this,"Empezando carga..",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }
}