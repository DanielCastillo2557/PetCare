package com.example.petcareapp.loginregister

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class RegistroDuenioNombreFotoActivity : AppCompatActivity() {
    private lateinit var imagePerfil: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_duenio_nombre_foto)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroDuenio1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imagePerfil = findViewById(R.id.imagePerfil)
        imagePerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val btnSiguiente = findViewById<ImageButton>(R.id.btnSiguiente)
        val editNombre = findViewById<EditText>(R.id.editNombreCuidador)

        btnSiguiente.setOnClickListener {
            val nombre = editNombre.text.toString().trim()

            if (nombre.isBlank()) {
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            imagenUri?.let { uri ->
                subirImagenACloudinary(
                    this,
                    uri,
                    onSuccess = { url ->
                        val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
                        intent.putExtra("nombre", nombre)
                        intent.putExtra("fotoUrl", url)
                        startActivity(intent)
                    },
                    onError = { error ->
                        runOnUiThread {
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } ?: run {
                val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
                intent.putExtra("nombre", nombre)
                intent.putExtra("fotoUrl", "")
                startActivity(intent)
            }
        }
    }

    private fun subirImagenACloudinary(
        context: Context,
        imagenUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val inputStream = context.contentResolver.openInputStream(imagenUri)
        val requestBody = inputStream?.readBytes()?.toRequestBody("image/*".toMediaType())
        if (requestBody == null) {
            onError("No se pudo leer la imagen")
            return
        }

        val request = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "foto_duenio.jpg", requestBody)
            .addFormDataPart("upload_preset", "petcare_preset")
            .build()

        val client = OkHttpClient()
        val requestFinal = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dt25xxciq/image/upload")
            .post(request)
            .build()

        client.newCall(requestFinal).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Error al subir: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("Error en respuesta: ${response.message}")
                    return
                }

                val json = JSONObject(response.body?.string() ?: "")
                val url = json.getString("secure_url")
                onSuccess(url)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imagenUri = data.data
            imagePerfil.setImageURI(imagenUri)
        }
    }
}
