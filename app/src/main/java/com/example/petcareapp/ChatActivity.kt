package com.example.petcareapp

import android.os.Bundle
import android.util.Log
import android.widget.EditText // Importar EditText
import android.widget.ImageButton // Importar ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Si usas la Toolbar del XML
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager // Importar LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // Importar RecyclerView
import com.example.petcareapp.adapters.MensajeAdapter // IMPORTANTE: Importa tu MensajeAdapter
import com.example.petcareapp.models.ChatMensaje // IMPORTANTE: Importa tu ChatMensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference // Importar CollectionReference
import com.google.firebase.firestore.FirebaseFirestore // Importar FirebaseFirestore
import com.google.firebase.firestore.Query // Importar Query
import com.google.firebase.firestore.ktx.toObject // Importar para convertir a objeto

class ChatActivity : AppCompatActivity() {

    // --- Variables para la lógica del chat ---
    private lateinit var chatId: String
    private lateinit var receptorId: String
    private lateinit var receptorNombre: String
    private var receptorFotoUrl: String? = null

    private var currentUser: FirebaseUser? = null
    private lateinit var currentUserId: String

    // --- Vistas (AHORA LAS INICIALIZAREMOS) ---
    private lateinit var toolbarChat: Toolbar // Descomenta si usas Toolbar
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSendMessage: ImageButton

    // --- Referencias a Firestore ---
    private val db = FirebaseFirestore.getInstance()
    // private lateinit var chatDocRef: DocumentReference // Referencia al documento del chat específico
    private lateinit var mensajesRef: CollectionReference // Referencia a la subcolección de mensajes

