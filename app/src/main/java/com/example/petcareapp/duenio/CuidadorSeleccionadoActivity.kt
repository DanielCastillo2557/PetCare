package com.example.petcareapp.duenio

import android.content.Intent
import android.os.Bundle
import android.util.Log // Importar Log para depuración
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.petcareapp.R
import com.example.petcareapp.models.Solicitud
// import com.google.firebase.Timestamp // Ya no se necesita aquí si fecha es @ServerTimestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CuidadorSeleccionadoActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cuidadorId: String
    private lateinit var idMascota: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cuidador_seleccionado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cuidadorId = intent.getStringExtra("cuidadorId") ?: ""
        idMascota = intent.getStringExtra("idMascota") ?: ""

        if (cuidadorId.isEmpty() || idMascota.isEmpty()) { // Verificar también idMascota
            Toast.makeText(this, "Error: Datos incompletos para procesar la solicitud.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarDatosCuidador(cuidadorId)

        val btnEnviarSolicitud = findViewById<Button>(R.id.btnEnviarSolicitud)
        btnEnviarSolicitud.setOnClickListener {
            dejarSolicitud()
        }
    }

    private fun cargarDatosCuidador(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    findViewById<TextView>(R.id.txtNombreCuidador).text = doc.getString("nombre") ?: "Nombre no disponible"
                    findViewById<TextView>(R.id.txtDireccionCuidador).text = doc.getString("direccion") ?: "Dirección no disponible"
                    //findViewById<TextView>(R.id.txtPuntuacionCuidador).text = doc.getString("puntuacion") ?: "Puntuación no disponible"
                    findViewById<TextView>(R.id.txtDescCuidador).text = doc.getString("descripcion") ?: "Descripción no disponible"

                    val fotoUrl = doc.getString("foto_url") ?: ""
                    if (fotoUrl.isNotBlank()){
                        val imagenPerfil = findViewById<ImageView>(R.id.cuidadorImage)
                        Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_user) // Imagen por defecto mientras carga
                            .error(R.drawable.ic_error) // Si hay error en la URL
                            .into(imagenPerfil)
                    }
                } else {
                    Toast.makeText(this, "No se encontraron datos para este cuidador.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del cuidador", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dejarSolicitud() {
        val uidDuenio = auth.currentUser?.uid
        if (uidDuenio == null) {
            Toast.makeText(this, "Debes iniciar sesión para enviar una solicitud.", Toast.LENGTH_SHORT).show()
            return
        }

        // Paso 1: Obtener datos de la mascota
        db.collection("usuarios").document(uidDuenio)
            .collection("mascotas").document(idMascota)
            .get()
            .addOnSuccessListener { mascotaDoc ->
                if (!mascotaDoc.exists()) {
                    Toast.makeText(this, "Error: No se encontraron datos de la mascota.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val nombreMascota = mascotaDoc.getString("nombre") ?: ""
                val especie = mascotaDoc.getString("especie") ?: ""
                val raza = mascotaDoc.getString("raza") ?: ""
                val edadStr = mascotaDoc.getString("edad") ?: "0" // Asumiendo que 'edad' en la mascota es String
                // Si 'edad' en la mascota es Long/Int, conviértelo a String directamente:
                // val edadMascota = mascotaDoc.getLong("edad")?.toString() ?: "0"
                val tamanio = mascotaDoc.getString("tamanio") ?: ""
                val descripcion = mascotaDoc.getString("descripcion") ?: ""
                val fotoUrl = mascotaDoc.getString("fotoUrl") ?: ""

                // Paso 2: Obtener el nombre del dueño desde el documento del usuario
                db.collection("usuarios").document(uidDuenio).get()
                    .addOnSuccessListener { userDoc ->
                        val nombreDuenioReal = userDoc.getString("nombre") ?: "Dueño Anónimo"

                        // Paso 3: Crear el objeto Solicitud
                        val solicitud = Solicitud(
                            // id será asignado por Firestore (@DocumentId)
                            idMascota = idMascota,
                            // fecha será asignada por Firestore (@ServerTimestamp)
                            estado = "pendiente", // Valor por defecto en el modelo, pero se puede especificar
                            idDueno = uidDuenio,
                            nombreDueno = nombreDuenioReal,
                            nombreMascota = nombreMascota,
                            especie = especie,
                            raza = raza,
                            edad = edadStr, // Usa el String directamente si así lo tienes en el modelo Solicitud
                            // Si edad en el modelo Solicitud fuera Int/Long, y edadStr es String:
                            // edad = edadStr.toLongOrNull() ?: 0L (o toIntOrNull)
                            tamanio = tamanio,
                            descripcion = descripcion,
                            fotoUrl = fotoUrl
                            // idChat es nullable y tiene valor por defecto, se puede omitir
                        )

                        // Paso 4: Guardar la solicitud
                        db.collection("usuarios").document(cuidadorId)
                            .collection("solicitudes").add(solicitud)
                            .addOnSuccessListener {
                                AlertDialog.Builder(this)
                                    .setTitle("Solicitud enviada")
                                    .setMessage("Tu solicitud ha sido enviada correctamente.")
                                    .setPositiveButton("Aceptar") { _, _ ->
                                        val intent = Intent(this, InicioDuenioActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                        finish()
                                    }
                                    .setCancelable(false)
                                    .show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("CuidadorSeleccionado", "Error al enviar solicitud", e)
                                Toast.makeText(this, "Error al enviar solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CuidadorSeleccionado", "Error al obtener datos del dueño", e)
                        Toast.makeText(this, "Error al obtener datos del dueño: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CuidadorSeleccionado", "Error al obtener datos de la mascota", e)
                Toast.makeText(this, "Error al obtener datos de la mascota: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}