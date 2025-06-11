package com.example.petcareapp

import android.content.Intent // Necesario para la navegación
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView // Necesario para los botones de la barra de navegación
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// import com.bumptech.glide.Glide // Descomenta si usas Glide para imágenes de perfil

class PerfilCuidadorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Vistas para los datos del perfil
    private lateinit var imgFotoPerfil: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var btnEditarPerfil: Button

    // Vistas para los botones de la barra de navegación inferior
    private lateinit var btnNavPerfilEnPerfil: ImageView
    private lateinit var btnNavMapaEnPerfil: ImageView
    private lateinit var btnNavChatsEnPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_cuidador) // Asegúrate que este es el XML con la barra inferior

        // Manejo de WindowInsets para edge-to-edge
        // El padding se aplica al ConstraintLayout raíz (mainPerfilCuidador)
        // La barra inferior se anclará al fondo de este layout.
        // El NestedScrollView dentro se encargará del scroll de su contenido.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPerfilCuidador)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas de datos del perfil
        imgFotoPerfil = findViewById(R.id.imgFotoPerfilCuidador)
        tvNombre = findViewById(R.id.tvNombreCuidador)
        tvEmail = findViewById(R.id.tvEmailCuidador)
        tvTelefono = findViewById(R.id.tvTelefonoCuidador)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfilCuidador)

        // Inicializar botones de la barra de navegación inferior
        // Asegúrate de que estos IDs coincidan con los de tu activity_perfil_cuidador.xml
        btnNavPerfilEnPerfil = findViewById(R.id.btnNavPerfilEnPerfil)
        btnNavMapaEnPerfil = findViewById(R.id.btnNavMapaEnPerfil)
        btnNavChatsEnPerfil = findViewById(R.id.btnNavChatsEnPerfil)

        cargarDatosUsuario()

        btnEditarPerfil.setOnClickListener {
            // Lógica para ir a una pantalla de edición de perfil
            Toast.makeText(this, "Funcionalidad Editar Perfil no implementada", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, EditarPerfilCuidadorActivity::class.java)
            // startActivity(intent)
        }

        // --- Configurar OnClickListeners para la barra de navegación inferior ---
        btnNavPerfilEnPerfil.setOnClickListener {
            // Ya estás en la pantalla de Perfil.
            // Podrías no hacer nada, o quizás recargar los datos si fuera necesario.
            Toast.makeText(this, "Ya estás en la pantalla de Perfil", Toast.LENGTH_SHORT).show()
            // cargarDatosUsuario() // Ejemplo: si quisieras forzar una recarga
        }

        btnNavMapaEnPerfil.setOnClickListener {
            // Navegar a MapaCuidadorActivity
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            // Opcional: Flags para manejar la pila de actividades.
            // Por ejemplo, para evitar múltiples instancias del mapa o limpiar actividades superiores.
            // intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT // Trae la instancia existente al frente
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Más común para tabs
            startActivity(intent)
            // No llames a finish() aquí si quieres que el usuario pueda volver al perfil con el botón "atrás"
        }

        btnNavChatsEnPerfil.setOnClickListener {
            // Navegar a tu actividad de Chats (reemplaza 'ChatsCuidadorActivity' si es diferente)
            Toast.makeText(this, "Ir a Chats", Toast.LENGTH_SHORT).show() // Placeholder
            // val intent = Intent(this, ChatsCuidadorActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // startActivity(intent)
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            // Considerar redirigir al Login:
            // startActivity(Intent(this, LoginActivity::class.java))
            // finishAffinity() // Cierra todas las actividades de esta tarea
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

                    val fotoUrl = document.getString("fotoUrl")
                    if (!fotoUrl.isNullOrEmpty()) {
                        // Glide.with(this).load(fotoUrl).placeholder(R.drawable.ic_user).into(imgFotoPerfil)
                        Toast.makeText(this, "Cargar imagen desde URL: $fotoUrl", Toast.LENGTH_LONG).show()
                    } else {
                        imgFotoPerfil.setImageResource(R.drawable.ic_user) // Placeholder por defecto
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