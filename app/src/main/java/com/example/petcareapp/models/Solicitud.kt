package com.example.petcareapp.models

import com.google.firebase.firestore.DocumentId // Importante para el ID del documento
import com.google.firebase.firestore.ServerTimestamp // Para que Firestore ponga la fecha/hora
import java.util.Date // Es común usar java.util.Date con @ServerTimestamp

data class Solicitud(
    @DocumentId var id: String = "", // <--- CAMBIO CLAVE: Para almacenar el ID del documento

    val idMascota: String = "",

    @ServerTimestamp var fecha: Date? = null, // CAMBIO: Usar Date y @ServerTimestamp
    // Firestore guardará com.google.firebase.Timestamp
    // pero puede convertirlo a/desde java.util.Date

    var estado: String = "pendiente", // CAMBIO: Hecho 'var' y con valor por defecto

    val idDueno: String = "",
    val nombreDueno: String = "",
    // Considera añadir aquí:
    // val fotoUrlDueno: String? = null, // Si quieres guardar la foto del dueño con la solicitud

    // Campos de la mascota
    val nombreMascota: String = "",
    val especie: String = "",
    val raza: String = "",
    val edad: String = "0", // CAMBIO SUGERIDO: String para consistencia con Intents,
    // o mantenlo Int/Long y convierte al pasar por Intent.
    // Si es número en Firestore, usa Long en el modelo (edad: Long = 0)
    val tamanio: String = "",
    val descripcion: String = "",
    val fotoUrl: String? = null, // CAMBIO: Hecho nullable (String?)
    val albumFotos: List<String> = emptyList(),
    // Campo para el ID del chat cuando se acepte
    var idChat: String? = null  // <--- NUEVO
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(
        id = "",
        idMascota = "",
        fecha = null,
        estado = "pendiente",
        idDueno = "",
        nombreDueno = "",
        // fotoUrlDueno = null,
        nombreMascota = "",
        especie = "",
        raza = "",
        edad = "0", // o 0L si es Long
        tamanio = "",
        descripcion = "",
        fotoUrl = null,
        albumFotos = emptyList(),
        idChat = null
    )
}
