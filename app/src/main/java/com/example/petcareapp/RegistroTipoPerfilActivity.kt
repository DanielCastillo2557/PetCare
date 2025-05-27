package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem // Importa MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroTipoPerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        
        // Carga el layout XML para esta Activity
        setContentView(R.layout.activity_registro_tipo_perfil)

        // Configuración para manejar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar la ActionBar (para el título y la flecha de retroceso)
        supportActionBar?.apply {
            // Habilitar el botón de retroceso (flecha hacia atrás)
            setDisplayHomeAsUpEnabled(true)
            // Cambiar el título de la ActionBar
            title = "Registro" // O el título que prefieras
        }

        // --- Encontrar las vistas por su ID (ahora sí existirán después de setContentView)
        val buttonDuenio: Button = findViewById(R.id.btnDuenio)
        val buttonCuidador: Button = findViewById(R.id.btnCuidador)

        // --- Establecer OnClickListener para cada botón ---

        buttonDuenio.setOnClickListener {
            // Aquí podrías navegar a una Activity para crear un perfil de dueño
            val intent = Intent(this, RegistroDuenioNombreFotoActivity::class.java)
            startActivity(intent)

        }

        buttonCuidador.setOnClickListener {
            // Aquí podrías navegar a una Activity para crear un perfil de cuidador
            val intent = Intent(this, RegistroCuidadorNombreFotoActivity::class.java)
            startActivity(intent)
        }
    }

    // --- Manejar el clic del botón de retroceso de la ActionBar ---
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Acción al presionar la flecha de retroceso
                // Esto típicamente regresa a la Activity anterior en la pila de navegación
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}