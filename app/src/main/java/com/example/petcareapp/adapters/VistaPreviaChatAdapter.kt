package com.example.petcareapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.example.petcareapp.R
import com.example.petcareapp.models.VistaPreviaChat // Importa tu modelo VistaPreviaChat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class VistaPreviaChatAdapter(
    private val listaChats: List<VistaPreviaChat>,
    private val onItemClicked: (VistaPreviaChat) -> Unit
) : RecyclerView.Adapter<VistaPreviaChatAdapter.VistaPreviaChatViewHolder>() {

    inner class VistaPreviaChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewReceptorFoto: ImageView = itemView.findViewById(R.id.imageViewReceptorFoto)
        private val textViewReceptorNombre: TextView = itemView.findViewById(R.id.textViewReceptorNombre)
        private val textViewUltimoMensaje: TextView = itemView.findViewById(R.id.textViewUltimoMensaje)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestampUltimoMensaje)

        fun bind(vistaPreviaChat: VistaPreviaChat) {
            textViewReceptorNombre.text = vistaPreviaChat.nombreOtroUsuario
            textViewUltimoMensaje.text = vistaPreviaChat.ultimoMensaje

            if (vistaPreviaChat.timestampUltimoMensaje != null) {
                textViewTimestamp.text = formatTimestamp(vistaPreviaChat.timestampUltimoMensaje)
            } else {
                textViewTimestamp.text = ""
            }

            Glide.with(itemView.context)
                .load(vistaPreviaChat.fotoUrlOtroUsuario)
                .placeholder(R.drawable.ic_user) // Asegúrate de tener este drawable
                .error(R.drawable.ic_user)       // O usa R.mipmap.ic_launcher si prefieres
                .into(imageViewReceptorFoto)

            itemView.setOnClickListener {
                onItemClicked(vistaPreviaChat)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaPreviaChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_preview, parent, false) // Usa tu layout de item
        return VistaPreviaChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: VistaPreviaChatViewHolder, position: Int) {
        holder.bind(listaChats[position])
    }

    override fun getItemCount(): Int = listaChats.size

    private fun formatTimestamp(timestamp: Date): String {
        val ahora = Calendar.getInstance()
        val mensajeCal = Calendar.getInstance()
        mensajeCal.time = timestamp

        val diffMillis = ahora.timeInMillis - mensajeCal.timeInMillis
        val diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis)
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            diffSeconds < 60 -> "Ahora"
            diffMinutes < 60 -> "${diffMinutes}m"
            diffHours < 24 && ahora.get(Calendar.DAY_OF_YEAR) == mensajeCal.get(Calendar.DAY_OF_YEAR) ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp) // Hora:Minuto si es hoy
            diffDays < 1 || (diffHours < 24 && ahora.get(Calendar.DAY_OF_YEAR) != mensajeCal.get(Calendar.DAY_OF_YEAR)) ->
                "Ayer" // Si fue ayer
            diffDays < 7 ->
                SimpleDateFormat("EEE", Locale.getDefault()).format(timestamp) // Nombre del día (Lun, Mar) si es esta semana
            else ->
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(timestamp) // Fecha completa si es más antiguo
        }
    }
}