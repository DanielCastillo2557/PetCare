package com.example.petcareapp.duenio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton // Importar ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R
import com.example.petcareapp.models.Solicitud
import com.google.firebase.Timestamp
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

        // --- Inicializar el botón de retroceso ---
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Volver a la actividad anterior
            onBackPressedDispatcher.onBackPressed()
        }

        // Inicializar Firebase y obtener el ID del cuidador seleccionado
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cuidadorId = intent.getStringExtra("cuidadorId") ?: "" // Es mejor asignar un valor por defecto o manejar el error
        idMascota = intent.getStringExtra("idMascota") ?: ""

        // Verificar si cuidadorId está vacío y manejarlo, por ejemplo, finalizando la actividad
        if (cuidadorId.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del cuidador.", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad si no hay ID
            return   // Importante salir del onCreate para evitar más ejecuciones
        }

        cargarDatosCuidador(cuidadorId)

        // Configurar el botón para dejar la solicitud
        val btnEnviarSolicitud = findViewById<Button>(R.id.btnEnviarSolicitud)
        btnEnviarSolicitud.setOnClickListener {
            dejarSolicitud()
        }
    }

    // Cargar los datos del cuidador seleccionado
    private fun cargarDatosCuidador(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Actualizar la interfaz de usuario con los datos del cuidador
                    findViewById<TextView>(R.id.txtNombreCuidador).text = doc.getString("nombre") ?: "Nombre no disponible" // Valores por defecto
                    findViewById<TextView>(R.id.txtDireccionCuidador).text = doc.getString("direccion") ?: "Dirección no disponible"
                    // Suponiendo que tienes estos TextViews en tu layout
                    findViewById<TextView>(R.id.txtPuntuacionCuidador).text = doc.getString("puntuacion") ?: "Puntuación no disponible"
                    findViewById<TextView>(R.id.txtDescCuidador).text = doc.getString("descripcion") ?: "Descripción no disponible"
                } else {
                    Toast.makeText(this, "No se encontraron datos para este cuidador.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del cuidador", Toast.LENGTH_SHORT).show()
            }
    }

    // Dejar la solicitud al cuidador
    private fun dejarSolicitud() {
        val uidDuenio = auth.currentUser?.uid
        if (uidDuenio == null) {
            Toast.makeText(this, "Debes iniciar sesión para enviar una solicitud.", Toast.LENGTH_SHORT).show()
            // Podrías redirigir al login aquí
            return
        }

        db.collection("usuarios").document(uidDuenio)
            .collection("mascotas").document(idMascota)
            .get()
            .addOnSuccessListener { doc ->
                val nombreMascota = doc.getString("nombre") ?: ""
                val especie = doc.getString("especie") ?: ""
                val raza = doc.getString("raza") ?: ""
                val edadStr = doc.getString("edad") ?: "0"
                val edad = edadStr.toIntOrNull() ?: 0
                val tamanio = doc.getString("tamanio") ?: ""
                val descripcion = doc.getString("descripcion") ?: ""
                val fotoUrl = doc.getString("fotoUrl") ?: ""

                val nombreDuenio = doc.getString("nombreDuenio") ?: "Dueño Anónimo"

                val solicitud = Solicitud(
                    idMascota = idMascota,
                    idDueno = uidDuenio,
                    nombreDueno = nombreDuenio,
                    fecha = Timestamp.now(),
                    estado = "pendiente",

                    // Datos duplicados
                    nombreMascota = nombreMascota,
                    especie = especie,
                    raza = raza,
                    edad = edad,
                    tamanio = tamanio,
                    descripcion = descripcion,
                    fotoUrl = fotoUrl
                )

                db.collection("usuarios").document(cuidadorId)
                    .collection("solicitudes").add(solicitud)
                    .addOnSuccessListener {
                        // Mostrar un mensaje de éxito y volver a la pantalla de inicio del dueño
                        AlertDialog.Builder(this)
                            .setTitle("Solicitud enviada")
                            .setMessage("Tu solicitud ha sido enviada correctamente.")
                            .setPositiveButton("Aceptar") { _, _ ->

                                val intent = Intent(this, InicioDuenioActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            .setCancelable(false) // Evitar que se cierre al tocar fuera
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                    }
            }


            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener datos del dueño: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}