package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty

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
    private lateinit var btnPerfil: ImageView // Variable para el botón de perfil (ImageView)

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
        val fabAgregarMascota: FloatingActionButton = findViewById(R.id.fabAgregarMascota)
        btnPerfil = findViewById(R.id.btnPerfil) // Inicializar el ImageView del perfil

        listaMascotas = mutableListOf()
        adapter = MascotaAdapter(listaMascotas) { mascota ->
            val intent = Intent(this, PerfilMiMascotaActivity::class.java).apply {
                putExtra("nombre", mascota.nombre)
                putExtra("raza", mascota.raza)
                putExtra("edad", mascota.edad)
                putExtra("especie", mascota.especie)
                putExtra("tamanio", mascota.tamanio)
                putExtra("descripcion", mascota.descripcion)
            }
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAgregarMascota.setOnClickListener {
            startActivity(Intent(this, RegistroMascotaDatos::class.java))
        }

        fabAgregarMascota.setOnClickListener {
            startActivity(Intent(this, RegistroMascotaDatos::class.java))
        }

        // --- Configurar OnClickListener para btnPerfil ---
        btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilDuenioActivity::class.java) // Navegar a PerfilDuenioActivity
            startActivity(intent)
        }
        // --- Fin de la configuración de btnPerfil ---

        cargarMascotas()
    }

    private fun cargarMascotas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            // Considera manejar el caso donde el UID es nulo,
            // por ejemplo, redirigiendo al login o mostrando un mensaje.
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            // Podrías llamar a finish() o iniciar LoginActivity aquí si es apropiado.
            return
        }
        val db = FirebaseFirestore.getInstance()
        // No es necesario volver a obtener fabAgregarMascota aquí, ya es una variable de clase o local en onCreate
        // val fabAgregarMascota: FloatingActionButton = findViewById(R.id.fabAgregarMascota)

        db.collection("usuarios").document(uid).collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                listaMascotas.clear()
                for (doc in result) {
                    val mascota = doc.toObject(Mascota::class.java)
                    // Es buena práctica verificar si la mascota no es null antes de añadirla,
                    // aunque toObject debería manejarlo si la clase Mascota tiene un constructor sin argumentos.
                    if (mascota != null) {
                        listaMascotas.add(mascota)
                    }
                }

                val fab: FloatingActionButton = findViewById(R.id.fabAgregarMascota) // Obtener referencia aquí o hacerla variable de clase
                if (listaMascotas.isEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    fab.visibility = View.GONE // Usar la referencia correcta
                } else {
                    layoutVacio.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    fab.visibility = View.VISIBLE // Usar la referencia correcta
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las mascotas", Toast.LENGTH_SHORT).show()
            }
    }
}