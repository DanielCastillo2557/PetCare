package com.example.petcareapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView // Importar TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent

class InicioCuidadorActivity : AppCompatActivity() {

    // Declarar el TextView como una variable miembro para acceder desde los listeners
    private lateinit var tvPantallaSeleccionada: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencia al TextView
        tvPantallaSeleccionada = findViewById(R.id.tvPantallaSeleccionada)

        // Obtener referencias a los botones (ImageViews)
        val btnPerfilCuidadorSuperior: ImageView = findViewById(R.id.btnPerfilCuidador)
        val btnNavPerfilCuidadorInferior: ImageView = findViewById(R.id.btnNavPerfilCuidador)
        val btnNavMapaCuidador: ImageView = findViewById(R.id.btnNavMapaCuidador)
        val btnNavChatsCuidador: ImageView = findViewById(R.id.btnNavChatsCuidador)

        // Configurar OnClickListeners para los botones

        btnPerfilCuidadorSuperior.setOnClickListener {
            tvPantallaSeleccionada.text = "Pantalla de Perfil (Superior)"
        }

        btnNavPerfilCuidadorInferior.setOnClickListener {
            // tvPantallaSeleccionada.text = "Pantalla de Perfil" // Puedes mantener esto si quieres para debug
            val intent = Intent(this, PerfilCuidadorActivity::class.java)
            // Considera flags para una mejor gestión de la pila de actividades si es necesario
            // intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            // o
            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnNavMapaCuidador.setOnClickListener {

            // Navegar a MapaCuidadorActivity
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            startActivity(intent)
        }

        btnNavChatsCuidador.setOnClickListener {
            tvPantallaSeleccionada.text = "Pantalla de Chats"
        }
    }
}