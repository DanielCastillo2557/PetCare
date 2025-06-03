package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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


        listaMascotas = mutableListOf()
        adapter = MascotaAdapter(listaMascotas){ mascota ->
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

        cargarMascotas()

    }

    private fun cargarMascotas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val fabAgregarMascota: FloatingActionButton = findViewById(R.id.fabAgregarMascota)

        db.collection("usuarios").document(uid).collection("mascotas")
            .get()
            .addOnSuccessListener { result ->
                listaMascotas.clear()
                for (doc in result) {
                    val mascota = doc.toObject(Mascota::class.java)
                    listaMascotas.add(mascota)
                }

                if (listaMascotas.isEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    fabAgregarMascota.visibility = View.GONE
                } else {
                    layoutVacio.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                    fabAgregarMascota.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar las mascotas", Toast.LENGTH_SHORT).show()
            }
    }
}