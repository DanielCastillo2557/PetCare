package com.example.petcareapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log // Para depuración
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth // Importar FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Importar FirebaseFirestore

class EditarPerfilMiMascotaActivity : AppCompatActivity() {

    // Referencias a los campos de edición
    private lateinit var etNombreMascota: TextInputEditText
    private lateinit var etEspecieMascota: TextInputEditText
    private lateinit var etRazaMascota: TextInputEditText
    private lateinit var etEdadMascota: TextInputEditText
    private lateinit var etTamanioMascota: TextInputEditText
    private lateinit var etDescripcionMascota: TextInputEditText

    private lateinit var tilNombreMascota: TextInputLayout

    // Variable para almacenar el ID de la mascota actual
    private var idMascotaActual: String? = null // <--- NECESARIO PARA FIRESTORE
    private var nombreOriginal: String? = null
    private var especieOriginal: String? = null
    private var razaOriginal: String? = null
    private var edadOriginal: String? = null
    private var tamanioOriginal: String? = null
    private var descripcionOriginal: String? = null
    private var fotoUrlOriginal: String? = null // Si también la pasas y editas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_mi_mascota)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainEditarMascota)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBackEditar = findViewById<ImageButton>(R.id.btnBackEditar)
        etNombreMascota = findViewById(R.id.etNombreMascotaEditar)
        etEspecieMascota = findViewById(R.id.etEspecieMascotaEditar)
        etRazaMascota = findViewById(R.id.etRazaMascotaEditar)
        etEdadMascota = findViewById(R.id.etEdadMascotaEditar)
        etTamanioMascota = findViewById(R.id.etTamanioMascotaEditar)
        etDescripcionMascota = findViewById(R.id.etDescripcionMascotaEditar)
        val btnGuardarCambiosMascota = findViewById<Button>(R.id.btnGuardarCambiosMascota)
        tilNombreMascota = findViewById(R.id.tilNombreMascotaEditar)

        // --- Recuperar datos pasados desde PerfilMiMascotaActivity ---
        idMascotaActual = intent.getStringExtra("EXTRA_ID_MASCOTA") // <--- RECUPERAR EL ID
        nombreOriginal = intent.getStringExtra("EXTRA_NOMBRE")
        especieOriginal = intent.getStringExtra("EXTRA_ESPECIE")
        razaOriginal = intent.getStringExtra("EXTRA_RAZA")
        edadOriginal = intent.getStringExtra("EXTRA_EDAD")
        tamanioOriginal = intent.getStringExtra("EXTRA_TAMANIO")
        descripcionOriginal = intent.getStringExtra("EXTRA_DESCRIPCION")
        fotoUrlOriginal = intent.getStringExtra("EXTRA_FOTO_URL") // Si lo pasas

        // --- Verificar si el ID se recibió ---
        if (idMascotaActual == null) {
            Toast.makeText(this, "Error: No se proporcionó el ID de la mascota.", Toast.LENGTH_LONG).show()
            Log.e("EditarPerfil", "Error: EXTRA_ID_MASCOTA es nulo.")
            finish() // No se puede continuar sin ID
            return
        }

        // Poblar los campos de edición
        etNombreMascota.setText(nombreOriginal)
        etEspecieMascota.setText(especieOriginal)
        etRazaMascota.setText(razaOriginal)
        etEdadMascota.setText(edadOriginal)
        etTamanioMascota.setText(tamanioOriginal)
        etDescripcionMascota.setText(descripcionOriginal)

