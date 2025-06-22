package com.example.petcareapp

import com.google.firebase.Timestamp

data class Solicitud(
    val idMascota: String = "",
    val nombreMascota: String = "",
    val fecha: Timestamp? = null,
    val estado: String = "", // Ej: "pendiente", "aceptada", "rechazada"
    val idDueno: String = "",   // UID del dueño que envió la solicitud
    val nombreDueno: String = ""  // Nombre del dueño que envió la solicitud
)
