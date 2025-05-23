package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegistroCuidadorDatosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cuidador_datos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroCuidador2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val btnGuardar: ImageButton = findViewById(R.id.btnGuardarRegistroCuidador)

        //Boton siguiente
        btnGuardar.setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmailCuidador).text.toString()
            val contrasena = findViewById<EditText>(R.id.editContrasenaCuidador).text.toString()
            val repetir = findViewById<EditText>(R.id.editRepetirContrasenaCuidador).text.toString()
            val numero = findViewById<EditText>(R.id.editNumeroCuidador).text.toString()
            val direccion = findViewById<EditText>(R.id.editDireccionCuidador).text.toString()

            if (email.isBlank() || contrasena != repetir) {
                Toast.makeText(this, "Revisa los datos ingresados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, contrasena)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    // 1. Registro en "usuarios"
                    val datosUsuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "telefono" to numero,
                        "direccion" to direccion,
                        "tipo" to listOf("cuidador"),
                        "fecha_creacion" to FieldValue.serverTimestamp()
                    )

                    db.collection("usuarios").document(uid).set(datosUsuario)
                        .addOnSuccessListener {
                            // 2. Registro en "cuidadores"
                            val datosCuidador = hashMapOf(
                                "idusuario" to uid,
                                "descripcion" to descripcion
                            )

                            db.collection("cuidadores").document(uid).set(datosCuidador)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT)
                                        .show()
                                    val intent = Intent(this, InicioCuidadorActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al guardar cuidador",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}