        btnBackEditar.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnGuardarCambiosMascota.setOnClickListener {
            // Se llama a la nueva función que incluye la lógica de Firestore
            validarYGuardarCambiosEnFirestore()
        }
    }

    private fun validarYGuardarCambiosEnFirestore() {
        // 1. Obtener los nuevos valores de los campos
        val nuevoNombre = etNombreMascota.text.toString().trim()
        val nuevaEspecie = etEspecieMascota.text.toString().trim()
        val nuevaRaza = etRazaMascota.text.toString().trim()
        val nuevaEdad = etEdadMascota.text.toString().trim()
        val nuevoTamanio = etTamanioMascota.text.toString().trim()
        val nuevaDescripcion = etDescripcionMascota.text.toString().trim()
        // val nuevaFotoUrl = fotoUrlOriginal // Por ahora, mantenemos la original, o implementa lógica de cambio de foto

        // 2. Validar los datos
        if (nuevoNombre.isEmpty()) {
            tilNombreMascota.error = "El nombre no puede estar vacío"
            etNombreMascota.requestFocus()
            return
        } else {
            tilNombreMascota.error = null
        }
        if (nuevaEspecie.isEmpty()) {
            // Asumo que tienes TextInputLayouts para estos también o usa etEspecieMascota.error
            etEspecieMascota.error = "La especie no puede estar vacía"
            etEspecieMascota.requestFocus()
            return
        } else { etEspecieMascota.error = null }
        if (nuevaRaza.isEmpty()) {
            etRazaMascota.error = "La raza no puede estar vacía"
            etRazaMascota.requestFocus()
            return
        } else { etRazaMascota.error = null }
        if (nuevaEdad.isEmpty()) {
            etEdadMascota.error = "La edad no puede estar vacía"
            etEdadMascota.requestFocus()
            return
        } else {
            try {
                nuevaEdad.toInt() // o .toFloat() si permites decimales
            } catch (e: NumberFormatException) {
                etEdadMascota.error = "La edad debe ser un número"
                etEdadMascota.requestFocus()
                return
            }
            etEdadMascota.error = null
        }
        if (nuevoTamanio.isEmpty()) {
            etTamanioMascota.error = "El tamaño no puede estar vacío"
            etTamanioMascota.requestFocus()
            return
        } else { etTamanioMascota.error = null }


        // 3. Lógica para guardar los datos en Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_LONG).show()
            Log.e("EditarPerfil", "Error: UID de Firebase es nulo.")
            return
        }

        // idMascotaActual ya fue verificado en onCreate, pero una comprobación extra no hace daño
        if (idMascotaActual != null) {
            val mascotaActualizada = hashMapOf(
                "nombre" to nuevoNombre,
                "especie" to nuevaEspecie,
                "raza" to nuevaRaza,
                "edad" to nuevaEdad,
                "tamanio" to nuevoTamanio,
                "descripcion" to nuevaDescripcion
                // "fotoUrl" to nuevaFotoUrl // Solo incluye si la actualizas
                // No incluyas "id" aquí, ya que es el identificador del documento
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaActual!!)
                .update(mascotaActualizada as Map<String, Any>) // Hacemos cast a Map<String, Any>
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil de mascota actualizado", Toast.LENGTH_SHORT).show()
                    Log.d("EditarPerfil", "Mascota actualizada en Firestore con ID: $idMascotaActual")
                    // Devolver resultado a PerfilMiMascotaActivity
                    val resultIntent = Intent()
                    // Puedes opcionalmente pasar algunos datos de vuelta si PerfilMiMascotaActivity
                    // no recarga desde Firestore, pero la mejor práctica es que sí lo haga.
                    // resultIntent.putExtra("NOMBRE_ACTUALIZADO_MASCOTA", nuevoNombre)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar en BD: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("EditarPerfil", "Error al actualizar Firestore: ", e)
                }
        } else {
            // Este caso no debería ocurrir si la verificación en onCreate está bien
            Toast.makeText(this, "Error crítico: No se encontró ID de mascota para actualizar.", Toast.LENGTH_LONG).show()
            Log.e("EditarPerfil", "Error crítico: idMascotaActual es nulo en el momento de guardar.")
        }
    }



    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED) // Llama a setResult antes de super.onBackPressed() o finish()
        super.onBackPressed()
        // o finish(); si no quieres el comportamiento por defecto de back.
    }
}