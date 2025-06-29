package com.example.petcareapp.models

import com.google.firebase.Timestamp

data class Solicitud(
    val idMascota: String = "",
    val fecha: Timestamp? = null,
    val estado: String = "", // Ej: "pendiente", "aceptada", "rechazada"
    val idDueno: String = "",   // UID del dueño que envió la solicitud
    val nombreDueno: String = "",  // Nombre del dueño que envió la solicitud

    // Campos duplicados
    val nombreMascota: String = "",
    val especie: String = "",
    val raza: String = "",
    val edad: Int = 0,
    val tamanio: String = "",
    val descripcion: String = "",
    val fotoUrl: String = ""
)
