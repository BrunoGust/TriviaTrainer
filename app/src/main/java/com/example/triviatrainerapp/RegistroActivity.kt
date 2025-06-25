package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.triviatrainerapp.MainActivity.Companion.EXTRA_USERNAME
import com.example.triviatrainerapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RegistroActivity : AppCompatActivity() {
    //lateinit var binding: ActivityMainBinding
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference: DatabaseReference = firebaseDatabase.getReference().child("Usuarios")

    // Referencias a los componentes del overlay de carga
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var loadingMessageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContentView(R.layout.activity_registro)


        val registrar_btn: Button = findViewById(R.id.regsitrar_btn)
        val usuarioInput: EditText = findViewById(R.id.usuario_input)
        val email_input: EditText = findViewById(R.id.email_input)
        val pass_input: EditText = findViewById(R.id.clave_input)
        val confir_pass_input: EditText = findViewById(R.id.confirmar_clave_input)
        val volver_btn: Button = findViewById(R.id.volver_btn)

        // Enlazar los componentes del overlay de carga
        loadingOverlay = findViewById(R.id.loading_overlay)
        loadingMessageTextView = findViewById(R.id.loading_message)

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

            // Mostrar la pantalla de carga
            showLoadingOverlay("REGISTRANDO USUARIO...")

            databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(this@RegistroActivity, "Este email ya está registrado.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Si el email no existe, proceder con el registro
                            insertarUsuario(Usuario(username, pass, email, "")) // La clave se llenará dentro de insertarUsuario
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Ocultar la pantalla de carga una vez que se recibe la respuesta de verificación
                        hideLoadingOverlay()
                        Toast.makeText(this@RegistroActivity, "Error al verificar email: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })



        }

        volver_btn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
    private fun insertarUsuario(u: Usuario){
        val newRef = databaseReference.push()
        val key = newRef.key
        if(key != null){
            u.clave = key
            newRef.setValue(u)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario registrado exitosamente.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, InicioActivity::class.java).apply { // Directo a InicioActivity
                        putExtra(MainActivity.EXTRA_USERNAME, u.username)
                    }
                    startActivity(intent)
                    hideLoadingOverlay()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            hideLoadingOverlay()
            Toast.makeText(this, "Error al generar clave de usuario.", Toast.LENGTH_SHORT).show()
        }
    }

    // Funciones para controlar la visibilidad del overlay de carga
    private fun showLoadingOverlay(message: String) {
        loadingMessageTextView.text = message
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        loadingOverlay.visibility = View.GONE
    }
}