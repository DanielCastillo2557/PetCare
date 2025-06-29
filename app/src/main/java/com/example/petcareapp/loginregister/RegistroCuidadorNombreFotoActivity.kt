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
        val buttonSiguiente = findViewById<ImageButton>(R.id.btnSiguienteCuidadorNombreFoto)

        //Boton siguiente
        buttonSiguiente.setOnClickListener {
            val nombre = editNombre.text.toString().trim() // .trim() para quitar espacios al inicio/final
            val descripcion = editDescripcion.text.toString().trim() // .trim()

            if (nombre.isBlank() || descripcion.isBlank()){
                // Mensaje un poco más específico y considerando ambos campos
                Toast.makeText(this, "Por favor, ingresa tu nombre y una descripción.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else{
                subirFotoYGuardarDatos(nombre, descripcion)
            }
        }
    }

    private fun subirFotoYGuardarDatos(nombre: String, descripcion: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("fotos_perfil/$uid.jpg")

        imagenUri?.let { uri ->
            Log.d("Registro", "URI seleccionada: ${uri}")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val intent = Intent(this, RegistroCuidadorDatosActivity::class.java)
                        intent.putExtra("nombre", nombre)
                        intent.putExtra("descripcion", descripcion)
                        intent.putExtra("fotoUrl", downloadUri.toString())
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // Si imagenUri es null, no se subió una imagen, así que se guarda sin foto
            val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("descripcion", descripcion)
            intent.putExtra("fotoUrl", "")
            startActivity(intent)
        }
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