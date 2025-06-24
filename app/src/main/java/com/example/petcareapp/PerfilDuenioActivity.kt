package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilDuenioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvEmailUsuario: TextView
    private lateinit var tvTelefonoUsuario: TextView
    private lateinit var tvDireccionUsuario: TextView
    private lateinit var imgFotoPerfil: ImageView
    private lateinit var btnEditarPerfil: Button
    private lateinit var btnVolverDesdePerfil: ImageView

    // Variables para almacenar los datos actuales y pasarlos
    private var nombreActual: String? = null
    private var emailActual: String? = null
    private var telefonoActual: String? = null
    private var direccionActual: String? = null
    // private var fotoUrlActual: String? = null // Si también manejas la foto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_duenio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        tvEmailUsuario = findViewById(R.id.tvEmailUsuario)
        tvTelefonoUsuario = findViewById(R.id.tvTelefonoUsuario)
        tvDireccionUsuario = findViewById(R.id.tvDireccionUsuario)
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil)
        btnVolverDesdePerfil = findViewById(R.id.btnVolverDesdePerfil)

        cargarDatosPerfil()

        btnEditarPerfil.setOnClickListener {
            // Crear Intent para EditarPerfilDuenioActivity
            val intent = Intent(this, EditarPerfilDuenioActivity::class.java)

            // (Opcional pero recomendado) Pasar datos actuales a la actividad de edición
            intent.putExtra("NOMBRE_ACTUAL", nombreActual)
            intent.putExtra("EMAIL_ACTUAL", emailActual) // El email usualmente no se edita si es el de login
            intent.putExtra("TELEFONO_ACTUAL", telefonoActual)
            intent.putExtra("DIRECCION_ACTUAL", direccionActual)
            // intent.putExtra("FOTO_URL_ACTUAL", fotoUrlActual)

            startActivity(intent)
        }

        btnVolverDesdePerfil.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosPerfil() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nombreActual = document.getString("nombre") // Guardar para pasar
                    emailActual = document.getString("email")   // Guardar para pasar
                    telefonoActual = document.getString("telefono") // Guardar para pasar
                    direccionActual = document.getString("direccion") // Guardar para pasar
                    // fotoUrlActual = document.getString("fotoUrl") // Guardar para pasar

                    tvNombreUsuario.text = nombreActual ?: "No disponible"
                    tvEmailUsuario.text = emailActual ?: "No disponible"
                    tvTelefonoUsuario.text = telefonoActual ?: "No disponible"
                    tvDireccionUsuario.text = direccionActual ?: "No disponible"

                    // if (fotoUrlActual != null) {
                    //     Glide.with(this).load(fotoUrlActual).placeholder(R.drawable.ic_user).into(imgFotoPerfil)
                    // }
                } else {
                    Toast.makeText(this, "No se encontraron datos del perfil", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar datos del perfil: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // Es buena práctica refrescar los datos si el usuario vuelve de la pantalla de edición
    override fun onResume() {
        super.onResume()
        // Si vienes de editar, es posible que los datos hayan cambiado, así que recarga.
        // Podrías hacerlo más eficiente si EditarPerfilDuenioActivity devuelve un resultado
        // indicando si se guardaron cambios, pero para empezar esto funciona.
        if (auth.currentUser != null) { // Solo recarga si el usuario sigue logueado
            cargarDatosPerfil()
        }
    }
}