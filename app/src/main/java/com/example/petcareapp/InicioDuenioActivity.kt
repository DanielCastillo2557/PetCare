package com.example.petcareapp

import android.app.Activity // Para Activity.RESULT_OK
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty

// import androidx.core.view.isEmpty

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioDuenioActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MascotaAdapter
    private lateinit var listaMascotas: MutableList<Mascota>
    private lateinit var layoutVacio: LinearLayout
    private lateinit var btnPerfil: ImageView
    private lateinit var fabAgregarMascota: FloatingActionButton

    // --- Botones de la barra de navegación inferior ---
    private lateinit var btnNavMisMascotas: ImageView
    private lateinit var btnNavMisChats: ImageView

    // --- Launcher para el resultado de PerfilMiMascotaActivity ---
    private lateinit var perfilMascotaLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_duenio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerMascotas)
        layoutVacio = findViewById(R.id.layoutVacio)
        val btnAgregarMascota: ImageButton = findViewById(R.id.btnAgregarMascota)
        fabAgregarMascota = findViewById(R.id.fabAgregarMascota) // Inicializar la variable miembro
        btnPerfil = findViewById(R.id.btnPerfil)

        // Inicialización de botones de navegación inferior
        btnNavMisMascotas = findViewById(R.id.btnNavMisMascotas)
        btnNavMisChats = findViewById(R.id.btnNavMisChats)

        // --- Resaltar el botón de la sección actual ---
        actualizarResaltadoNavegacion(btnNavMisMascotas) // Mascotas es la actual

        // --- Inicializar el Launcher para PerfilMiMascotaActivity ---
        perfilMascotaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("InicioDuenio", "Resultado OK de PerfilMiMascotaActivity, recargando mascotas.")
                cargarMascotas()
            } else {
                Log.d("InicioDuenio", "Resultado de PerfilMiMascotaActivity: ${result.resultCode}")
            }
        }

        listaMascotas = mutableListOf()
        adapter = MascotaAdapter(listaMascotas) { mascota ->
            val intent = Intent(this, PerfilMiMascotaActivity::class.java).apply {
                // --- AÑADIR EL ID DE LA MASCOTA ---

                putExtra("id_mascota", mascota.id)
                putExtra("nombre", mascota.nombre)
                putExtra("raza", mascota.raza)
                putExtra("edad", mascota.edad)
                putExtra("especie", mascota.especie)
                putExtra("tamanio", mascota.tamanio)
                putExtra("descripcion", mascota.descripcion)
                putExtra("fotoUrl", mascota.fotoUrl) // Asegúrate que tu clase Mascota tiene fotoUrl
            }
            // --- USAR EL LAUNCHER EN LUGAR DE startActivity ---
            perfilMascotaLauncher.launch(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Listener para el botón de agregar mascota (en la UI, no el FAB)
        btnAgregarMascota.setOnClickListener {
            // Considera usar un launcher también si RegistroMascotaDatos podría necesitar
            // devolver un resultado para refrescar la lista inmediatamente.
            startActivity(Intent(this, RegistroMascotaDatos::class.java))
            // Si RegistroMascotaDatos añade una mascota y quieres verla inmediatamente,
            // deberías usar un launcher y recargar en el resultado.
        }

        // Listener para el FloatingActionButton de agregar mascota
        fabAgregarMascota.setOnClickListener {
            startActivity(Intent(this, RegistroMascotaDatos::class.java))
            // Misma consideración que arriba sobre el launcher.
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilDuenioActivity::class.java))
        }

        btnNavMisMascotas.setOnClickListener {

            Toast.makeText(this, "Ya estás en Mis Mascotas", Toast.LENGTH_SHORT).show()
        }

        btnNavMisChats.setOnClickListener {

            Toast.makeText(this, "Ir a Mis Chats (Implementar navegación)", Toast.LENGTH_SHORT).show()

        }

        cargarMascotas()
    }

    private fun actualizarResaltadoNavegacion(botonActivo: ImageView) {
        val colorResaltado = ContextCompat.getColor(this, R.color.color_nav_icon_resaltado)
        val colorDefault = ContextCompat.getColor(this, R.color.color_nav_icon_default)

        // Lista de todos los botones de navegación para iterar fácilmente
        val botonesNavegacion = listOf(btnNavMisMascotas, btnNavMisChats)

        for (boton in botonesNavegacion) {
            if (boton == botonActivo) {
                boton.colorFilter = PorterDuffColorFilter(colorResaltado, PorterDuff.Mode.SRC_IN)
            } else {
                boton.colorFilter = PorterDuffColorFilter(colorDefault, PorterDuff.Mode.SRC_IN)
            }
        }
    }


    private fun cargarMascotas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            Log.w("InicioDuenio", "cargarMascotas: UID es nulo.")
            // Aquí podrías redirigir al login o limpiar la lista y mostrar estado vacío
            listaMascotas.clear()
            adapter.notifyDataSetChanged()
            actualizarVisibilidadVacio() // Llama a una función para manejar la UI vacía
            return
        }
        val db = FirebaseFirestore.getInstance()
        Log.d("InicioDuenio", "Cargando mascotas para UID: $uid")

        db.collection("usuarios").document(uid).collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                listaMascotas.clear()
                if (result.isEmpty) {
                    Log.d("InicioDuenio", "No se encontraron mascotas para el usuario.")
                }
                for (doc in result) {
                    val mascota = doc.toObject(Mascota::class.java)
                    // --- ASIGNAR EL ID DEL DOCUMENTO AL OBJETO MASCOTA ---
                    mascota.id = doc.id // <--- ¡MUY IMPORTANTE!
                    listaMascotas.add(mascota)
                    Log.d("InicioDuenio", "Mascota cargada: ${mascota.nombre} con ID: ${mascota.id}")
                }
                adapter.notifyDataSetChanged() // Notificar al adaptador después de modificar la lista
                actualizarVisibilidadVacio() // Actualizar la visibilidad del layout vacío y el RecyclerView/FAB
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar las mascotas: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("InicioDuenio", "Error al cargar mascotas: ", e)
                actualizarVisibilidadVacio() // Asegurar que la UI refleje el estado de error/vacío
            }
    }

    private fun actualizarVisibilidadVacio() {
        if (listaMascotas.isEmpty()) {
            layoutVacio.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            fabAgregarMascota.visibility = View.GONE // FAB también se oculta si no hay mascotas
        } else {
            layoutVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAgregarMascota.visibility = View.VISIBLE // Mostrar FAB si hay mascotas
        }
    }

    override fun onResume() {
        super.onResume()

        actualizarResaltadoNavegacion(btnNavMisMascotas)

    }
}