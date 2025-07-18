package com.example.petcareapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareapp.R
import com.example.petcareapp.models.Solicitud
import java.text.SimpleDateFormat
import java.util.Locale
// No necesitas "import com.google.firebase.Timestamp" aquí si trabajas directamente con java.util.Date

class SolicitudAdapter (
    private val solicitudes: List<Solicitud>,
    private val onItemClick: (Solicitud) -> Unit)
    : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

    inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textoNombre: TextView = itemView.findViewById(R.id.txtNombreDuenio)
        //val textoNombreMascota: TextView = itemView.findViewById(R.id.txtNombreMascota)
        val imagenDuenio: ImageView = itemView.findViewById(R.id.imgDuenio)
        val textoFecha: TextView = itemView.findViewById(R.id.txtFechaSolicitud)
        // Aquí podrías añadir más vistas si tu item_solicitud.xml las tiene, como una ImageView para foto.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitud, parent, false)
        return SolicitudViewHolder(vista)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = solicitudes[position]
        holder.textoNombre.text = solicitud.nombreDueno
        //holder.textoNombreMascota.text = solicitud.nombreMascota

        val fotoUrl = solicitud.fotoUrl
        if (!fotoUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_user) // Imagen temporal mientras carga
                .error(R.drawable.ic_user) // Imagen si falla la carga
                .into(holder.imagenDuenio)
        } else {
            holder.imagenDuenio.setImageResource(R.drawable.ic_user)
        }

        // Configurar la fecha
        // COMO 'solicitud.fecha' YA ES UN java.util.Date?, NO NECESITAS .toDate()
        solicitud.fecha?.let { dateObject -> // 'dateObject' ya es un java.util.Date
            val formato = SimpleDateFormat("dd 'de' MMM 'de' yyyy, HH:mm", Locale("es", "CL"))
            holder.textoFecha.text = formato.format(dateObject) // Usar directamente dateObject
        } ?: run {
            holder.textoFecha.text = "Fecha desconocida"
        }

        holder.itemView.setOnClickListener { onItemClick(solicitud) }
    }

    override fun getItemCount(): Int = solicitudes.size
}