    // --- Adaptador para el RecyclerView ---
    private lateinit var mensajeAdapter: MensajeAdapter
    private var listaMensajes: MutableList<ChatMensaje> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mainChatLayout)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        currentUserId = currentUser!!.uid

        // --- Recoger datos del Intent ---
        chatId = intent.getStringExtra("idChat") ?: ""
        receptorId = intent.getStringExtra("idReceptor") ?: ""
        receptorNombre = intent.getStringExtra("nombreReceptor") ?: "Chat"
        receptorFotoUrl = intent.getStringExtra("fotoReceptorUrl")

        if (chatId.isBlank()) { // Solo necesitamos chatId para la subcolección de mensajes
            Toast.makeText(this, "Error: ID del chat no proporcionado.", Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "Falta idChat.")
            finish()
            return
        }
        if (receptorId.isBlank()) { // receptorId es útil, pero el chat funciona con chatId
            Log.w("ChatActivity", "receptorId no fue proporcionado, algunas funciones pueden ser limitadas.")
            // Podrías decidir si esto es un error fatal o no.
            // Para la lógica de envío de mensajes, es importante.
        }


        // --- Inicializar Vistas ---
        initViews()

        // --- Configurar Toolbar (si la tienes) ---
        setupToolbar() // Llama a la función para configurar la Toolbar

        // --- Configurar RecyclerView y Adapter ---
        setupRecyclerView()

        // --- Configurar Listener para enviar mensajes ---
        setupSendButton()

        // --- Cargar mensajes desde Firestore ---
        cargarMensajes()

        Log.d("ChatActivity", "Chat iniciado. ChatID: $chatId, Receptor: $receptorNombre ($receptorId), Usuario Actual: $currentUserId")
    }

    private fun initViews() {
        // Descomenta la Toolbar si la tienes en tu XML y quieres usarla
        toolbarChat = findViewById(R.id.toolbarChat) // Asumiendo que el ID en tu XML es toolbarChat

        // Corregir los IDs aquí para que coincidan con activity_chat.xml:
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages) // <--- CORREGIDO
        editTextMessage = findViewById(R.id.editTextMessage)          // <--- CORREGIDO

        buttonSendMessage = findViewById(R.id.buttonSendMessage) // Este ID parece coincidir
    }

    private fun setupToolbar() {
        // Descomenta y ajusta si tienes una Toolbar en tu activity_chat.xml con el ID R.id.toolbarChat
        /*
        toolbarChat = findViewById(R.id.toolbarChat) // Asegúrate de que este ID exista en activity_chat.xml
        setSupportActionBar(toolbarChat)
        supportActionBar?.title = receptorNombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        */
        // Si no usas una Toolbar definida en el XML pero quieres un título, puedes hacerlo así
        // (esto usará la ActionBar por defecto si tu tema la provee)
        supportActionBar?.title = receptorNombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Muestra el botón de atrás
        supportActionBar?.setDisplayShowHomeEnabled(true)


    }


    private fun setupRecyclerView() {
        mensajeAdapter = MensajeAdapter(listaMensajes, currentUserId)
        recyclerViewMessages.apply {
            adapter = mensajeAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // Los nuevos mensajes aparecen abajo y se scrollea hacia ellos
                // reverseLayout = true // Descomenta si quieres que la lista se llene de abajo hacia arriba
            }
        }
    }

    private fun setupSendButton() {
        buttonSendMessage.setOnClickListener {
            val textoMensaje = editTextMessage.text.toString().trim()
            if (textoMensaje.isNotEmpty()) {
                enviarMensaje(textoMensaje)
            } else {
                Toast.makeText(this, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarMensaje(texto: String) {
        if (chatId.isBlank()) {
            Log.e("ChatActivity", "No se puede enviar mensaje, chatId está vacío.")
            Toast.makeText(this, "Error al enviar mensaje (sin ID de chat)", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el objeto mensaje
        val nuevoMensaje = ChatMensaje(
            // idMensaje se generará automáticamente por Firestore si lo dejamos vacío
            idEmisor = currentUserId,
            idReceptor = receptorId, // Importante para saber quién es el destinatario directo
            texto = texto,
            timestamp = null // Firestore lo establecerá con @ServerTimestamp
        )

        // Referencia a la subcolección "mensajes" dentro del documento del chat específico
        mensajesRef = db.collection("chats").document(chatId).collection("mensajes")

        mensajesRef.add(nuevoMensaje)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Mensaje enviado con ID: ${it.id}")
                editTextMessage.text.clear() // Limpiar el campo de texto
                // Opcional: Hacer scroll al último mensaje (RecyclerView podría hacerlo automáticamente con stackFromEnd)
                // recyclerViewMessages.smoothScrollToPosition(mensajeAdapter.itemCount - 1)

                // También actualiza el documento principal del chat con el último mensaje
                actualizarUltimoMensajeEnChat(texto)

            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Error al enviar mensaje", e)
                Toast.makeText(this, "Error al enviar mensaje: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun actualizarUltimoMensajeEnChat(ultimoTexto: String) {
        if (chatId.isBlank()) return

        val chatDocRef = db.collection("chats").document(chatId)
        val actualizacionChat = hashMapOf(
            "ultimoMensajeTexto" to ultimoTexto,
            "ultimoMensajeTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(), // Usa el timestamp del servidor
            "ultimoMensajeEnviadoPor" to currentUserId // Opcional: para saber quién envió el último mensaje
        )

        chatDocRef.update(actualizacionChat as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Documento del chat actualizado con el último mensaje.")
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Error al actualizar el documento del chat.", e)
            }
    }

    private fun cargarMensajes() {
        if (chatId.isBlank()) {
            Log.e("ChatActivity", "No se pueden cargar mensajes, chatId está vacío.")
            return
        }

        // Referencia a la subcolección "mensajes"
        mensajesRef = db.collection("chats").document(chatId).collection("mensajes")

        mensajesRef.orderBy("timestamp", Query.Direction.ASCENDING) // Ordenar por timestamp
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatActivity", "Error escuchando mensajes.", e)
                    Toast.makeText(this, "Error al cargar mensajes: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    Log.d("ChatActivity", "Cantidad de mensajes recibidos: ${snapshots.size()}")
                    val nuevosMensajes = mutableListOf<ChatMensaje>()
                    for (documentChange in snapshots.documentChanges) {
                        // Puedes manejar diferentes tipos de cambios (ADDED, MODIFIED, REMOVED)
                        // Por simplicidad, aquí recargamos todos los mensajes visibles o añadimos nuevos
                        // Una lógica más avanzada usaría documentChange.type
                        val mensaje = documentChange.document.toObject<ChatMensaje>()
                        // Asignar el ID del documento al objeto mensaje si no lo hiciste antes
                        val mensajeConId = mensaje.copy(idMensaje = documentChange.document.id)
                        Log.d("ChatActivity", "Mensaje recibido: ${mensaje.texto} de ${mensaje.idEmisor}")
                        nuevosMensajes.add(mensajeConId)
                    }

                    // Para simplificar y evitar duplicados si la lógica de carga es sencilla:
                    // Limpiamos la lista actual y añadimos todos los mensajes de la snapshot.
                    // Una implementación más robusta manejaría los documentChanges.type (ADDED, MODIFIED, REMOVED)
                    // para actualizaciones más granulares del adapter.
                    listaMensajes.clear()
                    for (document in snapshots.documents) {
                        document.toObject<ChatMensaje>()?.let { mensaje ->
                            val mensajeConId = mensaje.copy(idMensaje = document.id)
                            listaMensajes.add(mensajeConId)
                        }
                    }
                    mensajeAdapter.notifyDataSetChanged() // Notificar al adapter de los cambios
                    // Hacer scroll al último mensaje
                    recyclerViewMessages.smoothScrollToPosition(mensajeAdapter.itemCount - 1)

                } else {
                    Log.d("ChatActivity", "No hay mensajes o la colección está vacía.")
                    // Puedes manejar el caso de que no haya mensajes (ej. mostrar un placeholder)
                    listaMensajes.clear()
                    mensajeAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        // onBackPressed() // Deprecated
        onBackPressedDispatcher.onBackPressed() // Forma actual
        return true
    }
}