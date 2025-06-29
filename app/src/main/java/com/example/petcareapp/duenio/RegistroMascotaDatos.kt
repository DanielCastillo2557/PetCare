package com.example.petcareapp.duenio

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R


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

        // Referencia al botón de retroceso
        val btnBack = findViewById<ImageView>(R.id.btnBack) // Especifica ImageView

        val spinnerEspecie = findViewById<Spinner>(R.id.spinnerEspecie)
        val spinnerRaza = findViewById<Spinner>(R.id.spinnerRaza)
        val spinnerTamanio = findViewById<Spinner>(R.id.spinnerTamaño)
        val editNombre = findViewById<EditText>(R.id.editNombreMascota)
        val editEdad = findViewById<EditText>(R.id.editEdadMascota)
        val editDescripcion = findViewById<EditText>(R.id.editDescripcion)
        val btnSiguiente = findViewById<ImageButton>(R.id.btnSiguienteMascota)

        // Configura los Spinners con datos de prueba o de tu base de datos
        val especies = listOf("Selecciona una especie", "Perro", "Gato", "Conejo", "Hamster", "Otro")
        val razas = listOf("Selecciona una raza", "Labrador", "Bulldog", "Siames", "Otra") // Corregido "Seleecciona"
        val tamanios = listOf("Selecciona un tamaño", "Pequeño", "Mediano", "Grande") // Corregido "Seleeciona"

        spinnerEspecie.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, especies)
        spinnerRaza.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, razas)
        spinnerTamanio.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tamanios)

        // Listener para el botón de retroceso
        btnBack.setOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la anterior
        }

        btnSiguiente.setOnClickListener {
            val especie = spinnerEspecie.selectedItem.toString()
            val nombre = editNombre.text.toString().trim() // Añadido trim()
            val raza = spinnerRaza.selectedItem.toString()
            val edad = editEdad.text.toString().trim() // Añadido trim()
            val tamanio = spinnerTamanio.selectedItem.toString()
            val descripcion = editDescripcion.text.toString().trim() // Añadido trim()

            // Validación para los Spinners (opcional pero recomendado)
            if (especie == especies[0] || raza == razas[0] || tamanio == tamanios[0]) {
                Toast.makeText(this, "Por favor selecciona todas las opciones de los desplegables", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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