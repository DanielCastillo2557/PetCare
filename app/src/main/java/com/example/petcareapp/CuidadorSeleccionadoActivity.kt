package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton // Importar ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CuidadorSeleccionadoActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cuidadorId: String

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
            // Opción 1: Usar onBackPressedDispatcher (recomendado para consistencia con el gesto de back)
            onBackPressedDispatcher.onBackPressed()

            // Opción 2: Simplemente finalizar la actividad (más directo si no necesitas un manejo complejo del back stack)
            // finish()
        }
        // --- Fin de la inicialización del botón de retroceso ---


        // Inicializar Firebase y obtener el ID del cuidador seleccionado
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cuidadorId = intent.getStringExtra("cuidadorId") ?: "" // Es mejor asignar un valor por defecto o manejar el error

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


        db.collection("usuarios").document(uidDuenio).get()
            .addOnSuccessListener { doc ->
                val nombreDuenio = doc.getString("nombre") ?: "Dueño Anónimo"
                val idMascota = intent.getStringExtra("idMascota")


                val solicitud = hashMapOf(
                    "idDueno" to uidDuenio,
                    "nombreDueno" to nombreDuenio,
                    "fecha" to FieldValue.serverTimestamp(),
                    "estado" to "pendiente",
                    "idMascota" to idMascota
                )

                // Agregar la solicitud a la colección "solicitudes" del cuidador
                db.collection("usuarios")
                    .document(cuidadorId)
                    .collection("solicitudes")
                    .add(solicitud)
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
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al enviar la solicitud: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener datos del dueño: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}