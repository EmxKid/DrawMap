package com.example.drawmap.data.model

data class Photo(
    val id: String,
    val routeId: String,
    val imageUrl: String,
    val location: Location? = null
)