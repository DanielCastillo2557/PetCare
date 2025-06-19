package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PerfilMiMascotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_mi_mascota)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nombre = intent.getStringExtra("nombre")
        val raza = intent.getStringExtra("raza")
        val edad = intent.getIntExtra("edad", 0)
        val especie = intent.getStringExtra("especie")
        val tamanio = intent.getStringExtra("tamanio")
        val descripcion = intent.getStringExtra("descripcion")

        val txtNombreMascota = findViewById<TextView>(R.id.txtNombreCuidador)
        val txtRazaMascota = findViewById<TextView>(R.id.txtPuntuacionCuidador)
        val txtEdadMascota = findViewById<TextView>(R.id.txtEdadMascota)
        val txtEspecieMascota = findViewById<TextView>(R.id.txtDireccionCuidador)
        val txtTamanioMascota = findViewById<TextView>(R.id.txtTamanioMascota)
        val txtDescripcionMascota = findViewById<TextView>(R.id.txtDescCuidador)

        txtNombreMascota.text = nombre
        txtRazaMascota.text = raza
        txtEdadMascota.text = edad.toString()
        txtEspecieMascota.text = especie
        txtTamanioMascota.text = tamanio
        txtDescripcionMascota.text = descripcion

        val botonEncargar = findViewById<Button>(R.id.btnEnviarSolicitud)
        botonEncargar.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
        }


    }
}