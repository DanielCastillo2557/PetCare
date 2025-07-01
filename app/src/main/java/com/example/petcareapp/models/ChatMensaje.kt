package com.example.petcareapp.models // Esta línea ya es correcta según tu archivo

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatMensaje(
    val idMensaje: String = "",    // ID único del mensaje, puede ser autogenerado por Firestore
    val idEmisor: String = "",     // UID del usuario que envía el mensaje
    val idReceptor: String = "",   // UID del usuario que recibe el mensaje (opcional si ya está en la info del chat)
    val texto: String = "",        // Contenido textual del mensaje
    @ServerTimestamp               // Anotación para que Firestore ponga la hora del servidor
    val timestamp: Date? = null    // Fecha y hora del mensaje, nullable y rellenado por el servidor
) {
    // Constructor sin argumentos requerido por Firestore para la deserialización.
    // Como todas las propiedades del constructor primario tienen valores por defecto,
    // este constructor secundario explícito es una buena práctica pero a menudo
    // Kotlin lo maneja implícitamente para data classes con valores por defecto.
    // No obstante, es más seguro incluirlo para asegurar la compatibilidad con Firestore.
    constructor() : this(
        idMensaje = "",
        idEmisor = "",
        idReceptor = "",
        texto = "",
        timestamp = null
    )
}