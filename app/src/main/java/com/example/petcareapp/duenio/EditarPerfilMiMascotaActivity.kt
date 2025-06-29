package com.example.petcareapp.duenio

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter // Importar ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner // Importar Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilMiMascotaActivity : AppCompatActivity() {

    // Referencias a los campos de edición
    private lateinit var etNombreMascota: TextInputEditText
    private lateinit var etEdadMascota: TextInputEditText // Para la edad, seguimos usando TextInputEditText
    private lateinit var etDescripcionMascota: TextInputEditText

    // --- Referencias a los Spinners ---
    private lateinit var spinnerEspecie: Spinner
    private lateinit var spinnerRaza: Spinner
    private lateinit var spinnerTamanio: Spinner

    // --- Listas para los adaptadores de los Spinners ---
    private val especiesList = listOf("Selecciona una especie", "Perro", "Gato", "Conejo", "Hamster", "Otro")
    private val razasList = listOf("Selecciona una raza", "Labrador", "Bulldog", "Siames", "Persa", "Mestizo", "Otra")
    private val tamaniosList = listOf("Selecciona un tamaño", "Pequeño", "Mediano", "Grande")


    private lateinit var tilNombreMascota: TextInputLayout
    // No necesitaremos TextInputLayout para especie, raza y tamaño si usamos Spinners.
    // private lateinit var tilEspecieMascota: TextInputLayout // Quitar o comentar
    // private lateinit var tilRazaMascota: TextInputLayout // Quitar o comentar
    // private lateinit var tilTamanioMascota: TextInputLayout // Quitar o comentar


    private var idMascotaActual: String? = null
    private var nombreOriginal: String? = null
    private var especieOriginal: String? = null
    private var razaOriginal: String? = null
    private var edadOriginal: String? = null // Mantenerlo para edad
    private var tamanioOriginal: String? = null
    private var descripcionOriginal: String? = null
    private var fotoUrlOriginal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil_mi_mascota)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainEditarMascota)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Inicializar vistas ---
        val btnBackEditar = findViewById<ImageButton>(R.id.btnBackEditar)
        etNombreMascota = findViewById(R.id.etNombreMascotaEditar)
        // Ya no se usan TextInputEditText para estos, sino Spinners
        // etEspecieMascota = findViewById(R.id.etEspecieMascotaEditar)
        // etRazaMascota = findViewById(R.id.etRazaMascotaEditar)
        etEdadMascota = findViewById(R.id.etEdadMascotaEditar) // Se mantiene
        // etTamanioMascota = findViewById(R.id.etTamanioMascotaEditar)
        etDescripcionMascota = findViewById(R.id.etDescripcionMascotaEditar)
        val btnGuardarCambiosMascota = findViewById<Button>(R.id.btnGuardarCambiosMascota)

        tilNombreMascota = findViewById(R.id.tilNombreMascotaEditar)
        // tilEspecieMascota = findViewById(R.id.tilEspecieMascotaEditar) // Comentar
        // tilRazaMascota = findViewById(R.id.tilRazaMascotaEditar) // Comentar
        // tilTamanioMascota = findViewById(R.id.tilTamanioMascotaEditar) // Comentar


        // --- Inicializar Spinners ---
        spinnerEspecie = findViewById(R.id.spinnerEspecieEditar)
        spinnerRaza = findViewById(R.id.spinnerRazaEditar)
        spinnerTamanio = findViewById(R.id.spinnerTamanioEditar)

        // --- Configurar adaptadores para Spinners ---
        spinnerEspecie.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, especiesList)
        spinnerRaza.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, razasList)
        spinnerTamanio.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tamaniosList)


        // --- Recuperar datos pasados ---
        idMascotaActual = intent.getStringExtra("EXTRA_ID_MASCOTA")
        nombreOriginal = intent.getStringExtra("EXTRA_NOMBRE")
        especieOriginal = intent.getStringExtra("EXTRA_ESPECIE")
        razaOriginal = intent.getStringExtra("EXTRA_RAZA")
        edadOriginal = intent.getStringExtra("EXTRA_EDAD")
        tamanioOriginal = intent.getStringExtra("EXTRA_TAMANIO")
        descripcionOriginal = intent.getStringExtra("EXTRA_DESCRIPCION")
        fotoUrlOriginal = intent.getStringExtra("EXTRA_FOTO_URL")

        if (idMascotaActual == null) {
            Toast.makeText(this, "Error: No se proporcionó el ID de la mascota.", Toast.LENGTH_LONG).show()
            Log.e("EditarPerfil", "Error: EXTRA_ID_MASCOTA es nulo.")
            finish()
            return
        }

        // --- Poblar los campos de edición (incluyendo Spinners) ---
        etNombreMascota.setText(nombreOriginal)
        etEdadMascota.setText(edadOriginal) // La edad sigue siendo un EditText
        etDescripcionMascota.setText(descripcionOriginal)

        // Establecer la selección inicial de los Spinners
        establecerSeleccionSpinner(spinnerEspecie, especiesList, especieOriginal)
        establecerSeleccionSpinner(spinnerRaza, razasList, razaOriginal)
        establecerSeleccionSpinner(spinnerTamanio, tamaniosList, tamanioOriginal)


        btnBackEditar.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnGuardarCambiosMascota.setOnClickListener {
            validarYGuardarCambiosEnFirestore()
        }
    }

    // --- Función auxiliar para establecer la selección de un Spinner ---
    private fun establecerSeleccionSpinner(spinner: Spinner, listaOpciones: List<String>, valorOriginal: String?) {
        if (valorOriginal != null) {
            val posicion = listaOpciones.indexOf(valorOriginal)
            if (posicion >= 0) {
                spinner.setSelection(posicion)
            } else {
                // Si el valor original no está en la lista (ej. "Otro" y la lista no lo tiene explícitamente como primera opción)
                // podrías seleccionar el primer item o manejarlo como prefieras.
                // O si "Otro" es una opción, asegúrate que esté en la lista.
                Log.w("EditarPerfil", "Valor original '$valorOriginal' no encontrado en la lista del Spinner. Seleccionando el primero.")
                if (listaOpciones.isNotEmpty()) {
                    spinner.setSelection(0)
                }
            }
        } else {
            // Si no hay valor original, selecciona el primero si la lista no está vacía
            if (listaOpciones.isNotEmpty()) {
                spinner.setSelection(0)
            }
        }
    }


    private fun validarYGuardarCambiosEnFirestore() {
        val nuevoNombre = etNombreMascota.text.toString().trim()
        val nuevaEdad = etEdadMascota.text.toString().trim() // Se mantiene
        val nuevaDescripcion = etDescripcionMascota.text.toString().trim()

        // --- Obtener valores de los Spinners ---
        val nuevaEspecie = spinnerEspecie.selectedItem.toString()
        val nuevaRaza = spinnerRaza.selectedItem.toString()
        val nuevoTamanio = spinnerTamanio.selectedItem.toString()

        // --- Validación para los Spinners (opcional pero recomendado) ---
        if (nuevaEspecie == especiesList[0]) { // Compara con el item "Selecciona una..."
            Toast.makeText(this, "Por favor selecciona una especie", Toast.LENGTH_SHORT).show()
            spinnerEspecie.requestFocus()
            return
        }
        if (nuevaRaza == razasList[0]) {
            Toast.makeText(this, "Por favor selecciona una raza", Toast.LENGTH_SHORT).show()
            spinnerRaza.requestFocus()
            return
        }
        if (nuevoTamanio == tamaniosList[0]) {
            Toast.makeText(this, "Por favor selecciona un tamaño", Toast.LENGTH_SHORT).show()
            spinnerTamanio.requestFocus()
            return
        }


        // Validación para los EditText
        if (nuevoNombre.isEmpty()) {
            tilNombreMascota.error = "El nombre no puede estar vacío"
            etNombreMascota.requestFocus()
            return
        } else {
            tilNombreMascota.error = null
        }

        if (nuevaEdad.isBlank()) { // "isBlank" es mejor para strings que pueden ser solo espacios
            // Asumo que tienes un TextInputLayout para etEdadMascotaEditar o usa .error directamente
            findViewById<TextInputLayout>(R.id.tilEdadMascotaEditar)?.error = "La edad no puede estar vacía"
            etEdadMascota.requestFocus()
            return
        } else {
            findViewById<TextInputLayout>(R.id.tilEdadMascotaEditar)?.error = null
        }



        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_LONG).show()
            Log.e("EditarPerfil", "Error: UID de Firebase es nulo.")
            return
        }

        if (idMascotaActual != null) {
            val mascotaActualizada = hashMapOf(
                "nombre" to nuevoNombre,
                "especie" to nuevaEspecie, // Valor del Spinner
                "raza" to nuevaRaza,       // Valor del Spinner
                "edad" to nuevaEdad,       // Valor del EditText
                "tamanio" to nuevoTamanio, // Valor del Spinner
                "descripcion" to nuevaDescripcion
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaActual!!)
                .update(mascotaActualizada as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil de mascota actualizado", Toast.LENGTH_SHORT).show()
                    Log.d("EditarPerfil", "Mascota actualizada en Firestore con ID: $idMascotaActual")
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar en BD: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("EditarPerfil", "Error al actualizar Firestore: ", e)
                }
        } else {
            Toast.makeText(this, "Error crítico: No se encontró ID de mascota para actualizar.", Toast.LENGTH_LONG).show()
            Log.e("EditarPerfil", "Error crítico: idMascotaActual es nulo en el momento de guardar.")
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}