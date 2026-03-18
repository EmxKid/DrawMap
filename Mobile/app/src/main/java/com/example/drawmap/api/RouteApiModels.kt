package com.example.drawmap.api

import com.google.gson.annotations.SerializedName

/**
 * DTO для Location согласно бэкенду
 */
data class LocationDto(
    @SerializedName("id") val id: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timestamp") val timestamp: String?, // ISO 8601 формат
    @SerializedName("photo") val photo: PhotoDto?
)

/**
 * DTO для Photo согласно бэкенду
 */
data class PhotoDto(
    @SerializedName("id") val id: String?,
    @SerializedName("routeId") val routeId: String?,
    @SerializedName("locationId") val locationId: String?,
    @SerializedName("imageData") val imageData: String? // Base64 строка
)

/**
 * DTO для Route согласно бэкенду
 */
data class RouteDto(
    @SerializedName("id") val id: String?,
    @SerializedName("totalDistance") val totalDistance: Double?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("locations") val locations: List<LocationDto>?
)

/**
 * DTO для HeatMap согласно бэкенду
 */
data class HeatMapDto(
    @SerializedName("visitFrequency") val visitFrequency: Int,
    @SerializedName("location") val location: LocationDto
)
