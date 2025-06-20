package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.triviatrainerapp.MainActivity.Companion.EXTRA_USERNAME

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        val registrar_btn: Button = findViewById(R.id.regsitrar_btn)
        val usuarioInput: EditText = findViewById(R.id.usuario_input)
        val email_input: EditText = findViewById(R.id.email_input)
        val pass_input: EditText = findViewById(R.id.clave_input)
        val confir_pass_input: EditText = findViewById(R.id.confirmar_clave_input)

        val volver_btn: Button = findViewById(R.id.volver_btn)

        registrar_btn.setOnClickListener{

            val username = usuarioInput.text.toString().trim()
            val email = email_input.text.toString().trim()
            val pass = pass_input.text.toString().trim()
            val confir_pass = confir_pass_input.text.toString().trim()

            if( username.isEmpty() || email.isEmpty() || pass.isEmpty()|| confir_pass.isEmpty()){
                Toast.makeText(this, "Porfavor, no deje campos vacios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(pass != confir_pass){
                Toast.makeText(this, "Las claves tiene que ser iguales.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                // Pasa el nombre de la clase de destino como un extra
                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "REGISTRANDO E INICIANDO SESIÓN...")
                putExtra(EXTRA_USERNAME, username)
            }
            //Toast.makeText(this,"Empezando carga..",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }

        volver_btn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }
}