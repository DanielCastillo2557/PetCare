package com.example.petcareapp.cuidador


import com.example.petcareapp.models.VeterinariaInfo
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.petcareapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder



class MapaCuidadorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var locationCallback: LocationCallback

    // Esto seguirá funcionando gracias a la importación de VeterinariaInfo
    private val marcadoresVeterinarias = HashMap<Marker, VeterinariaInfo>()
    private var marcadorUbicacionFija: Marker? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val DEFAULT_ZOOM = 15f
    private val OVERPASS_SEARCH_RADIUS_METERS = 5000
    private var mapIsReady = false
    private var locationPermissionGranted = false
    private var initialLocationUpdateDone = false

    private val USE_FIXED_LOCATION_FOR_TESTING = true
    private val FIXED_TEST_LATITUDE = -38.739670
    private val FIXED_TEST_LONGITUDE = -72.590393
    private val FIXED_LOCATION_PROVIDER_NAME = "fixed_test_provider"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa_cuidador)
        val rootView = findViewById<android.view.View>(R.id.mainMapCuidador)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragmentCuidador) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupBottomNavigationBar()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (USE_FIXED_LOCATION_FOR_TESTING) return

                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    if (mapIsReady && !initialLocationUpdateDone) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM))
                        cargarVeterinariasConOverpass(location)
                        initialLocationUpdateDone = true
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapIsReady = true
        configurarEstiloMapa()
        googleMap.uiSettings.isZoomControlsEnabled = true
        verificarPermisosUbicacion()

        googleMap.setOnMarkerClickListener { marker ->
            if (marker == marcadorUbicacionFija) {
                Toast.makeText(this, marker.title, Toast.LENGTH_SHORT).show()
                true
            }
            else {
                // val vetInfo = marcadoresVeterinarias[marker] // Puedes obtener la info si la necesitas
                // if (vetInfo != null) { /* Hacer algo con vetInfo si es necesario ANTES de mostrar la InfoWindow */ }
                false // Permite que Google Maps muestre la InfoWindow por defecto
            }
        }
    }

    private fun configurarEstiloMapa() {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) Log.e("MAPA_CUIDADOR", "Error al aplicar estilo al mapa")
        } catch (e: Resources.NotFoundException) {
            Log.e("MAPA_CUIDADOR", "No se encontró el archivo de estilo R.raw.map_style", e)
        }
    }

    private fun verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            activarMiUbicacionYBuscar()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun activarMiUbicacionYBuscar() {
        if (!mapIsReady) return

        for (markerInMap in marcadoresVeterinarias.keys) {
            markerInMap.remove()
        }
        marcadoresVeterinarias.clear()
        marcadorUbicacionFija?.remove()
        marcadorUbicacionFija = null

        if (USE_FIXED_LOCATION_FOR_TESTING) {
            val fixedLocation = Location(FIXED_LOCATION_PROVIDER_NAME).apply {
                latitude = FIXED_TEST_LATITUDE
                longitude = FIXED_TEST_LONGITUDE
            }
            currentLocation = fixedLocation
            val fixedLatLng = LatLng(fixedLocation.latitude, fixedLocation.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fixedLatLng, DEFAULT_ZOOM))
            marcadorUbicacionFija = googleMap.addMarker(
                MarkerOptions()
                    .position(fixedLatLng)
                    .title("Ubicación de Prueba: San Martín 599")
            )
            cargarVeterinariasConOverpass(fixedLocation)
            initialLocationUpdateDone = true

        } else {
            if (!locationPermissionGranted) {
                Toast.makeText(this, "Permiso de ubicación necesario.", Toast.LENGTH_SHORT).show()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-38.7359, -72.5904), 12f))
                return
            }
            googleMap.isMyLocationEnabled = true
            initialLocationUpdateDone = false
            solicitarActualizacionesDeUbicacion()
        }
    }

    @SuppressLint("MissingPermission")
    private fun solicitarActualizacionesDeUbicacion() {
        if (USE_FIXED_LOCATION_FOR_TESTING || !locationPermissionGranted) return
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e("MAPA_CUIDADOR", "Error al solicitar actualizaciones", e)
        }
    }

    private fun stopLocationUpdates() {
        if (!USE_FIXED_LOCATION_FOR_TESTING) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun cargarVeterinariasConOverpass(userLocation: Location) {
        val userLat = userLocation.latitude
        val userLng = userLocation.longitude

        for (markerInMap in marcadoresVeterinarias.keys) {
            markerInMap.remove()
        }
        marcadoresVeterinarias.clear()

        val overpassQuery = """
            [out:json][timeout:25];
            (
              node["amenity"="veterinary"](around:$OVERPASS_SEARCH_RADIUS_METERS,$userLat,$userLng);
              way["amenity"="veterinary"](around:$OVERPASS_SEARCH_RADIUS_METERS,$userLat,$userLng);
              relation["amenity"="veterinary"](around:$OVERPASS_SEARCH_RADIUS_METERS,$userLat,$userLng);
            );
            out center;
        """.trimIndent()

        try {
            val encodedQuery = URLEncoder.encode(overpassQuery, "UTF-8")
            val overpassUrl = "https://overpass-api.de/api/interpreter?data=$encodedQuery"
            val queue = Volley.newRequestQueue(this)
            val stringRequest = StringRequest(Request.Method.GET, overpassUrl,
                { response -> procesarRespuestaOverpass(response) },
                { error ->
                    Log.e("MAPA_OVERPASS", "Error en Overpass: ${error.message}", error)
                    Toast.makeText(this, "Error al buscar veterinarias (Overpass)", Toast.LENGTH_LONG).show()
                }
            )
            queue.add(stringRequest)
        } catch (e: Exception) {
            Log.e("MAPA_OVERPASS", "Error URL Overpass", e)
        }
    }

    private fun procesarRespuestaOverpass(jsonResponse: String) {
        if (!mapIsReady) return
        try {
            val jsonObject = JSONObject(jsonResponse)
            val elements = jsonObject.getJSONArray("elements")
            var veterinariasEncontradas = 0

            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                val tags = element.optJSONObject("tags")
                val nombre = tags?.optString("name", "Veterinaria (OSM)") ?: "Veterinaria (OSM)"
                val telefono = tags?.optString("phone")
                    ?: tags?.optString("contact:phone")
                    ?: tags?.optString("tel")

                var lat: Double? = null
                var lon: Double? = null
                if (element.has("lat") && element.has("lon")) {
                    lat = element.getDouble("lat")
                    lon = element.getDouble("lon")
                } else if (element.has("center")) {
                    val center = element.getJSONObject("center")
                    lat = center.getDouble("lat")
                    lon = center.getDouble("lon")
                }

                if (lat != null && lon != null) {
                    val position = LatLng(lat, lon)
                    // Aquí se usa VeterinariaInfo, que ahora está importada
                    val vetInfo = VeterinariaInfo(nombre, position, telefono)

                    val snippetInfo = if (!vetInfo.telefono.isNullOrEmpty()) {
                        "Tel: ${vetInfo.telefono}"
                    } else {
                        "Teléfono no disponible"
                    }

                    val markerOptions = MarkerOptions()
                        .position(vetInfo.latLng)
                        .title(vetInfo.nombre)
                        .snippet(snippetInfo)

                    val marker = googleMap.addMarker(markerOptions)
                    if (marker != null) {
                        marcadoresVeterinarias[marker] = vetInfo
                    }
                    veterinariasEncontradas++
                }
            }
            val baseMessage = if (USE_FIXED_LOCATION_FOR_TESTING) "cerca de ubicación de prueba." else "en $OVERPASS_SEARCH_RADIUS_METERS m."
            if (veterinariasEncontradas == 0) {
                Toast.makeText(this, "No se encontraron veterinarias (OSM) $baseMessage", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "$veterinariasEncontradas veterinarias (OSM) encontradas $baseMessage", Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            Log.e("MAPA_OVERPASS", "Error JSON Overpass", e)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
                activarMiUbicacionYBuscar()
            } else {
                locationPermissionGranted = false
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_LONG).show()
                if (mapIsReady && !USE_FIXED_LOCATION_FOR_TESTING) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-38.7359, -72.5904), 12f))
                } else if (mapIsReady && USE_FIXED_LOCATION_FOR_TESTING) {
                    activarMiUbicacionYBuscar()
                }
            }
        }
    }

    private fun setupBottomNavigationBar() {
        val btnPerfilCuidadorSuperior: ImageView = findViewById(R.id.btnPerfilCuidador)
        val btnNavInicioCuidador: ImageView = findViewById(R.id.btnNavInicioCuidador)
        val btnNavMapa: ImageView = findViewById(R.id.btnNavMapaVets)
        val btnNavChats: ImageView = findViewById(R.id.btnNavChatsCuidador)

        btnPerfilCuidadorSuperior.setOnClickListener {
            val intent = Intent(this, PerfilCuidadorActivity::class.java)
            startActivity(intent)
        }

        btnNavInicioCuidador.setOnClickListener {
            val intent = Intent(this, InicioCuidadorActivity::class.java)
            startActivity(intent)
        }

        btnNavMapa.setOnClickListener {
            Toast.makeText(this, "Refrescando mapa...", Toast.LENGTH_SHORT).show()
            if (mapIsReady) {
                initialLocationUpdateDone = false
                activarMiUbicacionYBuscar()
            }
        }

        btnNavChats.setOnClickListener {
            val intent = Intent(this, ListaChatsCuidadorActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mapIsReady) {
            if (locationPermissionGranted || USE_FIXED_LOCATION_FOR_TESTING) {
                initialLocationUpdateDone = false
                activarMiUbicacionYBuscar()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}