package com.example.petcareapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView // O ImageView si no usas CircleImageView
import java.util.UUID

class EditarPerfilDuenioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var currentUserUid: String? = null

    private lateinit var etNombreEditar: TextInputEditText
    private lateinit var etTelefonoEditar: TextInputEditText
    private lateinit var etDireccionEditar: TextInputEditText
    private lateinit var btnGuardarCambiosPerfil: Button
    private lateinit var btnVolverDesdeEditarPerfil: ImageView
    private lateinit var imgEditarFotoPerfil: CircleImageView
    private lateinit var tvCambiarFoto: TextView

    private var imagenUri: Uri? = null
    private var fotoUrlActual: String? = null // Esta se actualiza si la foto cambia

    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    imagenUri = uri
                    Glide.with(this)
                        .load(imagenUri)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(imgEditarFotoPerfil)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_duenio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentUserUid = auth.currentUser?.uid

        if (currentUserUid == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        etNombreEditar = findViewById(R.id.etNombreEditar)
        etTelefonoEditar = findViewById(R.id.etTelefonoEditar)
        etDireccionEditar = findViewById(R.id.etDireccionEditar)
        btnGuardarCambiosPerfil = findViewById(R.id.btnGuardarCambiosPerfil)
        btnVolverDesdeEditarPerfil = findViewById(R.id.btnVolverDesdeEditarPerfil)
        imgEditarFotoPerfil = findViewById(R.id.imgEditarFotoPerfil)
        tvCambiarFoto = findViewById(R.id.tvCambiarFoto)

        val nombreActualIntent = intent.getStringExtra("NOMBRE_ACTUAL")
        fotoUrlActual = intent.getStringExtra("FOTO_URL_ACTUAL") // Inicializar con la URL del intent
        val telefonoActualIntent = intent.getStringExtra("TELEFONO_ACTUAL")
        val direccionActualIntent = intent.getStringExtra("DIRECCION_ACTUAL")

        etNombreEditar.setText(nombreActualIntent)
        etTelefonoEditar.setText(telefonoActualIntent)
        etDireccionEditar.setText(direccionActualIntent)

        if (!fotoUrlActual.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoUrlActual)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(imgEditarFotoPerfil)
        }

        btnVolverDesdeEditarPerfil.setOnClickListener {
            finish()
        }

        imgEditarFotoPerfil.setOnClickListener {
            abrirGaleria()
        }
        tvCambiarFoto.setOnClickListener {
            abrirGaleria()
        }

        btnGuardarCambiosPerfil.setOnClickListener {
            guardarCambios()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        seleccionarImagenLauncher.launch(intent)
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

        btnGuardarCambiosPerfil.isEnabled = false
        Toast.makeText(this, "Guardando cambios...", Toast.LENGTH_SHORT).show()

        if (imagenUri != null) { // Si el usuario seleccionó una nueva imagen
            subirNuevaFotoYActualizarPerfil(nuevoNombre, nuevoTelefono, nuevaDireccion)
        } else { // Si no hay nueva imagen, solo actualizar los otros datos
            actualizarDatosPerfil(nuevoNombre, nuevoTelefono, nuevaDireccion, fotoUrlActual)
        }
    }

    private fun subirNuevaFotoYActualizarPerfil(nombre: String, telefono: String, direccion: String) {
        val userId = currentUserUid ?: run {
            Toast.makeText(this, "Error: UID de usuario no disponible.", Toast.LENGTH_LONG).show()
            btnGuardarCambiosPerfil.isEnabled = true
            return
        }
        val nombreArchivo = "fotos_perfil/perfil_${userId}_${UUID.randomUUID()}.jpg" // Ruta completa
        val nuevaStorageRef = storage.reference.child(nombreArchivo)

        // Guardar la URL de la foto anterior para poder eliminarla después
        val urlFotoAnteriorParaEliminar = fotoUrlActual

        imagenUri?.let { uri ->
            nuevaStorageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    nuevaStorageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val nuevaFotoUrl = downloadUri.toString()

                        // Antes de actualizar Firestore, intenta eliminar la foto anterior de Storage
                        if (!urlFotoAnteriorParaEliminar.isNullOrEmpty() &&
                            urlFotoAnteriorParaEliminar.startsWith("https://firebasestorage.googleapis.com")) {
                            try {
                                val fotoAnteriorStorageRef = storage.getReferenceFromUrl(urlFotoAnteriorParaEliminar)
                                fotoAnteriorStorageRef.delete().addOnSuccessListener {
                                    Log.d("EditarPerfil", "Foto anterior eliminada de Storage exitosamente.")
                                }.addOnFailureListener { e ->
                                    Log.e("EditarPerfil", "Error al eliminar foto anterior de Storage: ${e.message}", e)
                                    // No es crítico si falla, la nueva foto ya está subida.
                                }
                            } catch (e: Exception) {
                                Log.e("EditarPerfil", "Excepción al intentar obtener referencia de foto anterior: ${e.message}", e)
                                // Podría ser una URL inválida o malformada.
                            }
                        }

                        // Actualizar la variable de instancia fotoUrlActual con la nueva URL
                        this.fotoUrlActual = nuevaFotoUrl
                        actualizarDatosPerfil(nombre, telefono, direccion, nuevaFotoUrl)

                    }.addOnFailureListener { e ->
                        Log.e("EditarPerfil", "Error al obtener URL de descarga: ${e.message}", e)
                        Toast.makeText(this, "Error al obtener URL de foto: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        btnGuardarCambiosPerfil.isEnabled = true
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditarPerfil", "Error al subir foto: ${e.message}", e)
                    Toast.makeText(this, "Error al subir foto: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    btnGuardarCambiosPerfil.isEnabled = true
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    Log.d("EditarPerfil", "Subida en progreso: $progress%")
                }
        } ?: run {
            // Esto no debería ocurrir si imagenUri != null, pero es una salvaguarda
            Toast.makeText(this, "Error: No se seleccionó ninguna imagen nueva.", Toast.LENGTH_SHORT).show()
            btnGuardarCambiosPerfil.isEnabled = true
        }
    }

    private fun actualizarDatosPerfil(
        nombre: String,
        telefono: String,
        direccion: String,
        urlFoto: String?
    ) {
        val updates = hashMapOf<String, Any>(
            "nombre" to nombre,
            "telefono" to telefono,
            "direccion" to direccion
        )
        if (!urlFoto.isNullOrEmpty()) {
            updates["foto_url"] = urlFoto
        }
        // Si urlFoto es nulo o vacío y quieres explícitamente eliminar el campo de Firestore:
        // else {
        // updates["foto_url"] = FieldValue.delete() // Esto eliminaría el campo foto_url si no hay foto
        // }

        currentUserUid?.let { uid ->
            db.collection("usuarios").document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    // Prepara el intent de resultado si quieres devolver la nueva URL a la actividad anterior
                    val resultIntent = Intent()
                    if (!urlFoto.isNullOrEmpty()) {
                        resultIntent.putExtra("NUEVA_FOTO_URL", urlFoto)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("EditarPerfil", "Error al actualizar perfil en Firestore: ${e.message}", e)
                    Toast.makeText(this, "Error al actualizar el perfil: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    btnGuardarCambiosPerfil.isEnabled = true
                }
        } ?: run {
            Log.e("EditarPerfil", "Error: UID de usuario no disponible para actualizar Firestore.")
            Toast.makeText(this, "Error al actualizar el perfil: Usuario no identificado.", Toast.LENGTH_LONG).show()
            btnGuardarCambiosPerfil.isEnabled = true
        }
    }
}