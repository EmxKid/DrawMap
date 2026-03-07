package com.example.drawmap.data.model

data class Route(
    val id: String,
    val name: String,
    val coordinates: List<Location>,
    val photoIds: List<String>,
    val createdAt: String
)