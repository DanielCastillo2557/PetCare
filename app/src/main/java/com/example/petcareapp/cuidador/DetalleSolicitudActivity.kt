package com.example.petcareapp.cuidador

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
// import android.widget.ProgressBar // Descomenta si usas ProgressBar y lo tienes en tu XML
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// El import de androidx.compose.ui.semantics.text no parece usarse aquí, puedes quitarlo si no es necesario.
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.bumptech.glide.Glide
import com.example.petcareapp.R
// import com.example.petcareapp.chat.ChatActivity // Descomenta cuando tengas tu ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DetalleSolicitudActivity : AppCompatActivity() {
    private lateinit var imageMascota: ImageView
    private lateinit var textoNombreMascota: TextView // Renombrado para claridad
    private lateinit var textoEspecieMascota: TextView // Renombrado para claridad
    private lateinit var textoRazaMascota: TextView    // Renombrado para claridad
    private lateinit var textoEdadMascota: TextView    // Renombrado para claridad
    private lateinit var textoTamanoMascota: TextView  // Renombrado para claridad
    private lateinit var textoDescripcionMascota: TextView // Renombrado para claridad
    private lateinit var btnAceptar: Button
    // private lateinit var progressBar: ProgressBar

    // Datos recibidos del Intent
    private lateinit var receivedIdMascota: String
    private lateinit var receivedIdDueno: String
    private lateinit var receivedIdSolicitud: String // ID del documento de la solicitud
    private lateinit var receivedNombreDueno: String
    private var receivedFotoUrlDueno: String? = null

    // Datos de la mascota recibidos del Intent
    private lateinit var receivedNombreMascota: String
    private lateinit var receivedEspecieMascota: String
    private lateinit var receivedRazaMascota: String
    private lateinit var receivedEdadMascota: String // Asumimos que viene como String
    private lateinit var receivedTamanioMascota: String
    private lateinit var receivedDescripcionMascota: String
    private var receivedFotoMascotaUrl: String? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_solicitud) // Confirma que es el layout correcto
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        if (!recogerDatosDelIntent()) {
            Toast.makeText(this, "Error: Datos de la solicitud incompletos para mostrar detalles.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        inicializarVistas()
        popularVistasConDatos()

        btnAceptar.setOnClickListener {
            aceptarSolicitudYCrearChat()
        }

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Forma moderna de manejar el botón "atrás"
        }
    }

    private fun recogerDatosDelIntent(): Boolean {
        receivedIdMascota = intent.getStringExtra("idMascota") ?: ""
        receivedIdDueno = intent.getStringExtra("idDueno") ?: ""
        receivedIdSolicitud = intent.getStringExtra("idSolicitud") ?: "" // CRUCIAL
        receivedNombreDueno = intent.getStringExtra("nombreDueno") ?: "Dueño Desconocido" // CRUCIAL
        receivedFotoUrlDueno = intent.getStringExtra("fotoUrlDueno") // Opcional, puede ser null

        // Datos de la mascota
        receivedNombreMascota = intent.getStringExtra("nombreMascota") ?: "Nombre no disponible"
        receivedEspecieMascota = intent.getStringExtra("especie") ?: "Especie no disponible"
        receivedRazaMascota = intent.getStringExtra("raza") ?: "Raza no disponible"
        receivedEdadMascota = intent.getStringExtra("edad") ?: "Edad no disponible" // Viene como String
        receivedTamanioMascota = intent.getStringExtra("tamanio") ?: "Tamaño no disponible"
        receivedDescripcionMascota = intent.getStringExtra("descripcion") ?: "Sin descripción"
        receivedFotoMascotaUrl = intent.getStringExtra("fotoUrl")

        // Validar que los IDs esenciales estén presentes
        return !(receivedIdMascota.isBlank() || receivedIdDueno.isBlank() || receivedIdSolicitud.isBlank())
    }

    private fun inicializarVistas() {
        imageMascota = findViewById(R.id.imagenMascota)
        textoNombreMascota = findViewById(R.id.txtNombreMascota)
        textoEspecieMascota = findViewById(R.id.txtEspecieMascota)
        textoRazaMascota = findViewById(R.id.txtRazaMascota)
        textoEdadMascota = findViewById(R.id.txtEdadMascota)
        textoTamanoMascota = findViewById(R.id.txtTamanioMascota)
        textoDescripcionMascota = findViewById(R.id.txtDescripcionMascota)
        btnAceptar = findViewById(R.id.btnAceptarSolicitud)
        // progressBar = findViewById(R.id.progressBar) // Si la usas
    }

    private fun popularVistasConDatos() {
        textoNombreMascota.text = receivedNombreMascota
        textoEspecieMascota.text = "Especie: ${receivedEspecieMascota}"
        textoRazaMascota.text = "Raza: ${receivedRazaMascota}"
        textoEdadMascota.text = "Edad: ${receivedEdadMascota}" // Asume que receivedEdadMascota ya es un String
        textoTamanoMascota.text = "Tamaño: ${receivedTamanioMascota}"
        textoDescripcionMascota.text = receivedDescripcionMascota

        if (!receivedFotoMascotaUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(receivedFotoMascotaUrl)
                .placeholder(R.drawable.ic_circulo_fondo) // Cambia a tu placeholder
                .into(imageMascota)
        } else {
            imageMascota.setImageResource(R.drawable.ic_mis_mascotas) // Placeholder si no hay URL
        }

        // Opcional: Mostrar nombre del dueño si tienes un TextView para ello
        // val tvNombreDuenoSolicitante: TextView = findViewById(R.id.tvNombreDuenoSolicitante)
        // tvNombreDuenoSolicitante.text = "Solicitud de: $receivedNombreDueno"
    }

    private fun aceptarSolicitudYCrearChat() {
        val cuidadorUid = auth.currentUser?.uid
        if (cuidadorUid == null) {
            Toast.makeText(this, "Error: Cuidador no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreCuidadorActual = auth.currentUser?.displayName ?: "Cuidador Anónimo"
        val fotoUrlCuidadorActual = auth.currentUser?.photoUrl?.toString()

        // progressBar?.visibility = View.VISIBLE // Si usas ProgressBar
        btnAceptar.isEnabled = false

        val solicitudRef = db
            .collection("usuarios")
            .document(cuidadorUid)
            .collection("solicitudes")
            .document(receivedIdSolicitud)
        val nuevoChatDocRef = db.collection("chats").document()
        val nuevoChatId = nuevoChatDocRef.id

        db.runBatch { batch ->
            // 1. Actualizar la solicitud
            val actualizacionSolicitud = mapOf(
                "estado" to "aceptada",
                "idChat" to nuevoChatId, // Guardamos el ID del chat en la solicitud
                "fechaActualizacion" to FieldValue.serverTimestamp()
            )
            batch.update(solicitudRef, actualizacionSolicitud)

            // 2. Crear el nuevo documento de chat
            val infoParticipantesMap = mapOf(
                receivedIdDueno to mapOf("nombre" to receivedNombreDueno, "fotoUrl" to (receivedFotoUrlDueno ?: "")), // USA LOS DATOS RECIBIDOS
                cuidadorUid to mapOf("nombre" to nombreCuidadorActual, "fotoUrl" to (fotoUrlCuidadorActual ?: ""))
            )

            val datosChat = hashMapOf(
                "idChat" to nuevoChatId,
                "idSolicitud" to receivedIdSolicitud, // Referencia a la solicitud original
                "participantes" to listOf(receivedIdDueno, cuidadorUid),
                "infoParticipantes" to infoParticipantesMap,
                "ultimoMensajeTexto" to "¡Tu solicitud ha sido aceptada! Ya pueden coordinar.",
                "ultimoMensajeTimestamp" to FieldValue.serverTimestamp(),
                "ultimoMensajeEnviadoPor" to "sistema", // O cuidadorUid si quieres que aparezca como mensaje del cuidador
                "noLeidoPor" to mapOf(receivedIdDueno to 1, cuidadorUid to 0), // El dueño tiene 1 no leído (este mensaje)
                "fechaCreacion" to FieldValue.serverTimestamp()
                // Considera añadir un campo "tipoDeChat" si tienes diferentes tipos de chat (ej: "solicitud_cuidado")
            )
            batch.set(nuevoChatDocRef, datosChat)

            // 3. (Opcional pero recomendado) Añadir un primer mensaje de sistema a la subcolección de mensajes del chat
            val primerMensajeRef = nuevoChatDocRef.collection("mensajes").document()
            val mensajeSistema = hashMapOf(
                "idMensaje" to primerMensajeRef.id,
                "idEmisor" to "sistema",
                "idReceptor" to receivedIdDueno, // Para saber a quién iba dirigido este mensaje del sistema
                "texto" to "[MENSAJE DEL SISTEMA]\nEl cuidador ha aceptado la solicitud. Ya pueden comunicarse por aquí para coordinar los detalles.",
                "timestamp" to FieldValue.serverTimestamp(),
                "leido" to false, // O un mapa de {uid: timestamp} para saber cuándo leyó cada uno
                "tipo" to "sistema" // O "notificacion", "texto", etc.
            )
            batch.set(primerMensajeRef, mensajeSistema)

        }.addOnSuccessListener {
            // progressBar?.visibility = View.GONE // Si usas ProgressBar
            Toast.makeText(this, "Solicitud aceptada. Chat iniciado.", Toast.LENGTH_LONG).show()

            val resultIntent = Intent()
            resultIntent.putExtra("solicitudProcesadaId", receivedIdSolicitud) // Para que la lista anterior pueda identificar y actualizar/eliminar
            resultIntent.putExtra("chatCreadoId", nuevoChatId) // Podría ser útil para la actividad anterior
            setResult(Activity.RESULT_OK, resultIntent)

            // NAVEGAR A LA ACTIVIDAD DE CHAT (CUANDO LA TENGAS)
            /*
            val intentChat = Intent(this, ChatActivity::class.java).apply {
                putExtra("idChat", nuevoChatId)
                putExtra("nombreChat", "Chat con ${receivedNombreDueno}") // O el nombre de la mascota
                putExtra("idReceptor", receivedIdDueno)
                putExtra("nombreReceptor", receivedNombreDueno)
                putExtra("fotoReceptorUrl", receivedFotoUrlDueno)
            }
            startActivity(intentChat)
            */
            finish() // Cierra DetalleSolicitudActivity

        }.addOnFailureListener { e ->
            // progressBar?.visibility = View.GONE // Si usas ProgressBar
            btnAceptar.isEnabled = true // Re-habilita el botón en caso de error
            Toast.makeText(this, "Error al aceptar solicitud: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e("DetalleSolicitud", "Error al procesar aceptación y crear chat", e)
        }
    }
}