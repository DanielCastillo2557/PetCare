package com.example.petcareapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilCuidadorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etNombreCompleto: TextInputEditText // Renombrado para claridad, usa el ID etNombreEditar

    private lateinit var etEmail: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var progressBar: ProgressBar

    private var currentNombreCompleto: String? = null // Renombrado
    // private var currentApellido: String? = null // ELIMINADO
    private var currentEmail: String? = null
    private var currentTelefono: String? = null
    private var currentDireccion: String? = null

    companion object {
        const val TAG = "EditarPerfilCuidador"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_cuidador) // Este es el XML modificado

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainEditarPerfil)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombreCompleto = findViewById(R.id.etNombreEditar) // El ID sigue siendo etNombreEditar en el XML

        etEmail = findViewById(R.id.etEmailEditar)
        etTelefono = findViewById(R.id.etTelefonoEditar)
        etDireccion = findViewById(R.id.etDireccionEditar)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        progressBar = findViewById(R.id.progressBarEditar)

        cargarDatosActuales()

        btnGuardarCambios.setOnClickListener {
            if (validarCampos()) {
                guardarCambios()
            }
        }
    }

    private fun cargarDatosActuales() {
        showLoading(true)
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            finish()
            return
        }

        db.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Asumimos que Firestore tiene un campo "nombre" que contiene el nombre completo
                    currentNombreCompleto = document.getString("nombre")

                    currentEmail = document.getString("email")
                    currentTelefono = document.getString("telefono")
                    currentDireccion = document.getString("direccion")

                    etNombreCompleto.setText(currentNombreCompleto)

                    etEmail.setText(currentEmail ?: currentUser.email)
                    etTelefono.setText(currentTelefono)
                    etDireccion.setText(currentDireccion)

                } else {
                    Toast.makeText(this, "No se pudieron cargar los datos.", Toast.LENGTH_SHORT).show()
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error al cargar datos", e)
                showLoading(false)
            }
    }

    private fun validarCampos(): Boolean {
        if (etNombreCompleto.text.toString().trim().isEmpty()) { // Validar etNombreCompleto
            etNombreCompleto.error = "El nombre completo no puede estar vacío"
            etNombreCompleto.requestFocus()
            return false
        }
        return true
    }

    private fun guardarCambios() {
        showLoading(true)
        val nuevoNombreCompleto = etNombreCompleto.text.toString().trim() // Obtener de etNombreCompleto

        val nuevoEmail = etEmail.text.toString().trim()
        val nuevoTelefono = etTelefono.text.toString().trim()
        val nuevaDireccion = etDireccion.text.toString().trim()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no encontrado.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        val updates = hashMapOf<String, Any>()
        // El campo en Firestore se llama "nombre" pero contiene el nombre completo
        if (nuevoNombreCompleto != currentNombreCompleto) updates["nombre"] = nuevoNombreCompleto

        if (nuevoEmail != currentEmail) updates["email"] = nuevoEmail
        if (nuevoTelefono != currentTelefono) updates["telefono"] = nuevoTelefono
        if (nuevaDireccion != currentDireccion) updates["direccion"] = nuevaDireccion

        if (updates.isEmpty()) {
            Toast.makeText(this, "No hay cambios para guardar.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            finish()
            return
        }

        db.collection("usuarios").document(currentUser.uid)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Perfil actualizado correctamente en Firestore.")
                Toast.makeText(this, "Perfil actualizado.", Toast.LENGTH_SHORT).show()

                // Actualizar el nombre para mostrar en Firebase Auth si cambió
                if (updates.containsKey("nombre")) { // la clave "nombre"
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nuevoNombreCompleto) // Usar nuevoNombreCompleto
                        .build()
                    currentUser.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Nombre para mostrar en Firebase Auth actualizado.")
                            }
                        }
                }
                setResult(Activity.RESULT_OK)
                showLoading(false)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar perfil", e)
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                showLoading(false)
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGuardarCambios.isEnabled = !isLoading
        etNombreCompleto.isEnabled = !isLoading

        etEmail.isEnabled = !isLoading
        etTelefono.isEnabled = !isLoading
        etDireccion.isEnabled = !isLoading
    }
}