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
// Asumiendo que tendrás estas actividades, sino coméntalas o créalas:
// import com.example.petcareapp.PerfilCuidadorActivity // O el nombre correcto de tu Activity de perfil
// import com.example.petcareapp.ChatsCuidadorActivity // O el nombre correcto de tu Activity de chats


class MapaCuidadorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    // Si planeas usar la ubicación del usuario y Places API, descomenta y usa estas:
    // private lateinit var fusedLocationClient: FusedLocationProviderClient
    // private lateinit var placesClient: PlacesClient
    // private var lastKnownLocation: Location? = null
    // private val defaultLocation = LatLng(-38.7396, -72.5904) // Temuco

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa_cuidador)

        // Asume que el ID raíz de tu activity_mapa_cuidador.xml es 'mainMapCuidador'
        // Si sigue siendo 'main', usa R.id.main
        val rootView = findViewById<android.view.View>(R.id.mainMapCuidador) // O R.id.main si no lo cambiaste

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Aplicar padding solo a los lados del rootView para que el contenido no se oculte
            // detrás de las barras de navegación laterales (si las hubiera en modo landscape o tablet)
            // El padding superior e inferior lo manejaremos de forma más específica.
            v.setPadding(systemBars.left, 0, systemBars.right, 0)

            // 1. Padding superior para el fragmento del mapa
            // Esto es para que el contenido del mapa no quede debajo de la barra de estado
            // si la barra de estado es transparente debido a enableEdgeToEdge.
            val mapFragmentView = supportFragmentManager.findFragmentById(R.id.mapFragmentCuidador)?.view
            mapFragmentView?.setPadding(0, systemBars.top, 0, 0)


            // 2. Padding inferior para la barra de navegación (si la tienes y quieres que respete los insets)
            // Si tu barra de navegación inferior (barraInferiorMapaCuidador) está al final del ConstraintLayout,
            // y quieres que el ConstraintLayout mismo tenga padding para la barra de navegación del sistema,
            // puedes aplicar systemBars.bottom al padding inferior del rootView.
            // O, si tu barra de navegación personalizada ya tiene una altura fija y está correctamente
            // restringida al fondo, puede que esto no sea estrictamente necesario o
            // que prefieras manejar el padding dentro de la propia barra.
            // Para un control simple, aplicar el padding inferior al rootView puede funcionar:
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)


            // Devolver los insets consumidos (o no, dependiendo de tu estrategia)
            // WindowInsetsCompat.CONSUMED // Si los has manejado completamente
            insets // Si quieres que otros listeners también los reciban
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