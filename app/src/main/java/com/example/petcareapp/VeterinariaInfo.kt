package com.example.petcareapp

import com.google.android.gms.maps.model.LatLng

data class VeterinariaInfo(
    val nombre: String,
    val latLng: LatLng,
    val telefono: String? = null,
    val fuente: String = "Fuente: OpenStreetMap"
)
