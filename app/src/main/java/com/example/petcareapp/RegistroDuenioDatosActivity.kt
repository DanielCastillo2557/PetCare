package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegistroDuenioDatosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_duenio_datos)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnGuardarRegistro: ImageButton = findViewById(R.id.btnGuardarRegistro)
        btnGuardarRegistro.setOnClickListener {
            registrarUsuario()

            //Intent para navegar a InicioDuenioActivity
            val intent = Intent(this, InicioDuenioActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroDuenio2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun registrarUsuario() {
        val nombre = intent.getStringExtra("nombre") ?: "" // recibido del activity anterior
        val email = findViewById<EditText>(R.id.editEmail).text.toString()
        val contrasena = findViewById<EditText>(R.id.editContrasena).text.toString()
        val confirmar = findViewById<EditText>(R.id.editRepetirContrasena).text.toString()
        val direccion = findViewById<EditText>(R.id.editDireccion).text.toString()
        val telefono = findViewById<EditText>(R.id.editNumero).text.toString()

        if (email.isBlank() || contrasena.isBlank() || contrasena != confirmar) {
            Toast.makeText(this, "Verifica los datos ingresados", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val usuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "telefono" to telefono,
                        "direccion" to direccion,
                        "tipo" to listOf("duenio"),
                        "fecha_creacion" to FieldValue.serverTimestamp()
                    )

                    db.collection("usuarios").document(uid).set(usuario)
                        .addOnSuccessListener {
                            guardarCredenciales(email, contrasena)
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Error al guardar en Firestore",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun guardarCredenciales(email: String, password: String) {
        val credentialManager = CredentialManager.create(this)
        val credential = CreatePasswordRequest(email, password)

        lifecycleScope.launch {
            try {
                credentialManager.createCredential(
                    request = credential,
                    context = this@RegistroDuenioDatosActivity
                )
                Log.d("CredentialManager", "Credenciales guardadas")
            } catch (e: Exception) {
                Log.e("CredentialManager", "No se pudieron guardar las credenciales: ${e.message}")
            }
        }
    }
}