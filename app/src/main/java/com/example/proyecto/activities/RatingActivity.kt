package com.example.proyecto.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.proyecto.R
import com.example.proyecto.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class RatingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var textViewPoints: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rating)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtener referencias a los elementos de la UI
        val editTextCode: EditText = findViewById(R.id.editTextCode)
        val ratingBar: RatingBar = findViewById(R.id.ratingBar)
        val buttonSend: Button = findViewById(R.id.buttonSend)
        val buttonCancel: Button = findViewById(R.id.buttonCancel)
        bottomNavigationView = findViewById(R.id.navigation)
        textViewPoints = findViewById(R.id.textViewPoints)

        // Cargar los puntos actuales del usuario
        loadUserPoints()

        buttonSend.setOnClickListener {
            val code = editTextCode.text.toString().trim()
            val rating = ratingBar.rating
            getLocationAndVerify(code, rating)
        }

        buttonCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Configurar el menú
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_search -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_saved -> {
                    val intent = Intent(this, FavsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_points -> {
                    loadUserPoints()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadUserPoints() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userData = document.toObject(User::class.java)
                        if (userData != null) {
                            updatePointsTextView(userData.points)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al cargar los puntos: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("RatingActivity", "Error al cargar los puntos", e)
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePointsTextView(points: Int) {
        textViewPoints.text = points.toString()
    }

    private fun getLocationAndVerify(code: String, rating: Float) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                verifyAndSaveRating(code, rating, location)
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verifyAndSaveRating(code: String, rating: Float, userLocation: Location) {
        if (code.isEmpty()) {
            Toast.makeText(this, "El código no puede estar vacío", Toast.LENGTH_LONG).show()
            return
        }

        db.collection("Discotecas").whereEqualTo("code", code)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Código de discoteca incorrecto", Toast.LENGTH_LONG).show()
                } else {
                    val discoteca = documents.documents[0]
                    val geoPoint = discoteca.getGeoPoint("location")
                    if (geoPoint != null) {
                        val discotecaLocation = Location("").apply {
                            latitude = geoPoint.latitude
                            longitude = geoPoint.longitude
                        }
                        val distance = userLocation.distanceTo(discotecaLocation)
                        if (distance <= 1000) {
                            checkLastRatingTime(code, rating)
                        } else {
                            Toast.makeText(
                                this,
                                "Debes estar a menos de 1 km de la discoteca para valorar",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "No se pudo obtener la ubicación de la discoteca",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al verificar el código: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("RatingActivity", "Error al verificar el código", e)
            }
    }

    private fun checkLastRatingTime(code: String, rating: Float) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId)
                .collection("ratings")
                .whereEqualTo("code", code)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        saveRatingToFirestore(code, rating)
                    } else {
                        val lastRating = documents.documents[0]
                        val lastTimestamp = lastRating.getDate("timestamp") ?: Date(0)
                        val currentTime = Date()

                        val diff = currentTime.time - lastTimestamp.time
                        val hours = diff / (1000 * 60 * 60)

                        if (hours >= 24) {
                            saveRatingToFirestore(code, rating)
                        } else {
                            Toast.makeText(
                                this,
                                "Solo puedes valorar una vez cada 24 horas",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al verificar la última valoración: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("RatingActivity", "Error al verificar la última valoración", e)
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveRatingToFirestore(code: String, rating: Float) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val ratingData = mapOf(
                "code" to code,
                "rating" to rating,
                "timestamp" to Date()
            )
            db.collection("users").document(userId)
                .collection("ratings")
                .add(ratingData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Valoración guardada con éxito", Toast.LENGTH_LONG).show()
                    updateUserPoints()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al guardar valoración: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("RatingActivity", "Error al guardar valoración", e)
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUserPoints() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val newPoints = (snapshot.getLong("points") ?: 0L) + 10
                transaction.update(userRef, "points", newPoints)
                newPoints
            }.addOnSuccessListener { newPoints ->
                updatePointsTextView(newPoints.toInt())
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al actualizar los puntos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("RatingActivity", "Error al actualizar los puntos", e)
            }
        }
    }

    private fun enableEdgeToEdge() {
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
