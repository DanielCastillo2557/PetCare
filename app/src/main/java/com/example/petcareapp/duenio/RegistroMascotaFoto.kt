package com.example.petcareapp.duenio

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
import com.example.petcareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


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

        val CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/dt25xxciq/image/upload"
        val CLOUDINARY_UPLOAD_PRESET = "petcare_preset" // debe estar configurado como "unsigned"

        btnGuardar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()

            if (imagenUrl != null) {
                val inputStream = contentResolver.openInputStream(imagenUrl!!)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes != null) {
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "mascota.jpg", imageBytes.toRequestBody("image/*".toMediaType()))
                        .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                        .build()

                    val request = Request.Builder()
                        .url(CLOUDINARY_UPLOAD_URL)
                        .post(requestBody)
                        .build()

                    val client = OkHttpClient()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@RegistroMascotaFoto, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val json = JSONObject(response.body!!.string())
                                val imageUrl = json.getString("secure_url")

                                val mascota = hashMapOf(
                                    "especie" to especie,
                                    "nombre" to nombre,
                                    "raza" to raza,
                                    "edad" to edad,
                                    "tamanio" to tamanio,
                                    "descripcion" to descripcion,
                                    "fotoUrl" to imageUrl
                                )

                                db.collection("usuarios").document(uid).collection("mascotas")
                                    .add(mascota)
                                    .addOnSuccessListener {
                                        runOnUiThread {
                                            Toast.makeText(this@RegistroMascotaFoto, "Mascota registrada con éxito", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this@RegistroMascotaFoto, InicioDuenioActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                    .addOnFailureListener {
                                        runOnUiThread {
                                            Toast.makeText(this@RegistroMascotaFoto, "Error al guardar mascota", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@RegistroMascotaFoto, "Error al obtener URL de imagen", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
            } else {
                val mascota = hashMapOf(
                    "especie" to especie,
                    "nombre" to nombre,
                    "raza" to raza,
                    "edad" to edad,
                    "tamanio" to tamanio,
                    "descripcion" to descripcion,
                    "fotoUrl" to "" // sin foto
                )

                db.collection("usuarios").document(uid).collection("mascotas")
                    .add(mascota)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Mascota registrada sin imagen", Toast.LENGTH_SHORT).show()
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
    //Manejo de la seleccion de la imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imagenUrl = data.data
            imgFotoMascota.setImageURI(imagenUrl)  // Cambia el ícono por la imagen seleccionada
        }
    }
}