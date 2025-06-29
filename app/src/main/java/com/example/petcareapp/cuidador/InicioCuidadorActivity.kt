package com.example.petcareapp.cuidador

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareapp.R
import com.example.petcareapp.models.Solicitud
import com.example.petcareapp.adapters.SolicitudAdapter


class InicioCuidadorActivity : AppCompatActivity() {

    // Declaración de variables
    private lateinit var tvPantallaSeleccionada: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SolicitudAdapter
    private lateinit var listaSolicitudes: MutableList<Solicitud>
    private lateinit var layoutVacio: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_cuidador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencias a los botones (ImageViews)
        val btnPerfilCuidadorSuperior: ImageView = findViewById(R.id.btnPerfilCuidador)
        val btnNavSolicitudes: ImageView = findViewById(R.id.btnNavSolicitudes)
        val btnNavMapaCuidador: ImageView = findViewById(R.id.btnNavMapaCuidador)
        val btnNavChatsCuidador: ImageView = findViewById(R.id.btnNavChatsCuidador)

        tvPantallaSeleccionada = findViewById(R.id.tvTituloBarraCuidador)
        recyclerView = findViewById(R.id.recyclerSolicitudes)
        layoutVacio = findViewById(R.id.layoutSolicitudesVacio)

        listaSolicitudes = mutableListOf()
        adapter = SolicitudAdapter(listaSolicitudes){ solicitud ->
            val intent = Intent(this, DetalleSolicitudActivity::class.java)
            intent.putExtra("idMascota", solicitud.idMascota)
            intent.putExtra("idDueno", solicitud.idDueno)
            intent.putExtra("nombreMascota", solicitud.nombreMascota)
            intent.putExtra("especie", solicitud.especie)
            intent.putExtra("raza", solicitud.raza)
            intent.putExtra("edad", solicitud.edad)
            intent.putExtra("tamanio", solicitud.tamanio)
            intent.putExtra("descripcion", solicitud.descripcion)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cargarSolicitudes()



        // Configurar OnClickListeners para los botones
        btnPerfilCuidadorSuperior.setOnClickListener {
            val intent = Intent(this, PerfilCuidadorActivity::class.java)
            // Considera flags para una mejor gestión de la pila de actividades si es necesario
            // intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            // o
            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnNavSolicitudes.setOnClickListener {
            cargarSolicitudes()
            tvPantallaSeleccionada.text = "Solicitudes"
        }

        btnNavMapaCuidador.setOnClickListener {
            // Navegar a MapaCuidadorActivity
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            startActivity(intent)
            tvPantallaSeleccionada.text = "Mapa"
        }

        btnNavChatsCuidador.setOnClickListener {
            tvPantallaSeleccionada.text = "Pantalla de Chats"
        }
    }

    private fun cargarSolicitudes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uid)
            .collection("solicitudes")
            .get()
            .addOnSuccessListener { result ->
                listaSolicitudes.clear()
                for (doc in result) {
                    val solicitud = doc.toObject(Solicitud::class.java)
                    listaSolicitudes.add(solicitud)
                }

                if (listaSolicitudes.isEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    layoutVacio.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
            }
    }

}