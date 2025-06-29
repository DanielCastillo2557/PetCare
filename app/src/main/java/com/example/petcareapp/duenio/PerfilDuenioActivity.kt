package com.example.petcareapp.duenio

import android.content.Intent
import android.graphics.drawable.Drawable // Asegúrate de que esta importación esté presente
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource // Importa DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.petcareapp.R
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

    private var nombreActual: String? = null
    private var emailActual: String? = null
    private var telefonoActual: String? = null
    private var direccionActual: String? = null
    private var fotoUrlActual: String? = null

    private companion object {
        private const val TAG = "PerfilDuenioActivity"
    }

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
            val intent = Intent(this, EditarPerfilDuenioActivity::class.java)
            intent.putExtra("NOMBRE_ACTUAL", nombreActual)
            intent.putExtra("EMAIL_ACTUAL", emailActual)
            intent.putExtra("TELEFONO_ACTUAL", telefonoActual)
            intent.putExtra("DIRECCION_ACTUAL", direccionActual)
            intent.putExtra("FOTO_URL_ACTUAL", fotoUrlActual)
            startActivity(intent)
        }

        btnVolverDesdePerfil.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosPerfil() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e(TAG, "Error: Usuario no autenticado (UID nulo)")
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        Log.d(TAG, "Cargando datos para UID: $uid")

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "Documento de usuario encontrado.")
                    nombreActual = document.getString("nombre")
                    emailActual = document.getString("email")
                    telefonoActual = document.getString("telefono")
                    direccionActual = document.getString("direccion")
                    fotoUrlActual = document.getString("foto_url")
                    Log.d(TAG, "Valor de 'foto_url' obtenido de Firestore: '$fotoUrlActual'")

                    tvNombreUsuario.text = nombreActual ?: "No disponible"
                    tvEmailUsuario.text = emailActual ?: "No disponible"
                    tvTelefonoUsuario.text = telefonoActual ?: "No disponible"
                    tvDireccionUsuario.text = direccionActual ?: "No disponible"

                    if (fotoUrlActual != null && fotoUrlActual!!.isNotEmpty()) {
                        Log.d(TAG, "Intentando cargar imagen con Glide desde: $fotoUrlActual")
                        Glide.with(this)
                            .load(fotoUrlActual)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .listener(object : RequestListener<Drawable> {
                                // onLoadFailed (manteniendo la versión anterior que parecía más cercana)
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>, // Target no nulo
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e(TAG, "Glide onLoadFailed: ${e?.message}", e)
                                    return false
                                }

                                // onResourceReady (con parámetros no nulos según el nuevo error)
                                override fun onResourceReady(
                                    resource: Drawable,    // CAMBIO: No nulo
                                    model: Any,          // CAMBIO: No nulo
                                    target: Target<Drawable>, // Target no nulo
                                    dataSource: DataSource,  // CAMBIO: No nulo
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.d(TAG, "Glide onResourceReady: Imagen cargada exitosamente.")
                                    // Aquí puedes usar 'resource', 'model', 'target', 'dataSource'
                                    // sabiendo que no son nulos si la carga fue exitosa.
                                    return false
                                }
                            })
                            .circleCrop()
                            .into(imgFotoPerfil)
                    } else {
                        Log.d(TAG, "'fotoUrlActual' es nula o vacía. Mostrando placeholder.")
                        imgFotoPerfil.setImageResource(R.drawable.ic_user)
                    }
                } else {
                    Log.w(TAG, "No se encontraron datos del perfil para el usuario con UID: $uid")
                    Toast.makeText(this, "No se encontraron datos del perfil", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al cargar datos del perfil desde Firestore para UID: $uid", exception)
                Toast.makeText(this, "Error al cargar datos del perfil: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            Log.d(TAG, "onResume: Recargando datos del perfil.")
            cargarDatosPerfil()
        } else {
            Log.w(TAG, "onResume: Usuario no autenticado, no se recargan datos.")
        }
    }
}