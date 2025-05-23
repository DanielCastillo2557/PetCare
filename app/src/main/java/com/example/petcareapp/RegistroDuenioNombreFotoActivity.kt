package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroDuenioNombreFotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_duenio_nombre_foto)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroDuenio1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Encontrar las vistas por su ID (ahora sí existirán después de setContentView)
        val btnSiguiente: ImageButton = findViewById(R.id.btnSiguiente)

        //Boton siguiente
        btnSiguiente.setOnClickListener {
            //Tomamos el nombre ingresado por el usuario
            val nombre = findViewById<EditText>(R.id.editNombre).text.toString()

            //Intent para navegar a RegistroDuenioDatosActivity enviando el nombre
            val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
            intent.putExtra("nombre", nombre)
            startActivity(intent)

        }
    }
}