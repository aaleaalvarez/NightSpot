package com.example.proyecto.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class NewDiscoActivity : AppCompatActivity() {

    // Declaración de vistas
    private lateinit var nameEditText: TextInputEditText
    private lateinit var codeEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var imageUrlEditText: TextInputEditText
    private lateinit var latitudeEditText: TextInputEditText
    private lateinit var longitudeEditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_disco)

        // Vinculación de las vistas
        nameEditText = findViewById(R.id.editTextDiscoName)
        codeEditText = findViewById(R.id.editTextDiscoCode)
        descriptionEditText = findViewById(R.id.editTextDiscoDescription)
        imageUrlEditText = findViewById(R.id.editTextImageUrl)
        latitudeEditText = findViewById(R.id.editTextLatitude)
        longitudeEditText = findViewById(R.id.editTextLongitude)
        saveButton = findViewById(R.id.buttonSaveDisco)
        cancelButton = findViewById(R.id.buttonCancelDisco)

        // Configuración de los listeners de los botones
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveDiscoInfo()
            } else {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()  // Cierra la actividad sin guardar
        }
    }

    // Verifica que todos los campos tengan datos
    private fun validateInputs(): Boolean {
        return nameEditText.text.toString().isNotEmpty() &&
                codeEditText.text.toString().isNotEmpty() &&
                descriptionEditText.text.toString().isNotEmpty() &&
                imageUrlEditText.text.toString().isNotEmpty() &&
                latitudeEditText.text.toString().isNotEmpty() &&
                longitudeEditText.text.toString().isNotEmpty()
    }

    // Guarda la información de la discoteca en Firestore
    private fun saveDiscoInfo() {
        val name = nameEditText.text.toString()
        val code = codeEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val imageUrl = imageUrlEditText.text.toString()
        val latitude = latitudeEditText.text.toString().toDouble()
        val longitude = longitudeEditText.text.toString().toDouble()

        // Instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        // Crea el documento con los datos de la discoteca
        val discoInfo = hashMapOf(
            "name" to name,
            "code" to code,
            "description" to description,
            "imageURL" to imageUrl,
            "location" to GeoPoint(latitude, longitude)
        )

        // Guarda el documento en la colección 'Discotecas'
        db.collection("Discotecas").add(discoInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Discoteca guardada correctamente.", Toast.LENGTH_SHORT).show()
                clearInputs()  // Limpia los campos después de guardar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    // Limpia los campos de entrada
    private fun clearInputs() {
        nameEditText.text?.clear()
        codeEditText.text?.clear()
        descriptionEditText.text?.clear()
        imageUrlEditText.text?.clear()
        latitudeEditText.text?.clear()
        longitudeEditText.text?.clear()
    }
}

