package com.example.petcareapp.duenio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.core.view.size

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareapp.ChatActivity
import com.example.petcareapp.R
// MODIFICADO: Importar el nuevo Adapter (asumiendo que lo llamarás VistaPreviaChatAdapter)
import com.example.petcareapp.adapters.VistaPreviaChatAdapter // CAMBIAR SI EL NOMBRE DEL ADAPTER ES OTRO
// MODIFICADO: Importar el nuevo modelo de datos
import com.example.petcareapp.models.VistaPreviaChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.gms.tasks.Tasks
// Eliminadas importaciones no utilizadas de tu código original como:
// import androidx.core.view.isEmpty
// import androidx.core.view.size
// import androidx.fragment.app.add
// import androidx.glance.visibility
// import kotlin.collections.addAll
// import kotlin.collections.sortByDescending
// import kotlin.text.clear
// Firebase ktx.toObject no es estrictamente necesario si usas doc.toObject(Clase::class.java)

class ListaChatsActivity : AppCompatActivity() {

    private lateinit var toolbar: LinearLayout
    private lateinit var recyclerViewListaChats: RecyclerView
    private lateinit var textViewNoChats: TextView
    private lateinit var barraInferior: LinearLayout
    private lateinit var btnNavMisMascotas: ImageView
    private lateinit var vistaPreviaChatAdapter: VistaPreviaChatAdapter // CAMBIAR SI EL NOMBRE DEL ADAPTER ES OTRO
    private val listaDeChats = mutableListOf<VistaPreviaChat>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista_chats)

        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mainListaChatsLayout)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.barraSuperior)
        recyclerViewListaChats = findViewById(R.id.recyclerViewListaChats)
        textViewNoChats = findViewById(R.id.textViewNoChats)
        barraInferior = findViewById(R.id.barraInferior)
        btnNavMisMascotas = findViewById(R.id.btnNavMisMascotas)

        btnNavMisMascotas.setOnClickListener {
            intent = Intent(this, InicioDuenioActivity::class.java)
            startActivity(intent)
        }
        setupRecyclerView()

        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            Log.w("ListaChatsActivity", "Usuario no autenticado. Finalizando actividad.")
            finish()
            return
        }

        cargarChats()
    }

    private fun setupRecyclerView() {
        // MODIFICADO: Usar el nuevo adapter y el tipo de dato correcto en la lambda
        vistaPreviaChatAdapter = VistaPreviaChatAdapter(listaDeChats) { vistaPreviaChat ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("idChat", vistaPreviaChat.idChat)
                putExtra("idReceptor", vistaPreviaChat.idOtroUsuario)
                putExtra("nombreReceptor", vistaPreviaChat.nombreOtroUsuario)
                putExtra("fotoReceptorUrl", vistaPreviaChat.fotoUrlOtroUsuario)
            }
            startActivity(intent)
        }
        recyclerViewListaChats.apply {
            layoutManager = LinearLayoutManager(this@ListaChatsActivity)
            // MODIFICADO: Asignar el nuevo adapter
            adapter = vistaPreviaChatAdapter
        }
    }

    private fun cargarChats() {
        val currentUserId = currentUser!!.uid

        db.collection("chats")
            .whereArrayContains("participantes", currentUserId)
            .orderBy("ultimoMensajeTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ListaChatsActivity", "Error escuchando chats.", e)
                    Toast.makeText(this, "Error al cargar chats: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    // MODIFICADO: Tipo de la lista temporal
                    val chatsCargadosTemporalmente = mutableListOf<VistaPreviaChat>()
                    val tareasDeUsuario = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                    for (doc in snapshots.documents) {
                        val chatId = doc.id
                        val participantes = doc.get("participantes") as? List<String>
                        val ultimoMensajeTexto = doc.getString("ultimoMensajeTexto") ?: ""
                        val ultimoMensajeTimestamp = doc.getTimestamp("ultimoMensajeTimestamp")

                        if (participantes != null) {
                            val idOtroUsuario = participantes.firstOrNull { it != currentUserId }
                            if (idOtroUsuario != null) {
                                val tareaInfoUsuario = db.collection("usuarios").document(idOtroUsuario).get()
                                    .addOnSuccessListener { userDoc ->
                                        if (userDoc != null && userDoc.exists()) {
                                            val nombreOtro = userDoc.getString("nombre") ?: "Desconocido"
                                            val fotoUrlOtro = userDoc.getString("fotoUrl")

                                            // MODIFICADO: Crear instancia del nuevo modelo
                                            val vistaPrevia = VistaPreviaChat(
                                                idChat = chatId,
                                                idOtroUsuario = idOtroUsuario,
                                                nombreOtroUsuario = nombreOtro,
                                                fotoUrlOtroUsuario = fotoUrlOtro,
                                                ultimoMensaje = ultimoMensajeTexto,
                                                timestampUltimoMensaje = ultimoMensajeTimestamp?.toDate()
                                            )
                                            synchronized(chatsCargadosTemporalmente) {
                                                chatsCargadosTemporalmente.add(vistaPrevia)
                                            }
                                        } else {
                                            Log.w("ListaChatsActivity", "Documento del usuario $idOtroUsuario no encontrado.")
                                        }
                                    }.addOnFailureListener { errorUsuario ->
                                        Log.e("ListaChatsActivity", "Error al obtener datos del usuario $idOtroUsuario", errorUsuario)
                                    }
                                tareasDeUsuario.add(tareaInfoUsuario)
                            } else {
                                Log.w("ListaChatsActivity", "Chat $chatId no tiene un 'otro participante' válido.")
                            }
                        }
                    }

                    Tasks.whenAllComplete(tareasDeUsuario).addOnCompleteListener {
                        synchronized(chatsCargadosTemporalmente) {
                            chatsCargadosTemporalmente.sortByDescending { it.timestampUltimoMensaje }
                            listaDeChats.clear()
                            listaDeChats.addAll(chatsCargadosTemporalmente)
                        }
                        // MODIFICADO: Notificar al nuevo adapter
                        vistaPreviaChatAdapter.notifyDataSetChanged()
                        actualizarVisibilidadNoChats()
                        Log.d("ListaChatsActivity", "Chats cargados y RecyclerView actualizado. Total: ${listaDeChats.size}")
                    }

                } else {
                    Log.d("ListaChatsActivity", "No hay chats o la colección está vacía para el usuario $currentUserId.")
                    listaDeChats.clear()
                    // MODIFICADO: Notificar al nuevo adapter
                    vistaPreviaChatAdapter.notifyDataSetChanged()
                    actualizarVisibilidadNoChats()
                }
            }
    }

    private fun actualizarVisibilidadNoChats() {
        if (listaDeChats.isEmpty()) {
            textViewNoChats.visibility = View.VISIBLE
            recyclerViewListaChats.visibility = View.GONE
        } else {
            textViewNoChats.visibility = View.GONE
            recyclerViewListaChats.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}