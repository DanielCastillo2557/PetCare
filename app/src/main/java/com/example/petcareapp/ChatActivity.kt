package com.example.petcareapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {
    private lateinit var idDueno: String
    private lateinit var idCuidador: String
    private lateinit var idMascota: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idDueno = intent.getStringExtra("idDueno") ?: ""
        idMascota = intent.getStringExtra("idMascota") ?: ""
        idCuidador = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}