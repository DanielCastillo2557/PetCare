package com.example.petcareapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.ImageView // Importar ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices


class MapaActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapa)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Configurar el botón de retroceso de la barra superior ---
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {

            onBackPressedDispatcher.onBackPressed()


        }


        // Configurar el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        verificarPermisosUbicacion()

        // Aplicar estilo al mapa
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) {
                Log.e("MAPA", "Error al aplicar estilo al mapa")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MAPA", "No se encontró el archivo de estilo", e)
        }

        // Obtener ubicación actual
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val miUbicacion = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 14f))
                }
            }
        }

        // Cargar cuidadores cercanos
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .whereArrayContains("tipo", "cuidador") // Solo usuarios cuidadores
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombre") ?: "Cuidador"
                    val ubicacion = document.get("ubicacion") as? Map<*, *>
                    val lat = (ubicacion?.get("lat") as? Double) ?: continue
                    val lng = (ubicacion["lng"] as? Double) ?: continue

                    // Agregar marcador para cada cuidador
                    val position = LatLng(lat, lng)
                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(nombre)
                            .snippet("Cuidador disponible")
                    )
                    marker?.tag = document.id // Guardar el ID del cuidador en el tag del marcador
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar cuidadores", Toast.LENGTH_SHORT).show()
            }

        // Manejar clic en un marcador
        googleMap.setOnInfoWindowClickListener { marker ->
            val cuidadorId = marker.tag as? String
            if (cuidadorId != null) {
                val intent = Intent(this, CuidadorSeleccionadoActivity::class.java)
                intent.putExtra("cuidadorId", cuidadorId)
                startActivity(intent)
            }
        }

    }

    // Solicitar permisos de ubicación
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private fun verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Ya tienes el permiso, habilita la capa de mi ubicación
            if (::googleMap.isInitialized) { // Asegúrate que el mapa esté listo
                googleMap.isMyLocationEnabled = true
            }
        } else {
            // Solicitar el permiso
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Manejar la respuesta de la solicitud de permisos
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Es buena práctica llamar a super primero

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                if (::googleMap.isInitialized) {
                    try {
                        googleMap.isMyLocationEnabled = true
                        // Intenta obtener la ubicación actual de nuevo y mover la cámara
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                val miUbicacion = LatLng(location.latitude, location.longitude)
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 14f))
                            } else {
                                Toast.makeText(this, "No se pudo obtener la ubicación actual tras conceder permiso.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: SecurityException) {
                        Log.e("MAPA", "Error de seguridad al habilitar mi ubicación", e)
                    }
                }
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de ubicación denegado. Algunas funciones del mapa pueden no estar disponibles.", Toast.LENGTH_LONG).show()
            }
        }
    }
}