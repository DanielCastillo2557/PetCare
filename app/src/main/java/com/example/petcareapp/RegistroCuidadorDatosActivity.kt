package com.example.petcareapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegistroCuidadorDatosActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var autoCompletarDireccion: AutoCompleteTextView
    private var latitud: Double? = null
    private var longitud: Double? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cuidador_datos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registroCuidador2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyC3OsTb7fayTkPuqYGpdY6vQRDkDrvu4YM")
        }

        val bounds = RectangularBounds.newInstance(
            LatLng(-38.80, -72.70), // suroeste
            LatLng(-38.70, -72.55)  // noreste
        )

        autoCompletarDireccion = findViewById(R.id.inputDireccion)
        autoCompletarDireccion.setOnClickListener {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setLocationBias(bounds)
                .setCountry("CL")
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtener datos del intent
        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val btnGuardar: ImageButton = findViewById(R.id.btnGuardarRegistroCuidador)

        //Boton siguiente
        btnGuardar.setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmailCuidador).text.toString()
            val contrasena = findViewById<EditText>(R.id.editContrasenaCuidador).text.toString()
            val repetir = findViewById<EditText>(R.id.editRepetirContrasenaCuidador).text.toString()
            val numero = findViewById<EditText>(R.id.editNumeroCuidador).text.toString()
            val direccion = autoCompletarDireccion.text.toString()

            if (email.isBlank() || contrasena != repetir) {
                Toast.makeText(this, "Revisa los datos ingresados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, contrasena)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    // 1. Registro en "usuarios"
                    val datosUsuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "telefono" to numero,
                        "direccion" to direccion,
                        "ubicacion" to hashMapOf(
                            "lat" to latitud,
                            "lng" to longitud
                        ),
                        "tipo" to listOf("cuidador"),
                        "fecha_creacion" to FieldValue.serverTimestamp()
                    )

                    db.collection("usuarios").document(uid).set(datosUsuario)
                        .addOnSuccessListener {
                            // 2. Registro en "cuidadores"
                            val datosCuidador = hashMapOf(
                                "idusuario" to uid,
                                "descripcion" to descripcion
                            )

                            db.collection("cuidadores").document(uid).set(datosCuidador)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT)
                                        .show()
                                    val intent = Intent(this, InicioCuidadorActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error al guardar cuidador",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Manejar el resultado del autocompletado
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            val direccion = place.address
            val latLng = place.latLng

            if (latLng != null) {
                autoCompletarDireccion.setText(direccion)
                this.latitud = latLng.latitude
                this.longitud = latLng.longitude
            }else{
                Toast.makeText(this, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}