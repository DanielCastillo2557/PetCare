package com.example.petcareapp.loginregister

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

        // --- Encontrar las vistas por su ID (ahora sí existirán después de setContentView)
        val btnSiguiente: ImageButton = findViewById(R.id.btnSiguiente)

        //Boton siguiente
        btnSiguiente.setOnClickListener {
            //Tomamos el nombre ingresado por el usuario
            val nombre = findViewById<EditText>(R.id.editNombreCuidador).text.toString()

            if (nombre.isBlank()){
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                subirFotoYGuardarDatos(nombre)

                //Intent para navegar a RegistroDuenioDatosActivity enviando el nombre
                //val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
                //intent.putExtra("nombre", nombre)
                //startActivity(intent)
            }
        }
    }

    fun subirFotoYGuardarDatos(nombre: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("fotos_perfil/$uid.jpg")

        imagenUri?.let { uri ->
            Log.d("Registro", "URI seleccionada: ${uri}")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
                        intent.putExtra("nombre", nombre)
                        intent.putExtra("fotoUrl", downloadUri.toString())
                        startActivity(intent)
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

    fun guardarDatosEnFirestore(nombre: String, fotoUrl: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usuario = hashMapOf(
            "nombre" to nombre,
            "foto_url" to fotoUrl,
            "tipo" to listOf("duenio") // o como lo estés manejando
        )

        FirebaseFirestore.getInstance().collection("usuarios")
            .document(uid)
            .set(usuario)
            .addOnSuccessListener {
                // Redirigir a la siguiente pantalla
                startActivity(Intent(this, RegistroDuenioDatosActivity::class.java))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
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
