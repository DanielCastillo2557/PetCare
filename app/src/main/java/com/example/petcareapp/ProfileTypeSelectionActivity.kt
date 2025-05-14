package com.example.petcareapp

import android.content.Intent // Importa Intent si planeas navegar desde aquí
import android.os.Bundle
import android.view.MenuItem // Importa MenuItem
import android.widget.Button
import android.widget.TextView // Importa TextView
import android.widget.Toast // Importa Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileTypeSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        
        // Carga el layout XML para esta Activity
        setContentView(R.layout.activity_profile_type_selection)
        // *** FIN DE LO QUE FALTA ***

        // Configuración para manejar las barras del sistema (esto ya lo tienes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Configurar la ActionBar (para el título y la flecha de retroceso) ---
        supportActionBar?.apply {
            // Habilitar el botón de retroceso (flecha hacia atrás)
            setDisplayHomeAsUpEnabled(true)
            // Cambiar el título de la ActionBar
            title = "Registro" // O el título que prefieras
        }

        // --- Encontrar las vistas por su ID (ahora sí existirán después de setContentView) ---
        val textViewTitle: TextView = findViewById(R.id.textViewProfileDefinition) // El texto "Define tu perfil"
        val buttonOwner: Button = findViewById(R.id.buttonOwner)
        val buttonCaretaker: Button = findViewById(R.id.buttonCaretaker)

        // --- Establecer OnClickListener para cada botón ---

        buttonOwner.setOnClickListener {
            // Código a ejecutar cuando se hace clic en "Soy dueño de mascota"
            Toast.makeText(this, "Seleccionado: Dueño de mascota", Toast.LENGTH_SHORT).show()
            // Aquí podrías navegar a una Activity para crear un perfil de dueño
            // val ownerProfileIntent = Intent(this, CreateOwnerProfileActivity::class.java)
            // startActivity(ownerProfileIntent)
        }

        buttonCaretaker.setOnClickListener {
            // Código a ejecutar cuando se hace clic en "Soy cuidador de mascota"
            Toast.makeText(this, "Seleccionado: Cuidador de mascota", Toast.LENGTH_SHORT).show()
            // Aquí podrías navegar a una Activity para crear un perfil de cuidador
            // val caretakerProfileIntent = Intent(this, CreateCaretakerProfileActivity::class.java)
            // startActivity(caretakerProfileIntent)
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