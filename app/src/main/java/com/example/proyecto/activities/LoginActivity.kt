package com.example.proyecto.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var rememberMeCheckBox: CheckBox
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("com.example.proyecto", Context.MODE_PRIVATE)

        // Vinculación de vistas
        emailInputLayout = findViewById(R.id.textInputEmail)
        passwordInputLayout = findViewById(R.id.textInputPassword)
        rememberMeCheckBox = findViewById(R.id.checkboxRemember)
        loginButton = findViewById(R.id.buttonLogin)
        registerButton = findViewById(R.id.buttonRegister)

        // Inicialización de Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Intentar iniciar sesión automáticamente si hay credenciales guardadas
        if (!tryAutoLogin()) {
            setupUI()
        }
    }

    private fun setupUI() {
        // Configuración del listener del botón de login
        loginButton.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        // Configuración del listener del botón de registro
        registerButton.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun tryAutoLogin(): Boolean {
        val rememberedEmail = sharedPreferences.getString("email", null)
        val rememberedPassword = sharedPreferences.getString("password", null)

        if (rememberedEmail != null && rememberedPassword != null) {
            auth.signInWithEmailAndPassword(rememberedEmail, rememberedPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        navigateToMainActivity()
                    } else {
                        // Si falla el inicio de sesión automático, mostrar la pantalla de login
                        setContentView(R.layout.activity_login)
                        setupUI()
                    }
                }
            return true
        }
        return false
    }

    private fun validateInputs(): Boolean {
        // Verificación de la entrada de correo electrónico
        val email = emailInputLayout.editText?.text.toString()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Ingresa un correo electrónico válido."
            return false
        } else {
            emailInputLayout.error = null
        }

        // Verificación de la entrada de la contraseña
        val password = passwordInputLayout.editText?.text.toString()
        if (password.isEmpty()) {
            passwordInputLayout.error = "La contraseña no puede estar vacía."
            return false
        } else {
            passwordInputLayout.error = null
        }

        return true
    }

    private fun performLogin() {
        val email = emailInputLayout.editText?.text.toString()
        val password = passwordInputLayout.editText?.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (rememberMeCheckBox.isChecked) {
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("password", password)
                        editor.apply()
                    } else {
                        sharedPreferences.edit().clear().apply()
                    }

                    navigateToMainActivity()
                } else {
                    Toast.makeText(
                        baseContext, "Autenticación fallida, comprueba los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val specificUserEmail = "admin@gmail.com"

        if (user != null && user.email == specificUserEmail) {
            // Navegar a NewDiscoActivity si el usuario es el específico
            val intent = Intent(this, NewDiscoActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Navegar a MainActivity para todos los demás usuarios
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
