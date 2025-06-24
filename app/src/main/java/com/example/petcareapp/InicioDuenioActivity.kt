package com.example.petcareapp

import android.app.Activity // Para Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher // Importante
import androidx.activity.result.contract.ActivityResultContracts // Importante
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty

// import androidx.core.view.isEmpty // No parece usarse, puedes quitarlo si no hay error

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
    private lateinit var fabAgregarMascota: FloatingActionButton // Hacerla variable miembro para fácil acceso

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

        // --- Inicializar el Launcher para PerfilMiMascotaActivity ---
        perfilMascotaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Si PerfilMiMascotaActivity indica que hubo cambios (porque se editó una mascota),
                // recargamos la lista para reflejar esos cambios.
                Log.d("InicioDuenio", "Resultado OK de PerfilMiMascotaActivity, recargando mascotas.")
                cargarMascotas()
                // Opcional: mostrar un Toast
                // Toast.makeText(this, "Lista de mascotas actualizada.", Toast.LENGTH_SHORT).show()
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

        cargarMascotas() // Carga inicial de mascotas
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

    // Función auxiliar para manejar la visibilidad del layout vacío, RecyclerView y FAB
    private fun actualizarVisibilidadVacio() {
        if (listaMascotas.isEmpty()) {
            layoutVacio.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            fabAgregarMascota.visibility = View.GONE

        } else {
            layoutVacio.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAgregarMascota.visibility = View.VISIBLE // Mostrar FAB si hay mascotas
        }
    }

    // Es buena práctica recargar las mascotas en onResume si vienes de RegistroMascotaDatos
    // y no usaste un launcher para ello, para asegurar que la lista esté actualizada.
    override fun onResume() {
        super.onResume()

    }
}