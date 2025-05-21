package com.example.petcareapp

import android.content.Intent // Importa Intent para la navegación
import android.os.Bundle
import android.widget.Button // Importa Button
import android.widget.TextView // Importa TextView
import android.widget.Toast // Importa Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Encontrar las vistas por su ID ---
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val buttonRegister: Button = findViewById(R.id.buttonRegister)
        val textViewForgotPassword: TextView = findViewById(R.id.textViewForgotPassword)

        // --- Establecer OnClickListener para cada vista ---

        // OnClickListener para el botón "Iniciar sesión"
        buttonLogin.setOnClickListener {
            // Código para iniciar sesión (aún no implementado aquí)
            Toast.makeText(this, "Botón Iniciar sesión presionado", Toast.LENGTH_SHORT).show()
        }

        // OnClickListener para el botón "Registrarse"
        buttonRegister.setOnClickListener {
            // Código a ejecutar cuando se hace clic en "Registrarse"
            Toast.makeText(this, "Botón Registrarse presionado", Toast.LENGTH_SHORT).show()


            // Crear un Intent para navegar a ProfileTypeSelectionActivity
            val intent = Intent(this, RegistroTipoPerfilActivity::class.java)
            // Iniciar la nueva Activity
            startActivity(intent)
            // *** FIN DE LO QUE NECESITAS AGREGAR O MODIFICAR ***
        }

        // OnClickListener para el texto "Olvidé mi contraseña"
        textViewForgotPassword.setOnClickListener {
            // Código para manejar "Olvidé mi contraseña" (aún no implementado aquí)
            Toast.makeText(this, "Texto Olvidé mi contraseña presionado", Toast.LENGTH_SHORT).show()
        }
    }
}