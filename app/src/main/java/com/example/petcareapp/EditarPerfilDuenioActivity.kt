package com.example.petcareapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText // Sigue siendo necesario para otros campos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilDuenioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUserUid: String? = null

    private lateinit var etNombreEditar: TextInputEditText

    private lateinit var etTelefonoEditar: TextInputEditText
    private lateinit var etDireccionEditar: TextInputEditText
    private lateinit var btnGuardarCambiosPerfil: Button
    private lateinit var btnVolverDesdeEditarPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_duenio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserUid = auth.currentUser?.uid

        if (currentUserUid == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        etNombreEditar = findViewById(R.id.etNombreEditar)
        // etEmailEditar = findViewById(R.id.etEmailEditar) // ELIMINAR ESTA LÍNEA
        etTelefonoEditar = findViewById(R.id.etTelefonoEditar)
        etDireccionEditar = findViewById(R.id.etDireccionEditar)
        btnGuardarCambiosPerfil = findViewById(R.id.btnGuardarCambiosPerfil)
        btnVolverDesdeEditarPerfil = findViewById(R.id.btnVolverDesdeEditarPerfil)

        // Recuperar y mostrar datos actuales
        val nombreActual = intent.getStringExtra("NOMBRE_ACTUAL")
        val telefonoActual = intent.getStringExtra("TELEFONO_ACTUAL")
        val direccionActual = intent.getStringExtra("DIRECCION_ACTUAL")

        etNombreEditar.setText(nombreActual)
        etTelefonoEditar.setText(telefonoActual)
        etDireccionEditar.setText(direccionActual)

        btnVolverDesdeEditarPerfil.setOnClickListener {
            finish()
        }

        btnGuardarCambiosPerfil.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = etNombreEditar.text.toString().trim()
        val nuevoTelefono = etTelefonoEditar.text.toString().trim()
        val nuevaDireccion = etDireccionEditar.text.toString().trim()

        if (nuevoNombre.isEmpty()) {
            etNombreEditar.error = "El nombre no puede estar vacío"
            etNombreEditar.requestFocus()
            return
        }

        val updates = hashMapOf<String, Any>(
            "nombre" to nuevoNombre,
            "telefono" to nuevoTelefono,
            "direccion" to nuevaDireccion
        )

        currentUserUid?.let { uid ->
            db.collection("usuarios").document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el perfil: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }
}