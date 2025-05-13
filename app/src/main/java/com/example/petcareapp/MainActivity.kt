package com.example.petcareapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView // Importa TextView
import android.widget.Toast // Importa Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Esto ya lo tienes, maneja los bordes de la pantalla

        // Establece el layout para esta Activity
        setContentView(R.layout.activity_main)

        // Configuración para manejar las barras del sistema (esto ya lo tienes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Encontrar las vistas por su ID ---
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val buttonRegister: Button = findViewById(R.id.buttonRegister)
        val textViewForgotPassword: TextView = findViewById(R.id.textViewForgotPassword)

        

        // OnClickListener para el botón "Iniciar sesión"
        buttonLogin.setOnClickListener {
            // Aquí pones el código que se ejecutará cuando se haga clic en "Iniciar sesión"
            // Por ejemplo, puedes mostrar un mensaje Toast:
            Toast.makeText(this, "Botón Iniciar sesión presionado", Toast.LENGTH_SHORT).show()
            // O navegar a otra Activity para el inicio de sesión real
            // val loginIntent = Intent(this, LoginDetailActivity::class.java)
            // startActivity(loginIntent)
        }

        // OnClickListener para el botón "Registrarse"
        buttonRegister.setOnClickListener {
            // Aquí pones el código que se ejecutará cuando se haga clic en "Registrarse"
            Toast.makeText(this, "Botón Registrarse presionado", Toast.LENGTH_SHORT).show()
            // O navegar a una Activity de registro
            // val registerIntent = Intent(this, RegisterActivity::class.java)
            // startActivity(registerIntent)
        }

        // OnClickListener para el texto "Olvidé mi contraseña"
        textViewForgotPassword.setOnClickListener {
            // Aquí pones el código que se ejecutará cuando se haga clic en el texto
            Toast.makeText(this, "Texto Olvidé mi contraseña presionado", Toast.LENGTH_SHORT).show()
            // O navegar a una Activity para restablecer la contraseña
            // val forgotPasswordIntent = Intent(this, ForgotPasswordActivity::class.java)
            // startActivity(forgotPasswordIntent)
        }
    }
}