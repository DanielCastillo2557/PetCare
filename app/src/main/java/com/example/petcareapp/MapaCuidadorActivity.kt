package com.example.petcareapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge // Mantén esto si quieres el mapa edge-to-edge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaCuidadorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa_cuidador) // Asegúrate que es el layout correcto


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplicar padding solo a la parte superior si el mapa ocupa toda la pantalla
            // y quieres que el contenido del mapa (no los controles) esté debajo de la barra de estado.
            // Opcionalmente, podrías querer aplicar padding a los controles del mapa de otra manera.
            // Para un mapa a pantalla completa, a veces no se aplica padding aquí y se deja
            // que el mapa se dibuje completamente edge-to-edge.
            // Por ahora, lo mantendremos como lo tienes para consistencia.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Obtener el SupportMapFragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentCuidador) as SupportMapFragment // Usa el ID de tu fragmento en activity_mapa_cuidador.xml

        // 5. Solicitar el mapa de forma asíncrona
        mapFragment.getMapAsync(this)
    }

    // 6. Implementar el método onMapReady
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Aquí puedes poner la lógica específica para el mapa del cuidador.
        // Puede ser la misma ubicación inicial, o una diferente.
        // Ejemplo: Ubicación para el cuidador (podría ser diferente a la de MapaActivity)
        val ubicacionCuidador = LatLng(-38.7396, -72.5904) // Ejemplo, Temuco (ligeramente diferente para distinguir)
        googleMap.addMarker(MarkerOptions().position(ubicacionCuidador).title("Ubicación Cuidador en Temuco"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCuidador, 15f)) // Zoom diferente

        // Otras configuraciones del mapa para el cuidador:
        // googleMap.uiSettings.isZoomControlsEnabled = true
        // googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        // ... etc.
    }
}