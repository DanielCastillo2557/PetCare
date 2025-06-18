package com.example.petcareapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class CuidadorSeleccionadoActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cuidador_seleccionado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el ID del cuidador seleccionado
        val cuidadorId = intent.getStringExtra("cuidadorId")
        if (cuidadorId != null) {
            db.collection("usuarios").document(cuidadorId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val nombre = doc.getString("nombre") ?: ""
                        val direccion = doc.getString("direccion") ?: ""
                        // ... otros campos

                        // cargar los valores en los TextView
                        findViewById<TextView>(R.id.txtNombreCuidador).text = nombre
                        findViewById<TextView>(R.id.txtDireccionCuidador).text = direccion
                        // ... otros campos
                    }
                }
        }
    }
}