package com.example.petcareapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore


class DetalleSolicitudActivity : AppCompatActivity() {
    private lateinit var imageMascota: ImageView
    private lateinit var textoNombre: TextView
    private lateinit var textoEspecie: TextView
    private lateinit var textoRaza: TextView
    private lateinit var textoEdad: TextView
    private lateinit var textoTamano: TextView
    private lateinit var textoDescripcion: TextView
    private lateinit var btnAceptar: Button
    private lateinit var idMascota: String
    private lateinit var idDueno: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_solicitud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recibir los datos del intent
        idMascota = intent.getStringExtra("idMascota") ?: ""
        idDueno = intent.getStringExtra("idDueno") ?: ""

        if (idMascota.isBlank() || idDueno.isBlank()) {
            Toast.makeText(this, "Error al obtener los datos de la mascota", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Referencias a vistas
        imageMascota = findViewById(R.id.imagenMascota)
        textoNombre = findViewById(R.id.txtNombreMascota)
        textoEspecie = findViewById(R.id.txtEspecieMascota)
        textoRaza = findViewById(R.id.txtRazaMascota)
        textoEdad = findViewById(R.id.txtEdadMascota)
        textoTamano = findViewById(R.id.txtTamanioMascota)
        textoDescripcion = findViewById(R.id.txtDescripcionMascota)
        btnAceptar = findViewById(R.id.btnAceptarSolicitud)

        cargarDatosMascotaIntent()
        //cargarDatosMascota(idMascota, idDueno)

        btnAceptar.setOnClickListener {
            aceptarSolicitud()
        }
    }

    private fun cargarDatosMascotaIntent() {
        textoNombre.text = intent.getStringExtra("nombreMascota") ?: ""
        textoEspecie.text = intent.getStringExtra("especie") ?: ""
        textoRaza.text = intent.getStringExtra("raza") ?: ""
        textoEdad.text = intent.getStringExtra("edad") ?: ""
        textoTamano.text = intent.getStringExtra("tamanio") ?: ""
        textoDescripcion.text = intent.getStringExtra("descripcion") ?: ""

        val fotoUrl = intent.getStringExtra("fotoUrl")
        if (!fotoUrl.isNullOrEmpty()) {
            Glide.with(this).load(fotoUrl).into(imageMascota)
        }
    }

    private fun aceptarSolicitud() {
        // Aquí va la lógica para actualizar el estado de la solicitud y abrir el chat
        Toast.makeText(this, "Solicitud aceptada (falta implementar chat)", Toast.LENGTH_SHORT).show()

        // Ejemplo: abrir actividad ChatActivity (debes implementarla)
        /*
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("idDueno", idDueno)
        startActivity(intent)
        */
    }
}