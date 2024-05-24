package com.example.proyecto.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.Disco
import com.example.proyecto.DiscoAdapter
import com.example.proyecto.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MainActivity : AppCompatActivity() {

    private lateinit var textViewGreeting: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiscoAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var viewMapButton: Button
    private lateinit var textViewResults: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firestore y FusedLocationProviderClient
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewDiscos)
        adapter = DiscoAdapter(mutableListOf()) // Lista vacía inicialmente
        layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        // Configurar TextView y ProgressBar
        textViewGreeting = findViewById(R.id.textViewGreeting)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        viewMapButton = findViewById(R.id.buttonViewMap)
        bottomNavigationView = findViewById(R.id.navigation)
        textViewResults = findViewById(R.id.textViewResults)

        // Mostrar el ProgressBar mientras cargan los datos
        loadingIndicator.visibility = ProgressBar.VISIBLE

        // Cargar el nombre del usuario y configurar el saludo
        loadUserName()

        // Verificar y solicitar permisos de ubicación
        checkLocationPermission()

        // Configurar el botón para ver el mapa
        viewMapButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        // Configurar el menú
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_search -> {
                    loadDiscos()
                    true
                }

                R.id.navigation_saved -> {
                    val intent = Intent(this, FavsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.navigation_points -> {
                    val intent = Intent(this, RatingActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    private fun loadUserName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("name") ?: "Usuario"
                    textViewGreeting.text =
                        "Hola $userName, estas son las discotecas basadas en tu ubicación."
                    loadingIndicator.visibility = ProgressBar.GONE
                }
                .addOnFailureListener {
                    textViewGreeting.text =
                        "Hola Usuario, estas son las discotecas basadas en tu ubicación."
                    loadingIndicator.visibility = ProgressBar.GONE
                }
        } else {
            textViewGreeting.text =
                "Hola Usuario, estas son las discotecas basadas en tu ubicación."
            loadingIndicator.visibility = ProgressBar.GONE
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No se tienen permisos de ubicación, solicitarlos al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permisos de ubicación ya otorgados
            loadDiscos()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permisos de ubicación otorgados
                loadDiscos()
            } else {
                // Permisos de ubicación no otorgados
                textViewResults.text = "Permisos de ubicación no otorgados"
                loadingIndicator.visibility = ProgressBar.GONE
            }
        }
    }

    private fun loadDiscos() {
        // Obtener la ubicación actual del usuario
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Cargar discotecas desde Firestore
                    firestore.collection("Discotecas")
                        .get()
                        .addOnSuccessListener { result ->
                            val discosList = mutableListOf<Pair<Disco, Float>>()
                            for (document in result) {
                                val id = document.id
                                val name = document.getString("name") ?: ""
                                val imageURL = document.getString("imageURL")
                                    ?: "" // Usa "imageURL" consistentemente
                                val description = document.getString("description") ?: ""
                                val geoPoint =
                                    document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0)

                                // Verificar los datos obtenidos de Firestore
                                Log.d(
                                    "FirestoreData",
                                    "ID: $id, Name: $name, ImageURL: $imageURL, Description: $description"
                                )

                                // Calcular la distancia entre la ubicación actual y la discoteca
                                val discoLocation = Location("").apply {
                                    latitude = geoPoint.latitude
                                    longitude = geoPoint.longitude
                                }
                                val distance = location.distanceTo(discoLocation)

                                val disco = Disco(id, name, imageURL, description, geoPoint, "")
                                discosList.add(Pair(disco, distance))
                            }
                            // Establecer un umbral de distancia máxima (por ejemplo, 5000 metros)
                            val maxDistance = 50000 // 5 km
                            val filteredList = discosList.filter { it.second <= maxDistance }

                            // Ordenar la lista por distancia
                            val sortedList = filteredList.sortedBy { it.second }

                            // Actualizar el adaptador con la lista ordenada
                            adapter.updateDiscos(sortedList.map { it.first })
                            textViewResults.text = "${sortedList.size} resultados"
                            loadingIndicator.visibility = ProgressBar.GONE

                            // Debugging: log the distances
                            for ((disco, distance) in sortedList) {
                                Log.d("DiscoDistance", "${disco.name}: $distance meters")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreError", "Error getting documents: ", exception)
                            textViewResults.text = "Error cargando discotecas"
                            loadingIndicator.visibility = ProgressBar.GONE
                        }
                } else {
                    // Manejar el caso donde no se puede obtener la ubicación
                    textViewResults.text = "No se pudo obtener la ubicación actual"
                    loadingIndicator.visibility = ProgressBar.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LocationError", "Error getting location: ", exception)
                textViewResults.text = "Error obteniendo la ubicación"
                loadingIndicator.visibility = ProgressBar.GONE
            }
    }
}
