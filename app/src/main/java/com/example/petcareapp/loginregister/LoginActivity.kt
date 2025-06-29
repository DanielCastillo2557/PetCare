package com.example.petcareapp.loginregister

import android.content.Intent // Importa Intent para la navegación
import android.os.Bundle
import android.widget.Button // Importa Button
import android.widget.EditText
import android.widget.TextView // Importa TextView
import android.widget.Toast // Importa Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.petcareapp.R
import com.example.petcareapp.cuidador.InicioCuidadorActivity
import com.example.petcareapp.duenio.InicioDuenioActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val campoEmail: EditText = findViewById(R.id.campoEmail)
        val campoContrasena: EditText = findViewById(R.id.campoContrasena)


        val buttonRegister: Button = findViewById(R.id.btnRegistrar)
        val textViewForgotPassword: TextView = findViewById(R.id.txtRecuperarContrasena)

        // --- Establecer OnClickListener para cada vista ---

        // OnClickListener para el botón "Iniciar sesión"
        btnLogin.setOnClickListener {
            // Código para iniciar sesión
            val email = campoEmail.text.toString()
            val contrasena = campoContrasena.text.toString()
            if (email.isBlank() || contrasena.isBlank()){
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
            else{
                loginUsuario(email, contrasena)
            }
        }

        // OnClickListener para el botón "Registrarse"
        buttonRegister.setOnClickListener {
            // Crear un Intent para navegar a ProfileTypeSelectionActivity
            val intent = Intent(this, RegistroTipoPerfilActivity::class.java)
            // Iniciar la nueva Activity
            startActivity(intent)
        }

        // OnClickListener para el texto "Olvidé mi contraseña"
        textViewForgotPassword.setOnClickListener {
            // Código para manejar "Olvidé mi contraseña" (aún no implementado aquí)
            Toast.makeText(this, "Texto Olvidé mi contraseña presionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUsuario(email: String, contrasena: String) {
        auth.signInWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener(this) { task -> if (task.isSuccessful) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                val db = FirebaseFirestore.getInstance()
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { document ->
                        val tipoPerfil = document.get("tipo") as? List<*>

                        when{
                            tipoPerfil?.contains("duenio") == true -> {
                                startActivity(Intent(this, InicioDuenioActivity::class.java))
                                finish()
                            }
                            tipoPerfil?.contains("cuidador") == true -> {
                                startActivity(Intent(this, InicioCuidadorActivity::class.java))
                                finish()
                            }
                            else -> {
                                Toast.makeText(this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            } else {
                val error = task.exception?.localizedMessage ?: "Error desconocido"
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}