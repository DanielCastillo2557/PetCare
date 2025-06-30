package com.example.petcareapp.duenio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R
import com.example.petcareapp.loginregister.RegistroDuenioDatosActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegistroMascotaFoto : AppCompatActivity() {

    private lateinit var imgFotoMascota: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var imagenUrl: Uri? = null
    private lateinit var iconoCamara: ImageView
    private lateinit var btnGuardar: ImageButton

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

        // Configurar campos de texto con los datos recuperados
        iconoCamara.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        btnGuardar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()
            val storageRef = FirebaseStorage.getInstance().reference.child("fotos_perfil/$uid.jpg")
            imagenUrl?.let { uri ->
                Log.d("Registro", "URI seleccionada: ${uri}")
                storageRef.putFile(uri)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val imagenUrl = downloadUri.toString()
                            val mascota = hashMapOf(
                                "especie" to especie,
                                "nombre" to nombre,
                                "raza" to raza,
                                "edad" to edad,
                                "tamanio" to tamanio,
                                "descripcion" to descripcion,
                                "fotoUrl" to imagenUrl
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
                            //guardarDatosEnFirestore(nombre, downloadUri.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                // Si no se seleccionó imagen, guarda solo el nombre y una URL vacía
                //guardarDatosEnFirestore(nombre, "")

                //Intent para navegar a RegistroDuenioDatosActivity enviando el nombre
                val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
                intent.putExtra("nombre", nombre)
                intent.putExtra("fotoUrl", "")
                startActivity(intent)
            }
        }
    }
    //Manejo de la seleccion de la imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imagenUrl = data.data
            imgFotoMascota.setImageURI(imagenUrl)  // Cambia el ícono por la imagen seleccionada
        }
    }
}