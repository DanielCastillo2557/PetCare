package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroDuenioDatosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_duenio_datos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroDuenio2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Encontrar las vistas por su ID (ahora sí existirán después de setContentView)
        val buttonGuardarRegistro: ImageButton = findViewById(R.id.btnGuardarRegistro)

        //Boton siguiente
        buttonGuardarRegistro.setOnClickListener {
            //Intent para navegar a InicioDuenioActivity
            val intent = Intent(this, InicioDuenioActivity::class.java)
            startActivity(intent)

        }
    }
}