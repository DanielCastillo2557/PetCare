package com.example.petcareapp.cuidador

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.bumptech.glide.Glide
import com.example.petcareapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class EditarPerfilCuidadorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imgEditarFotoPerfil: ImageView
    private lateinit var etNombreCompleto: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etDireccion: TextInputEditText
    private lateinit var btnGuardarCambios: Button
    private lateinit var progressBar: ProgressBar
    private var currentNombre: String? = null
    private var currentTelefono: String? = null
    private var currentDireccion: String? = null
    private var currentFotoUrl: String? = null // URL de la foto actual para posible borrado
    private var nuevaFotoUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    companion object {
        const val TAG = "EditarPerfilCuidador"
        const val EXTRA_NOMBRE = "com.example.petcareapp.EXTRA_NOMBRE"
        const val EXTRA_TELEFONO = "com.example.petcareapp.EXTRA_TELEFONO"
        const val EXTRA_DIRECCION = "com.example.petcareapp.EXTRA_DIRECCION"
        const val EXTRA_FOTO_URL = "com.example.petcareapp.EXTRA_FOTO_URL"
        const val RESULT_NUEVA_FOTO_URL = "com.example.petcareapp.RESULT_NUEVA_FOTO_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainEditarPerfil)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imgEditarFotoPerfil = findViewById(R.id.imgEditarFotoPerfil)
        etNombreCompleto = findViewById(R.id.etNombreEditar)
        etTelefono = findViewById(R.id.etTelefonoEditar)
        etDireccion = findViewById(R.id.etDireccionEditar)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        progressBar = findViewById(R.id.progressBarEditar)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.data?.let { uri ->
                    nuevaFotoUri = uri
                    Glide.with(this).load(uri).circleCrop().placeholder(R.drawable.ic_user).error(R.drawable.ic_user).into(imgEditarFotoPerfil)
                }
            }
        }

        imgEditarFotoPerfil.setOnClickListener {
            abrirSelectorDeImagen()
        }

        cargarDatosIniciales()

        btnGuardarCambios.setOnClickListener {
            if (validarCampos()) {
                guardarCambiosPerfil() // Cambiado nombre para reflejar el de Dueño (opcional)
            }
        }
    }

    private fun abrirSelectorDeImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun cargarDatosIniciales() {
        showLoading(true)
        currentNombre = intent.getStringExtra(EXTRA_NOMBRE)
        currentTelefono = intent.getStringExtra(EXTRA_TELEFONO)
        currentDireccion = intent.getStringExtra(EXTRA_DIRECCION)
        currentFotoUrl = intent.getStringExtra(EXTRA_FOTO_URL)

        if (auth.currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado. Por favor, inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
            showLoading(false)
            // Aquí podrías considerar redirigir al login
            finish()
            return
        }

        // Si los datos del intent son null, intenta cargar desde Firestore
        // Esto es útil si la actividad se recrea o se accede sin pasar todos los extras.
        if (currentNombre == null && currentTelefono == null && currentDireccion == null /* currentFotoUrl puede ser legítimamente null */) {
            Log.d(TAG, "Datos no recibidos por Intent o incompletos, cargando desde Firestore.")
            cargarDatosDesdeFirestore()
        } else {
            Log.d(TAG, "Datos recibidos por Intent.")
            actualizarUIConDatosCargados()
            showLoading(false)
        }
    }

    private fun cargarDatosDesdeFirestore() {
        val user = auth.currentUser
        if (user == null) { // Doble chequeo, aunque ya se hizo en cargarDatosIniciales
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            finish()
            return
        }
        db.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentNombre = document.getString("nombre")
                    currentTelefono = document.getString("telefono")
                    currentDireccion = document.getString("direccion")
                    currentFotoUrl = document.getString("foto_url") // Importante actualizarlo
                    actualizarUIConDatosCargados()
                } else {
                    Toast.makeText(this, "No se pudieron cargar los datos del perfil.", Toast.LENGTH_SHORT).show()
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error al cargar datos desde Firestore", e)
                showLoading(false)
            }
    }

    private fun actualizarUIConDatosCargados() {
        etNombreCompleto.setText(currentNombre)
        etTelefono.setText(currentTelefono)
        etDireccion.setText(currentDireccion)

        if (!currentFotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(currentFotoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(imgEditarFotoPerfil)
        } else {
            imgEditarFotoPerfil.setImageResource(R.drawable.ic_user)
        }
    }


    private fun validarCampos(): Boolean {
        if (etNombreCompleto.text.toString().trim().isEmpty()) {
            etNombreCompleto.error = "El nombre no puede estar vacío"
            etNombreCompleto.requestFocus()
            return false
        }
        // Puedes añadir más validaciones aquí (teléfono, dirección si son obligatorios)
        return true
    }

    private fun guardarCambiosPerfil() {
        showLoading(true) // O btnGuardarCambios.isEnabled = false; progressBar.visibility = View.VISIBLE
        val nuevoNombre = etNombreCompleto.text.toString().trim()
        val nuevoTelefono = etTelefono.text.toString().trim()
        val nuevaDireccion = etDireccion.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Error: UID de usuario no disponible.", Toast.LENGTH_LONG).show()
            showLoading(false)
            return
        }

        if (nuevaFotoUri != null) { // Si el usuario seleccionó una nueva imagen
            subirNuevaFotoYActualizarPerfil(userId, nuevoNombre, nuevoTelefono, nuevaDireccion)
        } else { // Si no hay nueva imagen, solo actualizar los otros datos
            // Usamos currentFotoUrl porque no se seleccionó una nueva
            actualizarDatosEnFirestore(userId, nuevoNombre, nuevoTelefono, nuevaDireccion, currentFotoUrl, false)
        }
    }

    private fun subirNuevaFotoYActualizarPerfil(userId: String, nombre: String, telefono: String, direccion: String) {
        val nombreArchivo = "fotos_perfil/perfil_${userId}_${UUID.randomUUID()}.jpg"
        val nuevaStorageRef = storage.reference.child(nombreArchivo)

        val urlFotoAnteriorParaEliminar = currentFotoUrl // Guardamos la URL actual antes de sobreescribirla

        nuevaFotoUri?.let { uri ->
            nuevaStorageRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    Log.d(TAG, "Subida en progreso: $progress%")
                    // Aquí podrías actualizar una ProgressBar de subida si la tuvieras
                }
                .addOnSuccessListener { taskSnapshot ->
                    nuevaStorageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val nuevaFotoUrl = downloadUri.toString()

                        // Intentar eliminar la foto anterior de Storage ANTES de actualizar Firestore con la nueva
                        if (!urlFotoAnteriorParaEliminar.isNullOrEmpty() &&
                            urlFotoAnteriorParaEliminar.startsWith("https://firebasestorage.googleapis.com")) {
                            try {
                                val fotoAnteriorStorageRef = storage.getReferenceFromUrl(urlFotoAnteriorParaEliminar)
                                fotoAnteriorStorageRef.delete().addOnSuccessListener {
                                    Log.d(TAG, "Foto anterior eliminada de Storage exitosamente.")
                                }.addOnFailureListener { e ->
                                    Log.w(TAG, "Error al eliminar foto anterior de Storage: ${e.message}", e)
                                    // No es crítico, la nueva foto ya está subida.
                                }
                            } catch (e: Exception) { // Captura IllegalArgumentException y otras posibles
                                Log.e(TAG, "Excepción al intentar obtener referencia de foto anterior: ${e.message}", e)
                            }
                        }
                        // Actualizar la variable de instancia currentFotoUrl con la nueva URL
                        // para que el siguiente guardado (si se hace sin cambiar foto) use la correcta.
                        this.currentFotoUrl = nuevaFotoUrl
                        actualizarDatosEnFirestore(userId, nombre, telefono, direccion, nuevaFotoUrl, true)

                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error al obtener URL de descarga: ${e.message}", e)
                        Toast.makeText(this, "Error al obtener URL de foto: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        showLoading(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al subir foto: ${e.message}", e)
                    Toast.makeText(this, "Error al subir foto: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
        } ?: run {
            // Esto no debería ocurrir si nuevaFotoUri != null, pero es una salvaguarda
            Toast.makeText(this, "Error: No se seleccionó ninguna imagen nueva.", Toast.LENGTH_SHORT).show()
            showLoading(false)
        }
    }

    private fun actualizarDatosEnFirestore(
        userId: String,
        nombre: String,
        telefono: String,
        direccion: String,
        fotoUrlParaGuardar: String?, // URL de la foto que se va a guardar (nueva o la actual si no cambió)
        fotoCambio: Boolean // Indicador si la foto cambió o no
    ) {
        val updates = hashMapOf<String, Any?>()

        // Solo añadir al mapa si el valor realmente cambió o si es la foto y esta cambió
        if (nombre != currentNombre) updates["nombre"] = nombre
        if (telefono != currentTelefono) updates["telefono"] = telefono
        if (direccion != currentDireccion) updates["direccion"] = direccion

        // Si la foto cambió, o si la fotoUrlParaGuardar es diferente de la que estaba (currentFotoUrl antes de la posible nueva subida)
        // O más simple: si 'fotoCambio' es true, actualizamos. Si es false, solo actualizamos si fotoUrlParaGuardar es diferente de currentFotoUrl (lo cual no debería ser si fotoCambio es false)
        if (fotoCambio) { // fotoCambio es true si se subió una nueva foto
            updates["foto_url"] = fotoUrlParaGuardar // Puede ser la nueva URL
        } else {
            // Si la foto no cambió pero queremos asegurar que foto_url esté en updates
            // si era diferente de la original (aunque esto no debería pasar si no hubo cambio)
            // Esta lógica es más simple: si la foto no cambió, no la añadimos a updates
            // a menos que la URL que tenemos (fotoUrlParaGuardar que sería == currentFotoUrl)
            // fuera diferente del estado inicial (lo cual ya manejan los if de nombre, telefono, etc.).
            // La forma más segura: si fotoCambio es true, o si fotoUrlParaGuardar es diferente al currentFotoUrl *original*
            // Por ahora, si fotoCambio es true, se añade. Si es false, no se añade.
            // Esto significa que si solo cambian los textos, foto_url no se toca en Firestore.
            // Si la fotoUrlParaGuardar que llega es diferente de this.currentFotoUrl (el estado *antes* de esta llamada),
            // entonces sí hay que actualizarla.
            // La variable fotoCambio simplifica esto:
            if (fotoCambio) updates["foto_url"] = fotoUrlParaGuardar
        }


        if (updates.isEmpty()) {
            Toast.makeText(this, "No hay cambios para guardar.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        db.collection("usuarios").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Perfil actualizado correctamente en Firestore.")
                Toast.makeText(this, "Perfil actualizado.", Toast.LENGTH_SHORT).show()

                // Actualizar DisplayName y PhotoUrl en Firebase Auth
                val profileUpdatesAuthBuilder = UserProfileChangeRequest.Builder()
                var authProfileChanged = false

                if (updates.containsKey("nombre")) {
                    val nombreAuth = updates["nombre"] as String?
                    profileUpdatesAuthBuilder.displayName = nombreAuth
                    // Actualizar variable local también
                    this.currentNombre = nombreAuth
                    authProfileChanged = true
                }
                // Si la foto cambió, actualizamos en Auth
                if (updates.containsKey("foto_url")) {
                    val finalFotoUrlAuth = updates["foto_url"] as String?
                    profileUpdatesAuthBuilder.photoUri = if (finalFotoUrlAuth != null) Uri.parse(finalFotoUrlAuth) else null
                    // this.currentFotoUrl ya se actualizó en subirNuevaFotoYActualizarPerfil si la foto cambió
                    authProfileChanged = true
                }

                if (authProfileChanged) {
                    auth.currentUser?.updateProfile(profileUpdatesAuthBuilder.build())
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Perfil en Firebase Auth actualizado.")
                            } else {
                                Log.w(TAG, "Error al actualizar perfil en Firebase Auth: ${task.exception?.message}")
                            }
                        }
                }
                // Actualizar variables locales para telefono y dirección para consistencia
                if (updates.containsKey("telefono")) this.currentTelefono = updates["telefono"] as String?
                if (updates.containsKey("direccion")) this.currentDireccion = updates["direccion"] as String?


                val resultIntent = Intent()
                if (updates.containsKey("foto_url")) { // Si la foto se actualizó, enviar la nueva URL
                    resultIntent.putExtra(RESULT_NUEVA_FOTO_URL, updates["foto_url"] as String?)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                showLoading(false)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar perfil en Firestore", e)
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                showLoading(false)
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGuardarCambios.isEnabled = !isLoading
        imgEditarFotoPerfil.isEnabled = !isLoading // Permitir cambiar foto incluso si está cargando? Mejor no.
        etNombreCompleto.isEnabled = !isLoading
        etTelefono.isEnabled = !isLoading
        etDireccion.isEnabled = !isLoading
    }
}