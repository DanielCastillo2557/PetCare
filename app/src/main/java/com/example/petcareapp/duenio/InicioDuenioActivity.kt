package com.example.petcareapp.duenio

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

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareapp.R
// MODIFICADO: Importar ListaChatsActivity
import com.example.petcareapp.duenio.ListaChatsActivity // Asegúrate que la ruta sea correcta
import com.example.petcareapp.adapters.MascotaAdapter
import com.example.petcareapp.models.Mascota
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
        fabAgregarMascota = findViewById(R.id.fabAgregarMascota)
        btnPerfil = findViewById(R.id.btnPerfil)

        btnNavMisMascotas = findViewById(R.id.btnNavMisMascotas)
        btnNavMisChats = findViewById(R.id.btnNavMisChats)

        // Resaltar inicialmente Mis Mascotas
        actualizarResaltadoNavegacion(btnNavMisMascotas)

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
                putExtra("id_mascota", mascota.id)
                putExtra("nombre", mascota.nombre)
                putExtra("raza", mascota.raza)
                putExtra("edad", mascota.edad)
                putExtra("especie", mascota.especie)
                putExtra("tamanio", mascota.tamanio)
                putExtra("descripcion", mascota.descripcion)
                putExtra("fotoUrl", mascota.fotoUrl)
            }
            perfilMascotaLauncher.launch(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAgregarMascota.setOnClickListener {
            startActivity(Intent(this, RegistroMascotaDatos::class.java))
        }

        fabAgregarMascota.setOnClickListener {
            startActivity(Intent(this, RegistroMascotaDatos::class.java))
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilDuenioActivity::class.java))
        }

        btnNavMisMascotas.setOnClickListener {
            // Ya estamos aquí, solo actualizar resaltado por si acaso
            actualizarResaltadoNavegacion(btnNavMisMascotas)
            // Opcionalmente, mostrar un Toast si ya está en esta sección
            // Toast.makeText(this, "Ya estás en Mis Mascotas", Toast.LENGTH_SHORT).show()
        }

        btnNavMisChats.setOnClickListener {
            // MODIFICADO: Actualizar resaltado y navegar a ListaChatsActivity
            actualizarResaltadoNavegacion(btnNavMisChats)
            val intent = Intent(this, ListaChatsActivity::class.java)
            startActivity(intent)
            // Opcional: si quieres que esta actividad se cierre al ir a los chats, puedes añadir finish()
            // finish()
        }

        cargarMascotas()
    }

    private fun actualizarResaltadoNavegacion(botonActivo: ImageView) {
        val colorResaltado = ContextCompat.getColor(this, R.color.color_nav_icon_resaltado)
        val colorDefault = ContextCompat.getColor(this, R.color.color_nav_icon_default)
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
            listaMascotas.clear()
            adapter.notifyDataSetChanged()
            actualizarVisibilidadVacio()
            return
        }
        val db = FirebaseFirestore.getInstance()
        Log.d("InicioDuenio", "Cargando mascotas para UID: $uid")

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                listaMascotas.clear()
                if (result.isEmpty) {
                    Log.d("InicioDuenio", "No se encontraron mascotas para el usuario.")
                }
                for (doc in result) {
                    val mascota = doc.toObject(Mascota::class.java)
                    mascota.id = doc.id
                    listaMascotas.add(mascota)
                    Log.d("InicioDuenio", "Mascota cargada: ${mascota.nombre} con ID: ${mascota.id}")
                }
                adapter.notifyDataSetChanged()
                actualizarVisibilidadVacio()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar las mascotas: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("InicioDuenio", "Error al cargar mascotas: ", e)
                actualizarVisibilidadVacio()
            }
    }

    private fun actualizarVisibilidadVacio() {
        if (listaMascotas.isEmpty()) {
            layoutVacio.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            fabAgregarMascota.visibility = View.GONE
        } else {
            layoutVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAgregarMascota.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        actualizarResaltadoNavegacion(btnNavMisMascotas)
    }
}