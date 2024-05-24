package com.example.proyecto.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto.R
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InfoDiscoActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null

    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_disco)

        firestore = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        userId = user?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val name = intent.getStringExtra("NAME")
        val description = intent.getStringExtra("DESCRIPTION")
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val id = intent.getStringExtra("ID")
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)

        val textViewName: TextView = findViewById(R.id.tvDiscotecaName)
        val textViewDescription: TextView = findViewById(R.id.tvDescription)
        val imageView: ImageView = findViewById(R.id.imageViewPlaceholder)
        btnSave = findViewById(R.id.btnGuardar)
        val btnGo: Button = findViewById(R.id.btnGo)

        textViewName.text = name
        textViewDescription.text = description
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(imageView)
        }

        checkIfFavoriteAndSetButtonText(id)

        btnSave.setOnClickListener {
            if (btnSave.text == "Guardar") {
                if (id != null && name != null && imageUrl != null && description != null) {
                    saveFavorite(id, name, imageUrl, description)
                } else {
                    Toast.makeText(this, "Información incompleta", Toast.LENGTH_SHORT).show()
                }
            } else {
                removeFavorite(id)
            }
        }

        btnGo.setOnClickListener {
            openGoogleMaps(LatLng(latitude, longitude))
        }
    }

    private fun checkIfFavoriteAndSetButtonText(discoId: String?) {
        if (discoId == null) return

        firestore.collection("users").document(userId!!)
            .collection("favorites").document(discoId)
            .get()
            .addOnSuccessListener { document ->
                btnSave.text = if (document.exists()) "Quitar" else "Guardar"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveFavorite(id: String, name: String, imageUrl: String, description: String) {
        val favorite = hashMapOf(
            "id" to id,
            "name" to name,
            "imageUrl" to imageUrl,
            "description" to description
        )

        firestore.collection("users").document(userId!!)
            .collection("favorites").document(id)
            .set(favorite)
            .addOnSuccessListener {
                Toast.makeText(this, "Guardado en favoritos!", Toast.LENGTH_SHORT).show()
                btnSave.text = "Quitar"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun removeFavorite(id: String?) {
        if (id == null) return

        firestore.collection("users").document(userId!!)
            .collection("favorites").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Eliminado de favoritos!", Toast.LENGTH_SHORT).show()
                btnSave.text = "Guardar"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openGoogleMaps(discoLocation: LatLng) {
        val gmmIntentUri =
            Uri.parse("google.navigation:q=${discoLocation.latitude},${discoLocation.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_LONG).show()
        }
    }
}
