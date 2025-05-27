package com.example.petcareapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MascotaAdapter(private val lista: List<Mascota>) :
    RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val imgMascota: ImageView = view.findViewById(R.id.imgMascota)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreMascota)
        val txtRaza: TextView = view.findViewById(R.id.txtRazaMascota)
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
        // Aquí podrías cargar foto con Glide/Picasso si quieres
    }

    override fun getItemCount(): Int = lista.size
}
