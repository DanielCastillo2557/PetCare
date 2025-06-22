package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

        // Inicializar Firebase y obtener el ID del cuidador seleccionado
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        cuidadorId = intent.getStringExtra("cuidadorId") ?: return

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
                    findViewById<TextView>(R.id.txtNombreCuidador).text = doc.getString("nombre") ?: ""
                    findViewById<TextView>(R.id.txtDireccionCuidador).text = doc.getString("direccion") ?: ""

                    //val fotoUrl = doc.getString("foto_url")
                    //if (!fotoUrl.isNullOrEmpty()) {
                    //    val imgView = findViewById<ImageView>(R.id.imgPerfilCuidador)
                    //    Glide.with(this).load(fotoUrl).into(imgView)
                    //}
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del cuidador", Toast.LENGTH_SHORT).show()
            }
    }

    // Dejar la solicitud al cuidador
    private fun dejarSolicitud() {
        val uidDuenio = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uidDuenio).get()
            .addOnSuccessListener { doc ->
                val nombreDuenio = doc.getString("nombre") ?: ""

                val solicitud = hashMapOf(
                    "idDueno" to uidDuenio,
                    "nombreDueno" to nombreDuenio,
                    "fecha" to FieldValue.serverTimestamp(),
                    "estado" to "pendiente",

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
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al enviar la solicitud", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
    }
}