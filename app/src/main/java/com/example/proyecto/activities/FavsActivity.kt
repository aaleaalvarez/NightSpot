package com.example.proyecto.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.FavsAdapter
import com.example.proyecto.Local
import com.example.proyecto.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavsAdapter
    private lateinit var textViewSubtitle: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favs)

        // Obtener el ID del usuario autenticado
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Configurar el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSavedPlaces)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Referenciar el TextView
        textViewSubtitle = findViewById(R.id.textViewSubtitle)
        bottomNavigationView = findViewById(R.id.navigation)


        // Obtener los favoritos del usuario autenticado
        getFavorites(userId) { favorites ->
            Log.d("FavsActivity", "Fetched favorites: $favorites")
            adapter = FavsAdapter(favorites)
            recyclerView.adapter = adapter
            updateSubtitle(favorites.size)
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
                    refreshFavorites()
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

    private fun getFavorites(userId: String, callback: (List<Local>) -> Unit) {
        db.collection("users").document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val favorites = documents.mapNotNull { document ->
                    document.toObject(Local::class.java).takeIf { it.id.isNotEmpty() }
                }
                callback(favorites)
            }
            .addOnFailureListener { exception ->
                // Manejar el error adecuadamente, mostrar un mensaje al usuario
                Toast.makeText(
                    this,
                    "Error al obtener favoritos: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updateSubtitle(count: Int) {
        val subtitleText = "Has guardado $count locales."
        textViewSubtitle.text = subtitleText
    }

    private fun refreshFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            getFavorites(userId) { favorites ->
                Log.d("FavsActivity", "Fetched favorites: $favorites")
                adapter = FavsAdapter(favorites)
                recyclerView.adapter = adapter
                updateSubtitle(favorites.size)
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

