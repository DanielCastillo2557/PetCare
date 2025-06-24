package com.example.petcareapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// import com.bumptech.glide.Glide // Descomenta si usas Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Para recargar desde Firestore
import de.hdodenhof.circleimageview.CircleImageView

class PerfilMiMascotaActivity : AppCompatActivity() {

    private lateinit var tvTituloMascota: TextView
    private lateinit var imagenMascota: CircleImageView // Asegúrate que el ID en XML sea 'imagenMascota'
    private lateinit var txtNombreMascota: TextView
    private lateinit var txtRazaMascota: TextView
    private lateinit var txtEdadMascota: TextView
    private lateinit var txtEspecieMascota: TextView
    private lateinit var txtTamanioMascota: TextView
    private lateinit var txtDescripcionMascota: TextView

    // --- Variables para almacenar los datos actuales ---
    private var mascotaIdActual: String? = null // <--- MUY IMPORTANTE
    private var nombreActual: String? = null
    private var razaActual: String? = null
    private var edadActual: String? = null
    private var especieActual: String? = null
    private var tamanioActual: String? = null
    private var descripcionActual: String? = null
    private var fotoUrlActual: String? = null // Si manejas fotos

    private lateinit var editarPerfilLauncher: ActivityResultLauncher<Intent>
    private var seHicieronCambios = false // Flag para notificar a InicioDuenioActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_mi_mascota)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Obtener referencias a las vistas ---
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        tvTituloMascota = findViewById(R.id.tvTituloMascota)
        imagenMascota = findViewById(R.id.imagenMascota) // Asigna la referencia
        val editPhotoIcon = findViewById<ImageView>(R.id.editPhotoIcon)
        txtNombreMascota = findViewById(R.id.txtNombreMascota)
        txtRazaMascota = findViewById(R.id.txtRazaMascota)
        txtEdadMascota = findViewById(R.id.txtEdadMascota)
        txtEspecieMascota = findViewById(R.id.txtEspecieMascota)
        txtTamanioMascota = findViewById(R.id.txtTamanioMascota)
        txtDescripcionMascota = findViewById(R.id.txtDescripcionMascota)
        val btnEditarPerfilMascota = findViewById<Button>(R.id.btnEditarPerfilMascota)
        val botonEncargar = findViewById<Button>(R.id.btnEnviarSolicitud) // Asumo que es para otra funcionalidad

        // --- Recuperar datos del Intent ---
        mascotaIdActual = intent.getStringExtra("id_mascota") // <--- RECUPERAR ID
        nombreActual = intent.getStringExtra("nombre")
        razaActual = intent.getStringExtra("raza")
        edadActual = intent.getStringExtra("edad")
        especieActual = intent.getStringExtra("especie")
        tamanioActual = intent.getStringExtra("tamanio")
        descripcionActual = intent.getStringExtra("descripcion")
        fotoUrlActual = intent.getStringExtra("fotoUrl") // Si lo pasas

        // --- Verificar si el ID se recibió ---
        if (mascotaIdActual == null) {
            Toast.makeText(this, "Error: No se pudo cargar el perfil de la mascota (sin ID).", Toast.LENGTH_LONG).show()
            Log.e("PerfilMascota", "Error: 'id_mascota' es nulo en el Intent.")
            finish() // No se puede continuar sin el ID
            return
        }

        actualizarUI() // Poblar UI con datos iniciales

        // --- Configurar el launcher para el resultado de la edición ---
        editarPerfilLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Los datos se actualizaron en EditarPerfilMiMascotaActivity Y EN FIRESTORE
                Toast.makeText(this, "Perfil de mascota actualizado.", Toast.LENGTH_SHORT).show()
                seHicieronCambios = true // Marcar que hubo cambios para devolver a InicioDuenioActivity

