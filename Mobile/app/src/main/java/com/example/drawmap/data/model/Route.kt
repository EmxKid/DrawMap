package com.example.drawmap.data.model

import org.osmdroid.util.GeoPoint

data class Route(
    val id: String,
    val title: String,
    val coordinates: List<GeoPoint>,
    val photoUris: List<String> = emptyList(),
    val durationSeconds: Long? = null,
    val distanceMeters: Double? = null
)
