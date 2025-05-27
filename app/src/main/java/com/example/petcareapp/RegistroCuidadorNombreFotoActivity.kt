package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroCuidadorNombreFotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cuidador_nombre_foto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroCuidador1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Tomamos el nombre y descripcion ingresado por el usuario
        val editNombre = findViewById<EditText>(R.id.editNombreCuidador)
        val editDescripcion = findViewById<EditText>(R.id.editDescripcionCuidador)
        val buttonSiguiente: ImageButton = findViewById(R.id.btnSiguienteCuidadorNombreFoto)

        //Boton siguiente
        buttonSiguiente.setOnClickListener {
            val nombre = editNombre.text.toString()
            val descripcion = editDescripcion.text.toString()

            if (nombre.isBlank() || descripcion.isBlank()){
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Intent para navegar a RegistroDuenioDatosActivity
            val intent = Intent(this, RegistroCuidadorDatosActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("descripcion", descripcion)
            startActivity(intent)

        }
    }
}