                // Recargar datos desde Firestore para asegurar consistencia
                cargarDatosMascotaDesdeFirestore()


            } else {
                // El usuario podría haber cancelado la edición
                // Toast.makeText(this, "Edición cancelada", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Configurar Listeners ---
        btnBack.setOnClickListener {
            devolverResultadoAlAnterior()
            // finish() se llama dentro de devolverResultadoAlAnterior()
        }

        editPhotoIcon.setOnClickListener {
            // Lógica para editar foto (implementar si es necesario)
            Toast.makeText(this, "Editar foto (funcionalidad pendiente)", Toast.LENGTH_SHORT).show()
        }

        btnEditarPerfilMascota.setOnClickListener {
            if (mascotaIdActual == null) {
                Toast.makeText(this, "Error: No se puede editar sin ID de mascota.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intentEditar = Intent(this, EditarPerfilMiMascotaActivity::class.java)
            // Pasar los datos ACTUALES y el ID a la actividad de edición
            intentEditar.putExtra("EXTRA_ID_MASCOTA", mascotaIdActual) // <--- PASAR ID
            intentEditar.putExtra("EXTRA_NOMBRE", nombreActual)
            intentEditar.putExtra("EXTRA_ESPECIE", especieActual)
            intentEditar.putExtra("EXTRA_RAZA", razaActual)
            intentEditar.putExtra("EXTRA_EDAD", edadActual)
            intentEditar.putExtra("EXTRA_TAMANIO", tamanioActual)
            intentEditar.putExtra("EXTRA_DESCRIPCION", descripcionActual)
            intentEditar.putExtra("EXTRA_FOTO_URL", fotoUrlActual) // Si lo manejas

            editarPerfilLauncher.launch(intentEditar)
        }

        botonEncargar.setOnClickListener {
            val intentMapa = Intent(this, MapaActivity::class.java)
            // intentMapa.putExtra("ID_MASCOTA", mascotaIdActual) // Puedes pasar el ID si MapaActivity lo necesita
            startActivity(intentMapa)
        }
    }

    private fun cargarDatosMascotaDesdeFirestore() {
        if (mascotaIdActual == null) {
            Log.e("PerfilMascota", "cargarDatosMascotaDesdeFirestore: mascotaIdActual es nulo.")
            return
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado. No se pueden recargar los datos.", Toast.LENGTH_SHORT).show()
            Log.e("PerfilMascota", "cargarDatosMascotaDesdeFirestore: UID es nulo.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        Log.d("PerfilMascota", "Cargando datos desde Firestore: usuarios/$uid/mascotas/$mascotaIdActual")
        db.collection("usuarios").document(uid).collection("mascotas").document(mascotaIdActual!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.d("PerfilMascota", "Documento encontrado. Datos: ${documentSnapshot.data}")
                    val mascota = documentSnapshot.toObject(Mascota::class.java)
                    if (mascota != null) {
                        // Actualizar las variables miembro con los datos frescos de Firestore
                        // El ID no cambia, así que no es necesario reasignar mascotaIdActual aquí
                        // mascota.id = documentSnapshot.id // Ya lo tenemos en mascotaIdActual
                        nombreActual = mascota.nombre
                        razaActual = mascota.raza
                        edadActual = mascota.edad
                        especieActual = mascota.especie
                        tamanioActual = mascota.tamanio
                        descripcionActual = mascota.descripcion
                        fotoUrlActual = mascota.fotoUrl

                        actualizarUI() // Refrescar la UI
                    } else {
                        Log.e("PerfilMascota", "Error al convertir DocumentSnapshot a objeto Mascota.")
                        Toast.makeText(this, "Error al procesar datos de la mascota.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("PerfilMascota", "No se encontró el documento de la mascota en Firestore tras la edición.")
                    Toast.makeText(this, "No se encontraron datos actualizados de la mascota.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("PerfilMascota", "Error al recargar datos desde Firestore: ", e)
                Toast.makeText(this, "Error al recargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarUI() {
        tvTituloMascota.text = nombreActual ?: "Perfil de Mascota"
        txtNombreMascota.text = nombreActual ?: "No disponible"
        txtRazaMascota.text = razaActual ?: "No disponible"
        txtEdadMascota.text = if (edadActual != null && edadActual!!.isNotEmpty()) "$edadActual meses" else "No disponible"
        txtEspecieMascota.text = especieActual ?: "No disponible"
        txtTamanioMascota.text = tamanioActual ?: "No disponible"
        txtDescripcionMascota.text = descripcionActual ?: "No disponible"


    }

    private fun devolverResultadoAlAnterior() {
        if (seHicieronCambios) {

            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }


}