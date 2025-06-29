package com.example.petcareapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareapp.R
import com.example.petcareapp.models.Solicitud
import java.text.SimpleDateFormat
import java.util.Locale

class SolicitudAdapter (
    private val solicitudes: List<Solicitud>,
    private val onItemClick: (Solicitud) -> Unit)
    : RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder>() {

        inner class SolicitudViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textoNombre: TextView = itemView.findViewById(R.id.txtNombreDuenio)
            val textoFecha: TextView = itemView.findViewById(R.id.txtFechaSolicitud)
            //val iconoAdvertencia: ImageView = itemView.findViewById(R.id.iconoAdvertencia)
            //val inicial: TextView = itemView.findViewById(R.id.textoInicial)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
            val vista = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_solicitud, parent, false)
            return SolicitudViewHolder(vista)
        }

        override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
            val solicitud = solicitudes[position]
            holder.textoNombre.text = solicitud.nombreDueno

            // Configurar la fecha
            solicitud.fecha?.let { timestamp ->
                val date = timestamp.toDate()
                val formato = SimpleDateFormat("dd 'de' MMM 'de' yyyy, HH:mm", Locale("es", "CL"))
                holder.textoFecha.text = formato.format(date)
            } ?: run {
                holder.textoFecha.text = "Fecha desconocida"
            }

            holder.itemView.setOnClickListener { onItemClick(solicitud) }
        }

        override fun getItemCount(): Int = solicitudes.size
    }