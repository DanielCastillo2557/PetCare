package com.example.petcareapp.cuidador

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.petcareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilCuidadorActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var imgFotoPerfil: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var btnEditarPerfil: Button
    private lateinit var btnVolver: ImageView // Declara el botón de volver
    private lateinit var editarPerfilLauncher: ActivityResultLauncher<Intent>
    private lateinit var switchDisponibilidad: androidx.appcompat.widget.SwitchCompat

    // Variables para almacenar los datos actuales del usuario y pasarlos a la edición
    private var currentNombre: String? = null
    private var currentApellido: String? = null
    private var currentTelefono: String? = null
    private var currentDireccion: String? = null
    private var currentFotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPerfilCuidador)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplica padding a todos los lados del contenedor principal
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicialización de Vistas
        imgFotoPerfil = findViewById(R.id.imgFotoPerfilCuidador)
        tvNombre = findViewById(R.id.tvNombreCuidador)
        tvEmail = findViewById(R.id.tvEmailCuidador)
        tvTelefono = findViewById(R.id.tvTelefonoCuidador)
        tvDireccion = findViewById(R.id.tvDireccionCuidador)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfilCuidador)
        switchDisponibilidad = findViewById(R.id.switchDisponibilidad)

        val uid = auth.currentUser?.uid
        // Leer el estado actual
        if (uid != null) {
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val disponible = doc.getBoolean("disponible") ?: false
                    switchDisponibilidad.isChecked = disponible
                }
        }

        // Guardar cuando se cambie el switch
        switchDisponibilidad.setOnCheckedChangeListener { _, isChecked ->
            if (uid != null) {
                db.collection("usuarios").document(uid)
                    .update("disponible", isChecked)
                    .addOnSuccessListener {
                        Toast.makeText(this, if (isChecked) "Ahora estás disponible" else "No disponible", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar disponibilidad", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        btnVolver = findViewById(R.id.btnVolverDesdePerfilCuidador) // Inicializa el botón de volver

        editarPerfilLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Perfil actualizado. Recargando datos...", Toast.LENGTH_SHORT).show()
                cargarDatosUsuario()
            } else {
                Log.d("PerfilCuidador", "Edición cancelada o fallida, código: ${result.resultCode}")
            }
        }

        cargarDatosUsuario()

        btnEditarPerfil.setOnClickListener {
            // Asegurarse de que los datos se hayan cargado antes de intentar iniciar la edición
            // Puedes usar cualquier variable que sepas que se carga, como currentNombre
            if (currentNombre != null || currentFotoUrl != null || currentTelefono != null || currentDireccion != null) {
                val intent = Intent(this, EditarPerfilCuidadorActivity::class.java).apply {
                    putExtra(EditarPerfilCuidadorActivity.EXTRA_NOMBRE, currentNombre)

                    putExtra(EditarPerfilCuidadorActivity.EXTRA_TELEFONO, currentTelefono)
                    putExtra(EditarPerfilCuidadorActivity.EXTRA_DIRECCION, currentDireccion)
                    putExtra(EditarPerfilCuidadorActivity.EXTRA_FOTO_URL, currentFotoUrl)
                }
                editarPerfilLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Cargando datos del perfil, por favor espera.", Toast.LENGTH_SHORT).show()
                // Opcionalmente, intentar cargar de nuevo si currentNombre es null
                // cargarDatosUsuario()
            }
        }

        // Listener para el botón de volver
        btnVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // O finish() si prefieres
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado. Redirigiendo al login...", Toast.LENGTH_LONG).show()

            return
        }

        val uid = currentUser.uid
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("PerfilCuidador", "Datos del documento: ${document.data}")

                    // Guardar los datos actuales en las variables de la clase
                    currentNombre = document.getString("nombre")
                    currentApellido = document.getString("apellido")
                    currentTelefono = document.getString("telefono")
                    currentDireccion = document.getString("direccion")
                    currentFotoUrl = document.getString("foto_url")

                    // Actualizar UI
                    val nombreCompleto = currentNombre ?: ""
                    val apellido = currentApellido ?: ""
                    tvNombre.text = if (apellido.isNotBlank()) "$nombreCompleto $apellido" else nombreCompleto.ifBlank { "Nombre no disponible" }

                    tvEmail.text = document.getString("email") ?: currentUser.email ?: "Email no disponible"
                    tvTelefono.text = currentTelefono ?: "Teléfono no disponible"
                    tvDireccion.text = currentDireccion ?: "Dirección no disponible"

                    if (!currentFotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(currentFotoUrl)
                            .placeholder(R.drawable.ic_user) // Imagen mientras carga
                            .error(R.drawable.ic_user)       // Imagen si hay error
                            .circleCrop() // Si quieres la imagen circular (asegúrate que tu ImageView lo soporte o usa CircleImageView)
                            .into(imgFotoPerfil)
                    } else {
                        imgFotoPerfil.setImageResource(R.drawable.ic_user) // Imagen por defecto
                    }
                } else {
                    Log.d("PerfilCuidador", "No se encontró el documento del usuario.")
                    Toast.makeText(this, "No se pudieron cargar los datos del perfil.", Toast.LENGTH_SHORT).show()
                    // Podrías setear valores por defecto en la UI aquí si lo deseas
                    tvNombre.text = "N/A"
                    tvEmail.text = currentUser.email ?: "N/A"
                    tvTelefono.text = "N/A"
                    tvDireccion.text = "N/A"
                    imgFotoPerfil.setImageResource(R.drawable.ic_user)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PerfilCuidador", "Error al obtener datos del usuario", exception)
                Toast.makeText(this, "Error al cargar el perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
                // Podrías setear valores de error en la UI aquí
                tvNombre.text = "Error"
                tvEmail.text = "Error"
                tvTelefono.text = "Error"
                tvDireccion.text = "Error"
                imgFotoPerfil.setImageResource(R.drawable.ic_user) // Podrías tener un drawable específico para error
            }
    }
}