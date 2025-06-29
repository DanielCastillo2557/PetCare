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
import com.example.petcareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// import com.bumptech.glide.Glide

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

        imgFotoPerfil = findViewById(R.id.imgFotoPerfilCuidador)
        tvNombre = findViewById(R.id.tvNombreCuidador)
        tvEmail = findViewById(R.id.tvEmailCuidador)
        tvTelefono = findViewById(R.id.tvTelefonoCuidador)
        tvDireccion = findViewById(R.id.tvDireccionCuidador) // NUEVO: Inicializar el TextView
        btnEditarPerfil = findViewById(R.id.btnEditarPerfilCuidador)

        btnNavPerfilEnPerfil = findViewById(R.id.btnNavPerfilEnPerfil)
        btnNavMapaEnPerfil = findViewById(R.id.btnNavMapaEnPerfil)
        btnNavChatsEnPerfil = findViewById(R.id.btnNavChatsEnPerfil)

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
            val intent = Intent(this, EditarPerfilCuidadorActivity::class.java)
            editarPerfilLauncher.launch(intent)
        }

        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        // ... (sin cambios aquí) ...
        btnNavPerfilEnPerfil.setOnClickListener {
            Toast.makeText(this, "Ya estás en la pantalla de Perfil", Toast.LENGTH_SHORT).show()
        }

        btnNavMapaEnPerfil.setOnClickListener {
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnNavChatsEnPerfil.setOnClickListener {
            Toast.makeText(this, "Ir a Chats (no implementado)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.uid
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("PerfilCuidador", "Datos del documento: ${document.data}")

                    val nombreCompleto = document.getString("nombre") ?: "Nombre no disponible"
                    val apellido = document.getString("apellido") ?: ""
                    tvNombre.text = if (apellido.isNotBlank()) "$nombreCompleto $apellido" else nombreCompleto

                    tvEmail.text = document.getString("email") ?: currentUser.email ?: "Email no disponible"
                    tvTelefono.text = document.getString("telefono") ?: "Teléfono no disponible"
                    tvDireccion.text = document.getString("direccion") ?: "Dirección no disponible" // NUEVO: Cargar dirección

                    val fotoUrl = document.getString("fotoUrl")
                    if (!fotoUrl.isNullOrEmpty()) {
                        // Glide.with(this).load(fotoUrl).placeholder(R.drawable.ic_user).error(R.drawable.ic_user_error).into(imgFotoPerfil)
                        Toast.makeText(this, "Simulando carga de imagen desde URL", Toast.LENGTH_SHORT).show()
                        imgFotoPerfil.setImageResource(R.drawable.ic_user)
                    } else {
                        imgFotoPerfil.setImageResource(R.drawable.ic_user)
                    }
                } else {
                    Log.d("PerfilCuidador", "No se encontró el documento del usuario.")
                    Toast.makeText(this, "No se pudieron cargar los datos del perfil.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PerfilCuidador", "Error al obtener datos del usuario", exception)
                Toast.makeText(this, "Error al cargar el perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}