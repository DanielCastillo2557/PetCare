package com.example.petcareapp.models // Asegúrate que el paquete sea correcto

import java.util.Date

data class VistaPreviaChat(
    val idChat: String = "",
    val idOtroUsuario: String = "",
    val nombreOtroUsuario: String = "",
    val fotoUrlOtroUsuario: String? = null, // Puede ser nulo si el usuario no tiene foto
    val ultimoMensaje: String = "",
    val timestampUltimoMensaje: Date? = null // Puede ser nulo si aún no hay mensajes o para el timestamp inicial
)