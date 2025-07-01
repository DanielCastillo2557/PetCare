package com.example.petcareapp.adapters // Esta línea ya es correcta según tu archivo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.example.petcareapp.R
import com.example.petcareapp.models.ChatMensaje // Importa tu modelo de datos ChatMensaje
import java.text.SimpleDateFormat
import java.util.Date // Asegúrate de importar java.util.Date
import java.util.Locale

class MensajeAdapter(
    private var listaMensajes: MutableList<ChatMensaje>,
    private val currentUserId: String // ID del usuario actual para diferenciar mensajes
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_VISTA_ENVIADO = 1
        private const val TIPO_VISTA_RECIBIDO = 2
    }

    // --- ViewHolder para mensajes ENVIADOS ---
    inner class ViewHolderEnviado(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Asegúrate que los IDs coincidan con los de item_mensaje_enviado.xml
        val textoMensaje: TextView = itemView.findViewById(R.id.textViewMessageContent)
        val timestampMensaje: TextView = itemView.findViewById(R.id.textViewMessageTimestamp)

        fun bind(mensaje: ChatMensaje) {
            textoMensaje.text = mensaje.texto
            if (mensaje.timestamp != null) {
                // Formatear la fecha para mostrarla de forma legible
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                timestampMensaje.text = sdf.format(mensaje.timestamp as Date) // Cast a Date si es necesario
            } else {
                timestampMensaje.text = "Enviando..." // Placeholder mientras no hay timestamp del servidor
            }
        }
    }

    // --- ViewHolder para mensajes RECIBIDOS ---
    inner class ViewHolderRecibido(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Asegúrate que los IDs coincidan con los de item_mensaje_recibido.xml
        val textoMensaje: TextView = itemView.findViewById(R.id.textViewMessageContent)
        val timestampMensaje: TextView = itemView.findViewById(R.id.textViewMessageTimestamp)
        // Opcional: TextView para el nombre del emisor si lo incluyes en el layout
        // val nombreEmisor: TextView = itemView.findViewById(R.id.textViewSenderName)

        fun bind(mensaje: ChatMensaje) {
            textoMensaje.text = mensaje.texto
            if (mensaje.timestamp != null) {
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                timestampMensaje.text = sdf.format(mensaje.timestamp as Date) // Cast a Date si es necesario
            } else {
                timestampMensaje.text = "" // Para mensajes recibidos, el timestamp debería estar
            }
            // Opcional: si muestras nombreEmisor
            // nombreEmisor.text = mensaje.nombreEmisor // Asumiendo que 'nombreEmisor' está en ChatMensaje
        }
    }

    override fun getItemViewType(position: Int): Int {
        val mensaje = listaMensajes[position]
        return if (mensaje.idEmisor == currentUserId) {
            TIPO_VISTA_ENVIADO
        } else {
            TIPO_VISTA_RECIBIDO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_VISTA_ENVIADO) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mensaje_enviado, parent, false) // Usa tu layout item_mensaje_enviado.xml
            ViewHolderEnviado(view)
        } else { // TIPO_VISTA_RECIBIDO
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mensaje_recibido, parent, false) // Usa tu layout item_mensaje_recibido.xml
            ViewHolderRecibido(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = listaMensajes[position]
        if (holder.itemViewType == TIPO_VISTA_ENVIADO) {
            (holder as ViewHolderEnviado).bind(mensaje)
        } else { // TIPO_VISTA_RECIBIDO
            (holder as ViewHolderRecibido).bind(mensaje)
        }
    }

    override fun getItemCount(): Int {
        return listaMensajes.size
    }

    // Funciones de utilidad para el Adapter
    fun agregarMensajes(nuevosMensajes: List<ChatMensaje>) {
        val startPosition = listaMensajes.size
        listaMensajes.addAll(nuevosMensajes)
        notifyItemRangeInserted(startPosition, nuevosMensajes.size)
    }

    fun agregarMensajeAlInicio(mensaje: ChatMensaje) { // Para cuando cargas mensajes más antiguos
        listaMensajes.add(0, mensaje)
        notifyItemInserted(0)
    }

    fun agregarMensajeAlFinal(mensaje: ChatMensaje) { // Para nuevos mensajes entrantes
        listaMensajes.add(mensaje)
        notifyItemInserted(listaMensajes.size - 1)
    }

    // Metodo para actualizar toda la lista, útil para la carga inicial o reseteos
    fun submitList(nuevaLista: List<ChatMensaje>) {
        listaMensajes.clear()
        listaMensajes.addAll(nuevaLista)
        notifyDataSetChanged() // Para simplificar. Considera DiffUtil para optimizaciones.
    }
}