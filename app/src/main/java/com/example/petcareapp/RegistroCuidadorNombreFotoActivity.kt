package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton // Asegúrate de que esta importación esté
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroCuidadorNombreFotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cuidador_nombre_foto) // Asegúrate que este es el nombre correcto de tu archivo XML

        // Manejo de Insets para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroCuidador1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Lógica para el botón Siguiente ---
        //Tomamos el nombre y descripcion ingresado por el usuario
        val editNombre = findViewById<EditText>(R.id.editNombreCuidador)
        val editDescripcion = findViewById<EditText>(R.id.editDescripcionCuidador)
        val buttonSiguiente: ImageButton = findViewById(R.id.btnSiguienteCuidadorNombreFoto)

        //Boton siguiente
        buttonSiguiente.setOnClickListener {
            val nombre = editNombre.text.toString().trim() // .trim() para quitar espacios al inicio/final
            val descripcion = editDescripcion.text.toString().trim() // .trim()

            if (nombre.isBlank() || descripcion.isBlank()){
                // Mensaje un poco más específico y considerando ambos campos
                Toast.makeText(this, "Por favor, ingresa tu nombre y una descripción.", Toast.LENGTH_LONG).show()

                return@setOnClickListener
            }

            //Intent para navegar a RegistroCuidadorDatosActivity
            val intent = Intent(this, RegistroCuidadorDatosActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("descripcion", descripcion)
            startActivity(intent)
        }
        // --- Fin de la Lógica para el botón Siguiente ---
    }
}