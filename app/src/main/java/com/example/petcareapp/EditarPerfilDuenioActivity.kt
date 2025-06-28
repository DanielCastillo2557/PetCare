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
    private lateinit var storage: FirebaseStorage // Para Firebase Storage
    private var currentUserUid: String? = null

    private lateinit var etNombreEditar: TextInputEditText
    private lateinit var etTelefonoEditar: TextInputEditText
    private lateinit var etDireccionEditar: TextInputEditText
    private lateinit var btnGuardarCambiosPerfil: Button
    private lateinit var btnVolverDesdeEditarPerfil: ImageView
    private lateinit var imgEditarFotoPerfil: CircleImageView // O ImageView
    private lateinit var tvCambiarFoto: TextView


    private var imagenUri: Uri? = null // Para almacenar la URI de la imagen seleccionada
    private var fotoUrlActual: String? = null // Para almacenar la URL de la foto actual

    // ActivityResultLauncher para seleccionar imagen de la galería
    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    imagenUri = uri // Guardar la URI de la nueva imagen
                    // Mostrar la imagen seleccionada en el ImageView
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
        // enableEdgeToEdge() // Comentado temporalmente si causa problemas con el selector de imágenes
        setContentView(R.layout.activity_editar_perfil_duenio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance() // Inicializar Firebase Storage
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

        // Recuperar y mostrar datos actuales
        val nombreActual = intent.getStringExtra("NOMBRE_ACTUAL")
        fotoUrlActual = intent.getStringExtra("FOTO_URL_ACTUAL") // Recuperar URL de la foto
        val telefonoActual = intent.getStringExtra("TELEFONO_ACTUAL")
        val direccionActual = intent.getStringExtra("DIRECCION_ACTUAL")

        etNombreEditar.setText(nombreActual)
        etTelefonoEditar.setText(telefonoActual)
        etDireccionEditar.setText(direccionActual)

        // Cargar la foto de perfil actual si existe
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

        // Listener para cambiar la foto de perfil
        imgEditarFotoPerfil.setOnClickListener {
            abrirGaleria()
        }
        tvCambiarFoto.setOnClickListener { // También permitir clic en el texto
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

        // Deshabilitar botón para evitar múltiples clics
        btnGuardarCambiosPerfil.isEnabled = false
        Toast.makeText(this, "Guardando cambios...", Toast.LENGTH_SHORT).show()


        // Si se seleccionó una nueva imagen, subirla primero
        if (imagenUri != null) {
            subirNuevaFotoYActualizarPerfil(nuevoNombre, nuevoTelefono, nuevaDireccion)
        } else {
            // Si no hay nueva imagen, solo actualizar los otros datos
            actualizarDatosPerfil(nuevoNombre, nuevoTelefono, nuevaDireccion, fotoUrlActual)
        }
    }

    private fun subirNuevaFotoYActualizarPerfil(nombre: String, telefono: String, direccion: String) {
        val userId = currentUserUid ?: return // No debería ser nulo aquí
        // Crear una referencia única para la imagen en Storage
        val nombreArchivo = "perfil_${userId}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("fotos_perfil/$nombreArchivo")

        imagenUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Obtener la URL de descarga de la imagen subida
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val nuevaFotoUrl = downloadUri.toString()
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
                    // Podrías mostrar un ProgressBar aquí con el progreso
                    Log.d("EditarPerfil", "Subida en progreso: $progress%")
                }
        }
    }


    private fun actualizarDatosPerfil(
        nombre: String,
        telefono: String,
        direccion: String,
        urlFoto: String? // Puede ser la URL nueva o la anterior si no se cambió la foto
    ) {
        val updates = hashMapOf<String, Any>(
            "nombre" to nombre,
            "telefono" to telefono,
            "direccion" to direccion
        )
        // Solo añadir foto_url si es válida (nueva o la anterior si no se cambió y era válida)
        if (!urlFoto.isNullOrEmpty()) {
            updates["foto_url"] = urlFoto
        } else {
            // Si la URL es nula o vacía y se quiere "eliminar" la foto, se puede enviar un valor especial
            // o simplemente no incluir el campo, o Firestore.FieldValue.delete() si quieres eliminar el campo.
            // Por ahora, si es nula, no la actualizamos explícitamente a nulo a menos que sea la intención.
            // Si la URL anterior era válida y no se cambió, se usa esa.
            // Si no había URL antes y no se subió una nueva, no se añade nada.
        }


        currentUserUid?.let { uid ->
            db.collection("usuarios").document(uid)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    // Podrías devolver la nueva URL de la foto a la actividad anterior si es necesario
                    // setResult(Activity.RESULT_OK, Intent().putExtra("NUEVA_FOTO_URL", urlFoto))
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("EditarPerfil", "Error al actualizar perfil en Firestore: ${e.message}", e)
                    Toast.makeText(this, "Error al actualizar el perfil: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    btnGuardarCambiosPerfil.isEnabled = true
                }
        } ?: run {
            btnGuardarCambiosPerfil.isEnabled = true // Reactivar botón si currentUserUid es nulo (poco probable aquí)
        }
    }
}