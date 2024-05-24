package com.example.proyecto

import com.google.firebase.firestore.GeoPoint

data class Disco(
    var id: String = "",
    var name: String = "",
    var imageURL: String = "",  // Usa "imageURL" consistentemente
    var description: String = "",
    var location: GeoPoint = GeoPoint(0.0, 0.0),
    var code: String = ""
)
