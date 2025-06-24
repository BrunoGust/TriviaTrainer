package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
    var keyTemporal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_registro)
        //setContentView(binding.root)
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
                    val intent = Intent(this, LoadingScreenActivity::class.java).apply {
                        putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                        putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "REGISTRANDO E INICIANDO SESIÓN...")
                        putExtra(EXTRA_USERNAME, u.username) // Pasa el username real del objeto Usuario
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error al generar clave de usuario.", Toast.LENGTH_SHORT).show()
        }
    }
    fun insertar(u: Usuario){
        val key = databaseReference.push().key
        if(key!=null){
            u.clave = key
            databaseReference.push().setValue(u)
        }
    }
}