package com.example.petcareapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
//import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petcareapp.R
import com.example.petcareapp.models.Mascota

class MascotaAdapter(
    private val lista: List<Mascota>,
    private val onItemClick: (Mascota) -> Unit) :
    RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val imgMascota: ImageView = view.findViewById(R.id.imgMascota)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreCuidador)
        val txtRaza: TextView = view.findViewById(R.id.txtPuntuacionCuidador)
        val imagenMascota: ImageView = itemView.findViewById(R.id.imgMascota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = lista[position]
        holder.txtNombre.text = mascota.nombre
        holder.txtRaza.text = mascota.raza

        // Configurar la imagen
        val fotoUrl = mascota.fotoUrl
        if (!fotoUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_user) // Imagen temporal mientras carga
                .error(R.drawable.ic_user) // Imagen si falla la carga
                .into(holder.imagenMascota)
        } else {
            holder.imagenMascota.setImageResource(R.drawable.ic_user)
        }

        holder.itemView.setOnClickListener {
            onItemClick(mascota)
        }
    }

    override fun getItemCount(): Int = lista.size
}
