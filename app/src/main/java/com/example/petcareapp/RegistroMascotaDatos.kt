package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.DatePickerDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class RegistroMascotaDatos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_mascota_datos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinnerEspecie = findViewById<Spinner>(R.id.spinnerEspecie)
        val spinnerRaza = findViewById<Spinner>(R.id.spinnerRaza)
        val spinnerTamanio = findViewById<Spinner>(R.id.spinnerTamaño)
        val editNombre = findViewById<EditText>(R.id.editNombreMascota)
        val editEdad = findViewById<EditText>(R.id.editEdadMascota)
        val editDescripcion = findViewById<EditText>(R.id.editDescripcion)
        val btnSiguiente = findViewById<ImageButton>(R.id.btnSiguienteMascota)

        // Configura los Spinners con datos de prueba o de tu base de datos
        val especies = listOf("Selecciona una especie", "Perro", "Gato", "Conejo", "Hamster", "Otro")
        val razas = listOf("Seleecciona una raza", "Labrador", "Bulldog", "Siames", "Otra")
        val tamanios = listOf("Seleeciona un tamaño", "Pequeño", "Mediano", "Grande")

        spinnerEspecie.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, especies)
        spinnerRaza.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, razas)
        spinnerTamanio.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tamanios)

        btnSiguiente.setOnClickListener {
            val especie = spinnerEspecie.selectedItem.toString()
            val nombre = editNombre.text.toString()
            val raza = spinnerRaza.selectedItem.toString()
            val edad = editEdad.text.toString()
            val tamanio = spinnerTamanio.selectedItem.toString()
            val descripcion = editDescripcion.text.toString()

            if (nombre.isBlank() || edad.isBlank() || descripcion.isBlank()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pasar datos a la siguiente Activity
            val intent = Intent(this, RegistroMascotaFoto::class.java)
            intent.putExtra("especie", especie)
            intent.putExtra("nombre", nombre)
            intent.putExtra("raza", raza)
            intent.putExtra("edad", edad)
            intent.putExtra("tamanio", tamanio)
            intent.putExtra("descripcion", descripcion)
            startActivity(intent)
        }
    }
}