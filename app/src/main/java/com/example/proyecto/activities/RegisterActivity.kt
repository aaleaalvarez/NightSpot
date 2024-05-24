package com.example.proyecto.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var nameInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicialización Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Vinculación de vistas
        nameInputLayout = findViewById(R.id.textInputNameLayout)
        emailInputLayout = findViewById(R.id.textInputEmailRegisterLayout)
        passwordInputLayout = findViewById(R.id.textInputPasswordRegisterLayout)
        val registerButton: Button = findViewById(R.id.buttonRegisterUser)
        val goToLoginButton: Button = findViewById(R.id.buttonGoToLogin)

        registerButton.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        goToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val email = emailInputLayout.editText?.text.toString()
        val password = passwordInputLayout.editText?.text.toString()
        val name = nameInputLayout.editText?.text.toString()

        var valid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Ingrese un correo electrónico válido."
            valid = false
        } else {
            emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "La contraseña no puede estar vacía."
            valid = false
        } else {
            passwordInputLayout.error = null
        }

        if (name.isEmpty()) {
            nameInputLayout.error = "El nombre no puede estar vacío."
            valid = false
        } else {
            nameInputLayout.error = null
        }

        return valid
    }

    private fun performRegistration() {
        val email = emailInputLayout.editText?.text.toString()
        val password = passwordInputLayout.editText?.text.toString()
        val name = nameInputLayout.editText?.text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Registro exitoso y nombre de usuario guardado.",
                            Toast.LENGTH_SHORT
                        ).show()
                        saveUserInformation(
                            user.uid,
                            name,
                            email
                        ) // Guarda la información extra en Firestore
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Error al guardar el nombre de usuario.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    baseContext,
                    "Registro fallido: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveUserInformation(userId: String, name: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "name" to name,
            "email" to email
        )

        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Información del usuario guardada en Firestore.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al guardar en Firestore: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
