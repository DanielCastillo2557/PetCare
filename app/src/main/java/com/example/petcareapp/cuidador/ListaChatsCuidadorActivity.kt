package com.example.petcareapp.cuidador

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
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.core.view.size

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareapp.ChatActivity
import com.example.petcareapp.R
import com.example.petcareapp.adapters.VistaPreviaChatAdapter
import com.example.petcareapp.models.VistaPreviaChat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class ListaChatsCuidadorActivity : AppCompatActivity() {

    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var vistaPreviaChatAdapter: VistaPreviaChatAdapter
    private val listaVistasPreviasChat: MutableList<VistaPreviaChat> = mutableListOf()

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var layoutChatsVacio: LinearLayout
    private lateinit var tvTituloBarra: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista_chats_cuidador)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencias a los botones (ImageViews)
        val btnPerfilCuidadorSuperior: ImageView = findViewById(R.id.btnPerfilCuidador)
        val btnNavSolicitudes: ImageView = findViewById(R.id.btnNavInicioCuidador)
        val btnNavMapaCuidador: ImageView = findViewById(R.id.btnNavMapaVets)

        // Configurar OnClickListeners para los botones
        btnPerfilCuidadorSuperior.setOnClickListener {
            val intent = Intent(this, PerfilCuidadorActivity::class.java)
            startActivity(intent)
        }

        btnNavSolicitudes.setOnClickListener {
            val intent = Intent(this, InicioCuidadorActivity::class.java)
            startActivity(intent)
        }

        btnNavMapaCuidador.setOnClickListener {
            val intent = Intent(this, MapaCuidadorActivity::class.java)
            startActivity(intent)
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerViewChats = findViewById(R.id.recyclerViewChats)
        layoutChatsVacio = findViewById(R.id.layoutChatsVacio)

        setupRecyclerView()
        cargarConversaciones()
    }

    private fun setupRecyclerView() {
        vistaPreviaChatAdapter = VistaPreviaChatAdapter(listaVistasPreviasChat) { vistaPreviaChat ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("idChat", vistaPreviaChat.idChat)
                putExtra("idReceptor", vistaPreviaChat.idOtroUsuario)
                putExtra("nombreReceptor", vistaPreviaChat.nombreOtroUsuario)
                putExtra("fotoReceptorUrl", vistaPreviaChat.fotoUrlOtroUsuario)
            }
            startActivity(intent)
        }
        recyclerViewChats.layoutManager = LinearLayoutManager(this)
        recyclerViewChats.adapter = vistaPreviaChatAdapter
    }

    private fun cargarConversaciones() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            actualizarVisibilidadChats()
            return
        }

        Log.d("ListaChatsCuidador", "Cargando conversaciones para el cuidador UID: $currentUserId")

        db.collection("chats")
            .whereArrayContains("participantes", currentUserId)
            .orderBy("ultimoMensajeTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ListaChatsCuidador", "Error al escuchar cambios en los chats.", e)
                    Toast.makeText(this, "Error al cargar conversaciones: ${e.message}", Toast.LENGTH_SHORT).show()
                    actualizarVisibilidadChats()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.w("ListaChatsCuidador", "Snapshots es null.")
                    actualizarVisibilidadChats()
                    return@addSnapshotListener
                }

                Log.d("ListaChatsCuidador", "Chats recibidos: ${snapshots.size()} documentos.")

                val chatsCargadosTemporalmente = mutableListOf<VistaPreviaChat>()
                val tareasDeUsuario = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                if (snapshots.isEmpty) {
                    listaVistasPreviasChat.clear()
                    vistaPreviaChatAdapter.notifyDataSetChanged()
                    actualizarVisibilidadChats()
                    Log.d("ListaChatsCuidador", "No hay chats para el usuario $currentUserId.")
                    return@addSnapshotListener
                }

                for (doc in snapshots.documents) {
                    val chatId = doc.id
                    val participantes = doc.get("participantes") as? List<String>
                    val ultimoMensajeTexto = doc.getString("ultimoMensajeTexto") ?: "No hay mensajes"
                    val ultimoMensajeTimestampFirestore = doc.getTimestamp("ultimoMensajeTimestamp")

                    if (participantes != null) {
                        val idOtroUsuario = participantes.firstOrNull { it != currentUserId }
                        if (idOtroUsuario != null) {
                            val tareaInfoUsuario = db.collection("usuarios").document(idOtroUsuario).get()
                                .addOnSuccessListener { userDoc ->
                                    if (userDoc != null && userDoc.exists()) {
                                        val nombreOtro = userDoc.getString("nombre") ?: "Usuario Desconocido"
                                        val fotoUrlOtro = userDoc.getString("foto_url") // Campo corregido a "foto_url"

                                        val vistaPrevia = VistaPreviaChat(
                                            idChat = chatId,
                                            idOtroUsuario = idOtroUsuario,
                                            nombreOtroUsuario = nombreOtro,
                                            fotoUrlOtroUsuario = fotoUrlOtro ?: "",
                                            ultimoMensaje = ultimoMensajeTexto,
                                            timestampUltimoMensaje = ultimoMensajeTimestampFirestore?.toDate()
                                        )
                                        synchronized(chatsCargadosTemporalmente) {
                                            val index = chatsCargadosTemporalmente.indexOfFirst { it.idChat == vistaPrevia.idChat }
                                            if (index != -1) {
                                                chatsCargadosTemporalmente[index] = vistaPrevia // Actualizar
                                            } else {
                                                chatsCargadosTemporalmente.add(vistaPrevia) // Añadir nuevo
                                            }
                                        }
                                    } else {
                                        Log.w("ListaChatsCuidador", "Documento del usuario $idOtroUsuario no encontrado.")
                                        val vistaPrevia = VistaPreviaChat(
                                            idChat = chatId, idOtroUsuario = idOtroUsuario,
                                            nombreOtroUsuario = "Usuario (No encontrado)", fotoUrlOtroUsuario = null,
                                            ultimoMensaje = ultimoMensajeTexto,
                                            timestampUltimoMensaje = ultimoMensajeTimestampFirestore?.toDate()
                                        )
                                        synchronized(chatsCargadosTemporalmente) {
                                            val index = chatsCargadosTemporalmente.indexOfFirst { it.idChat == vistaPrevia.idChat }
                                            if (index != -1) {
                                                chatsCargadosTemporalmente[index] = vistaPrevia
                                            } else {
                                                chatsCargadosTemporalmente.add(vistaPrevia)
                                            }
                                        }
                                    }
                                }.addOnFailureListener { errorUsuario ->
                                    Log.e("ListaChatsCuidador", "Error al obtener datos del usuario $idOtroUsuario", errorUsuario)
                                    val vistaPrevia = VistaPreviaChat(
                                        idChat = chatId, idOtroUsuario = idOtroUsuario,
                                        nombreOtroUsuario = "Usuario (Error)", fotoUrlOtroUsuario = null,
                                        ultimoMensaje = ultimoMensajeTexto,
                                        timestampUltimoMensaje = ultimoMensajeTimestampFirestore?.toDate()
                                    )
                                    synchronized(chatsCargadosTemporalmente) {
                                        val index = chatsCargadosTemporalmente.indexOfFirst { it.idChat == vistaPrevia.idChat }
                                        if (index != -1) {
                                            chatsCargadosTemporalmente[index] = vistaPrevia
                                        } else {
                                            chatsCargadosTemporalmente.add(vistaPrevia)
                                        }
                                    }
                                }
                            tareasDeUsuario.add(tareaInfoUsuario)
                        } else {
                            Log.w("ListaChatsCuidador", "Chat $chatId no tiene un 'otro participante' válido.")
                        }
                    } else {
                        Log.w("ListaChatsCuidador", "Chat $chatId no tiene 'participantes'.")
                    }
                }

                Tasks.whenAllComplete(tareasDeUsuario).addOnCompleteListener {
                    synchronized(chatsCargadosTemporalmente) {
                        // Ordenar DESPUÉS de que todos los datos (incluyendo info de usuario) se hayan recopilado
                        chatsCargadosTemporalmente.sortByDescending { it.timestampUltimoMensaje }
                        listaVistasPreviasChat.clear()
                        listaVistasPreviasChat.addAll(chatsCargadosTemporalmente)
                    }
                    vistaPreviaChatAdapter.notifyDataSetChanged()
                    actualizarVisibilidadChats()
                    Log.d("ListaChatsCuidador", "Conversaciones procesadas y RecyclerView actualizado. Total: ${listaVistasPreviasChat.size}")
                }
            }
    }

    private fun actualizarVisibilidadChats() {
        if (listaVistasPreviasChat.isEmpty()) {
            recyclerViewChats.visibility = View.GONE
            layoutChatsVacio.visibility = View.VISIBLE
        } else {
            recyclerViewChats.visibility = View.VISIBLE
            layoutChatsVacio.visibility = View.GONE
        }
    }
}