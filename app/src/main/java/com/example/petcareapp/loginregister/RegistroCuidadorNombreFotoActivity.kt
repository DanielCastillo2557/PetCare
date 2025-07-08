package com.example.petcareapp.loginregister

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton // Asegúrate de que esta importación esté
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class RegistroCuidadorNombreFotoActivity : AppCompatActivity() {
    private lateinit var imagePerfil: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cuidador_nombre_foto) // Asegúrate que este es el nombre correcto de tu archivo XML
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
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

        // --- Lógica para el botón Siguiente ---
        //Tomamos el nombre y descripcion ingresado por el usuario
        val editNombre = findViewById<EditText>(R.id.editNombreCuidador)
        val editDescripcion = findViewById<EditText>(R.id.editDescripcionCuidador)
        val buttonSiguienteCuidador = findViewById<ImageButton>(R.id.btnSiguienteCuidadorNombreFoto)

        //Boton siguiente
        buttonSiguienteCuidador.setOnClickListener {
            val nombre = editNombre.text.toString() // .trim() para quitar espacios al inicio/final
            val descripcion = editDescripcion.text.toString() // .trim()

            if (nombre.isBlank() || descripcion.isBlank()){
                // Mensaje un poco más específico y considerando ambos campos
                Toast.makeText(this, "Por favor, ingresa tu nombre y una descripción.", Toast.LENGTH_LONG).show()
            }else{
                subirFotoYGuardarDatosCuidador(nombre, descripcion)
            }
        }
    }

    private fun subirFotoYGuardarDatosCuidador(nombre: String, descripcion: String) {
        imagenUri?.let { uri ->
            subirImagenACloudinary(
                context = this,
                imagenUri = uri,
                onSuccess = { url ->
                    val intent = Intent(this, RegistroCuidadorDatosActivity::class.java)
                    intent.putExtra("nombre", nombre)
                    intent.putExtra("descripcion", descripcion)
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
            // Si no se seleccionó imagen, continuar sin foto
            val intent = Intent(this, RegistroCuidadorDatosActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("descripcion", descripcion)
            intent.putExtra("fotoUrl", "")
            startActivity(intent)
        }
    }

    fun subirImagenACloudinary(
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
            .addFormDataPart("file", "mi_foto.jpg", requestBody)
            .addFormDataPart("upload_preset", "petcare_preset") // Reemplaza con tu preset
            .build()

        val client = OkHttpClient()
        val requestFinal = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dt25xxciq/image/upload") // Reemplaza <tu_cloud_name>
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


    //Manejo de la seleccion de la imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imagenUri = data.data
            imagePerfil.setImageURI(imagenUri)  // Cambia el ícono por la imagen seleccionada
        }
    }
}