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
import androidx.compose.ui.semantics.text
import androidx.core.view.isEmpty

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareapp.R
import com.example.petcareapp.models.Solicitud
import com.example.petcareapp.adapters.SolicitudAdapter
// Asegúrate de importar ListaChatsCuidadorActivity si está en el mismo paquete,
// o el paquete correcto si está en otro lugar.
// import com.example.petcareapp.cuidador.ListaChatsCuidadorActivity // Ya debería estar si está en el mismo paquete


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
            intent.putExtra("idSolicitud", solicitud.id)
            intent.putExtra("nombreDueno", solicitud.nombreDueno)
            intent.putExtra("fotoUrlDueno", solicitud.fotoUrl ?: "")

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

        // Cargar solicitudes inicialmente si esa es la pantalla por defecto
        cargarSolicitudes()
        tvPantallaSeleccionada.text = "Solicitudes" // Establecer título inicial



        // Configurar OnClickListeners para los botones
        btnPerfilCuidadorSuperior.setOnClickListener {
            val intent = Intent(this, PerfilCuidadorActivity::class.java)
            startActivity(intent)
        }

        btnNavSolicitudes.setOnClickListener {
            recyclerView.visibility = View.VISIBLE // Asegurarse que el RecyclerView de solicitudes sea visible
            // Podrías tener otros elementos UI que ocultar/mostrar si cambias de "sección"
            cargarSolicitudes()
            tvPantallaSeleccionada.text = "Solicitudes"
        }

        btnNavMapaCuidador.setOnClickListener {
            // Aquí podrías ocultar el RecyclerView de solicitudes si el mapa ocupa toda la pantalla
            // recyclerView.visibility = View.GONE
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            startActivity(intent)
            tvPantallaSeleccionada.text = "Mapa"
        }

        btnNavChatsCuidador.setOnClickListener {
            // Aquí podrías ocultar el RecyclerView de solicitudes si la pantalla de chats es una nueva actividad
            // recyclerView.visibility = View.GONE
            val intent = Intent(this, ListaChatsCuidadorActivity::class.java) // CAMBIO AQUÍ
            startActivity(intent)
        }
    }

    private fun cargarSolicitudes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Podrías añadir un indicador de carga aquí
        // progressBar.visibility = View.VISIBLE
        // layoutVacio.visibility = View.GONE
        // recyclerView.visibility = View.GONE


        db.collection("usuarios")
            .document(uid)
            .collection("solicitudes")
            .get()
            .addOnSuccessListener { result ->
                // progressBar.visibility = View.GONE
                listaSolicitudes.clear()
                for (doc in result) {
                    val solicitud = doc.toObject(Solicitud::class.java)
                    // Podrías querer asignar el ID del documento a tu objeto Solicitud si no lo haces ya
                    // solicitud.id = doc.id
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
                // progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
                layoutVacio.visibility = View.VISIBLE // Mostrar mensaje de vacío o error
                recyclerView.visibility = View.GONE
            }
    }

}