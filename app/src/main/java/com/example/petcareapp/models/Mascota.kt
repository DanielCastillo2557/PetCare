package com.example.petcareapp.models

import com.google.firebase.firestore.Exclude // Importante para el ID

data class Mascota(
    @get:Exclude var id: String? = null, // ID del documento de Firestore
    var nombre: String = "",
    var raza: String = "",
    var edad: String = "",
    var especie: String = "",
    var tamanio: String = "",
    var fotoUrl: String = "",
    var descripcion: String = "",
    var encargada: Boolean = false
) {
    // Constructor sin argumentos requerido por Firestore para la deserialización
    constructor() : this(null, "", "", "", "", "", "", "")
}
