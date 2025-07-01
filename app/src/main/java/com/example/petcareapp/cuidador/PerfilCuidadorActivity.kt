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
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< DESCOMENTAR/AÑADIR IMPORTACIÓN
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

    private lateinit var btnNavPerfilEnPerfil: ImageView
    private lateinit var btnNavMapaEnPerfil: ImageView
    private lateinit var btnNavChatsEnPerfil: ImageView

    private lateinit var editarPerfilLauncher: ActivityResultLauncher<Intent>

    // Variables para almacenar los datos actuales del usuario y pasarlos a la edición
    private var currentNombre: String? = null
    private var currentApellido: String? = null
    private var currentTelefono: String? = null
    private var currentDireccion: String? = null
    private var currentFotoUrl: String? = null
    // El email usualmente se toma de FirebaseAuth o del documento, no se suele editar directamente.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPerfilCuidador)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
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

        // Navegación inferior
        btnNavPerfilEnPerfil = findViewById(R.id.btnNavPerfilEnPerfil)
        btnNavMapaEnPerfil = findViewById(R.id.btnNavMapaEnPerfil)
        btnNavChatsEnPerfil = findViewById(R.id.btnNavChatsEnPerfil)

        editarPerfilLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Perfil actualizado. Recargando datos...", Toast.LENGTH_SHORT).show()
                cargarDatosUsuario() // Recarga los datos desde Firestore
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

        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        btnNavPerfilEnPerfil.setOnClickListener {
            // Ya está en perfil, no hacer nada o mostrar un Toast
            Toast.makeText(this, "Ya estás en la pantalla de Perfil", Toast.LENGTH_SHORT).show()
        }

        btnNavMapaEnPerfil.setOnClickListener {
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            // Flags para evitar múltiples instancias de la misma actividad si ya está en la pila
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            // No es común hacer finish() aquí a menos que quieras que el usuario no pueda volver al perfil con el botón atrás.
        }

        btnNavChatsEnPerfil.setOnClickListener {
            // Aquí iría la lógica para abrir la actividad de Chats
            // val intent = Intent(this, ChatsCuidadorActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Ir a Chats (no implementado)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado. Redirigiendo al login...", Toast.LENGTH_LONG).show()
            // Aquí deberías redirigir al usuario a tu LoginActivity
            // val intent = Intent(this, LoginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
            // finish()
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
                    currentFotoUrl = document.getString("foto_url") // <<<<<< CORREGIDO a foto_url

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