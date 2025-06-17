package com.example.petcareapp

import android.content.Intent // Necesario para navegar a otras actividades
import android.os.Bundle
import android.widget.ImageView // Necesario para referenciar los ImageViews como botones
import android.widget.Toast // Para mensajes de ejemplo
import androidx.activity.enableEdgeToEdge
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
        setContentView(R.layout.activity_mapa_cuidador)


        val rootView = findViewById<android.view.View>(R.id.mainMapCuidador)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())


            v.setPadding(systemBars.left, 0, systemBars.right, 0)


            val mapFragmentView = supportFragmentManager.findFragmentById(R.id.mapFragmentCuidador)?.view
            mapFragmentView?.setPadding(0, systemBars.top, 0, 0)



            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)



            insets
        }


        // Obtener el SupportMapFragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentCuidador) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // --- Configurar botones de la barra inferior ---
        // Asegúrate de que los IDs coincidan con los de tu activity_mapa_cuidador.xml
        val btnNavPerfilEnMapa: ImageView = findViewById(R.id.btnNavPerfilEnMapa)
        val btnNavMapaEnMapa: ImageView = findViewById(R.id.btnNavMapaEnMapa)
        val btnNavChatsEnMapa: ImageView = findViewById(R.id.btnNavChatsEnMapa)

        btnNavPerfilEnMapa.setOnClickListener {
            // Toast.makeText(this, "Ir a Perfil desde Mapa", Toast.LENGTH_SHORT).show() // Puedes eliminar o comentar esto
            val intent = Intent(this, PerfilCuidadorActivity::class.java)
            // Considera flags para la gestión de la pila si es parte de una navegación tipo "tab"
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            // No llames a finish() si quieres que el usuario pueda volver al mapa con "atrás"
        }

        btnNavMapaEnMapa.setOnClickListener {
            // Ya estás en la pantalla del mapa.
            // Podrías centrar el mapa en la ubicación del usuario, o no hacer nada.
            Toast.makeText(this, "Ya estás en la pantalla del Mapa", Toast.LENGTH_SHORT).show()
            // Ejemplo: centrar en la ubicación por defecto que tienes o la ubicación del usuario si la implementas
            // val ubicacionCuidador = LatLng(-38.7396, -72.5904)
            // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCuidador, 15f))
        }

        btnNavChatsEnMapa.setOnClickListener {
            Toast.makeText(this, "Ir a Chats desde Mapa", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, ChatsCuidadorActivity::class.java) // Reemplaza ChatsCuidadorActivity
            // startActivity(intent)
            // finish() // Opcional
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Tu lógica existente para el mapa del cuidador
        val ubicacionCuidador = LatLng(-38.7396, -72.5904)
        googleMap.addMarker(MarkerOptions().position(ubicacionCuidador).title("Ubicación Cuidador en Temuco"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionCuidador, 15f))
        googleMap.uiSettings.isZoomControlsEnabled = true // Ejemplo, habilitar controles de zoom

        // Aquí es donde también iniciarías la lógica de permisos y obtención de ubicación
        // si vas a implementar la búsqueda de veterinarios cercanos, como se discutió antes.
        // Ejemplo: checkLocationPermission()
    }

    // Si implementas la búsqueda de veterinarios, aquí irían los métodos:
    // private fun checkLocationPermission() { ... }
    // @SuppressLint("MissingPermission")
    // private fun getCurrentLocationAndFindVets() { ... }
    // private fun findNearbyVets(location: LatLng) { ... }
    // ... y el requestPermissionLauncher
}