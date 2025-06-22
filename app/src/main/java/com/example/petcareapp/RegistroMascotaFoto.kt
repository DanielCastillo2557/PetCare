package com.example.petcareapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroMascotaFoto : AppCompatActivity() {

    private lateinit var imgFotoMascota: ImageView
    private lateinit var iconoCamara: ImageView
    private lateinit var btnGuardar: ImageButton

    private var uriFoto: Uri? = null  // Para almacenar la URI de la foto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_mascota_foto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgFotoMascota = findViewById(R.id.imgFotoMascota)
        iconoCamara = findViewById(R.id.iconoCamaraMascota)
        btnGuardar = findViewById(R.id.btnGuardarMascota)

        // Recuperar datos del Intent
        val especie = intent.getStringExtra("especie") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val raza = intent.getStringExtra("raza") ?: ""
        val edad = intent.getStringExtra("edad") ?: ""
        val tamanio = intent.getStringExtra("tamanio") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""

        // Configurar botón de cámara (esto es opcional y requiere permisos)
        iconoCamara.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        btnGuardar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()
            val mascota = hashMapOf(
                "especie" to especie,
                "nombre" to nombre,
                "raza" to raza,
                "edad" to edad,
                "tamanio" to tamanio,
                "descripcion" to descripcion,
                "fotoUrl" to (uriFoto?.toString() ?: "")  // Aquí puedes subir a Storage si quieres
            )

            db.collection("usuarios").document(uid).collection("mascotas")
                .add(mascota)
                .addOnSuccessListener {
                    Toast.makeText(this, "Mascota registrada con éxito", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, InicioDuenioActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar mascota", Toast.LENGTH_SHORT).show()
                }
        }
    }
}