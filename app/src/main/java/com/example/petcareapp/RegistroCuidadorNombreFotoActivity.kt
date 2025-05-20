package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
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

        // --- Encontrar las vistas por su ID (ahora sí existirán después de setContentView)
        val buttonSiguiente: ImageButton = findViewById(R.id.btnSiguienteCuidadorNombreFoto)

        //Boton siguiente
        buttonSiguiente.setOnClickListener {
            //Intent para navegar a RegistroDuenioDatosActivity
            val intent = Intent(this, RegistroDuenioDatosActivity::class.java)
            startActivity(intent)

        }
    }
}