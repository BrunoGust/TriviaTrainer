package com.example.triviatrainerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.triviatrainerapp.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference: DatabaseReference =
        firebaseDatabase.getReference().child("Usuarios")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        val ingresar_btn: Button = findViewById(R.id.ingresar_btn)
        val usuarioInput: EditText = findViewById(R.id.usuario_input)
        val claveInput: EditText = findViewById(R.id.clave_input)
        val registrar_btn: Button = findViewById(R.id.registrar_btn)

        ingresar_btn.setOnClickListener {
            val inputIdentifier =
                usuarioInput.text.toString().trim() // Obtener el texto del EditText del usuario
            val password = claveInput.text.toString().trim()

            if (inputIdentifier.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un nombre de usuario.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener // Salir de la función si el campo está vacío
            }

            // Realizar la búsqueda en Firebase
            // Intentaremos buscar por email primero
            databaseReference.orderByChild("email").equalTo(inputIdentifier)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("MainActivity", "Email search onDataChange: ${snapshot.value}")
                        var loginSuccessful = false // Bandera para saber si el login fue exitoso

                        if (snapshot.exists()) {
                            // Usuario(s) encontrado(s) por email
                            for (userSnapshot in snapshot.children) {
                                try {
                                    val user = userSnapshot.getValue(Usuario::class.java)
                                    if (user != null) {
                                        Log.d("MainActivity", "User found by email: ${user.username}, Email: ${user.email}, Pass: ${user.password}")
                                        if (user.password == password) {
                                            // Contraseña correcta
                                            Toast.makeText(this@MainActivity, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@MainActivity, LoadingScreenActivity::class.java).apply {
                                                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                                                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "INICIANDO SESIÓN...")
                                                putExtra(EXTRA_USERNAME, user.username)
                                            }
                                            startActivity(intent)
                                            finish()
                                            loginSuccessful = true // Marcar como exitoso
                                            return // Salir del listener y el bucle
                                        }
                                    } else {
                                        Log.e("MainActivity", "Failed to deserialize Usuario object for email snapshot: ${userSnapshot.key}. Data: ${userSnapshot.value}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error deserializing user data for email snapshot ${userSnapshot.key}: ${e.message}", e)
                                }
                            }
                            // Si el bucle termina y el login no fue exitoso, es porque la contraseña era incorrecta
                            if (!loginSuccessful) {
                                Toast.makeText(this@MainActivity, "Contraseña incorrecta.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Usuario no encontrado por email. Intentar buscar por username.
                            Log.d("MainActivity", "Email not found, trying username search for: $inputIdentifier")
                            databaseReference.orderByChild("username").equalTo(inputIdentifier)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(usernameSnapshot: DataSnapshot) {
                                        Log.d("MainActivity", "Username search onDataChange: ${usernameSnapshot.value}")
                                        var usernameLoginSuccessful = false // Bandera para la búsqueda por username

                                        if (usernameSnapshot.exists()) {
                                            // Usuario(s) encontrado(s) por username
                                            for (userSnapshot in usernameSnapshot.children) {
                                                try {
                                                    val user = userSnapshot.getValue(Usuario::class.java)
                                                    if (user != null) {
                                                        Log.d("MainActivity", "User found by username: ${user.username}, Email: ${user.email}, Pass: ${user.password}")
                                                        if (user.password == password) {
                                                            Toast.makeText(this@MainActivity, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()
                                                            val intent = Intent(this@MainActivity, LoadingScreenActivity::class.java).apply {
                                                                putExtra(LoadingScreenActivity.EXTRA_DESTINATION_ACTIVITY_CLASS, InicioActivity::class.java.name)
                                                                putExtra(LoadingScreenActivity.EXTRA_LOADING_MESSAGE, "INICIANDO SESIÓN...")
                                                                putExtra(EXTRA_USERNAME, user.username)
                                                            }
                                                            startActivity(intent)
                                                            finish()
                                                            usernameLoginSuccessful = true // Marcar como exitoso
                                                            return // Salir del listener y el bucle
                                                        }
                                                    } else {
                                                        Log.e("MainActivity", "Failed to deserialize Usuario object for username snapshot: ${userSnapshot.key}. Data: ${userSnapshot.value}")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("MainActivity", "Error deserializing user data for username snapshot ${userSnapshot.key}: ${e.message}", e)
                                                }
                                            }
                                            // Si el bucle termina y el login no fue exitoso, es porque la contraseña era incorrecta
                                            if (!usernameLoginSuccessful) {
                                                Toast.makeText(this@MainActivity, "Contraseña incorrecta.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // Usuario no encontrado ni por email ni por username
                                            Toast.makeText(this@MainActivity, "Usuario o email no registrado.", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@MainActivity, "Error en la búsqueda de usuario por username: ${error.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("MainActivity", "Firebase query onCancelled (username): ${error.message}", error.toException())
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Error en la búsqueda de usuario por email: ${error.message}", Toast.LENGTH_SHORT).show()
                        Log.e("MainActivity", "Firebase query onCancelled (email): ${error.message}", error.toException())
                    }
                })
        }


        registrar_btn.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            //Toast.makeText(this,"Empezando carga..",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }
}