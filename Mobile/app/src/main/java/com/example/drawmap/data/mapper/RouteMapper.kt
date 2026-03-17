package com.example.drawmap.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.drawmap.api.LocationDto
import com.example.drawmap.api.RouteDto
import com.example.drawmap.data.model.Route
import org.osmdroid.util.GeoPoint
import java.time.Duration
import java.time.Instant

/**
 * Mapper для преобразования между DTO и Domain моделями маршрутов
 */
object RouteMapper {
    @RequiresApi(Build.VERSION_CODES.O)
    fun RouteDto.toDomainModel(): Route {
        val geoPoints = locations?.map { 
            GeoPoint(it.latitude, it.longitude) 
        } ?: emptyList()
        
        // Используем расстояние из бэкенда или вычисляем локально
        val distance = totalDistance ?: calculateDistance(geoPoints)
        
        // Вычисляем длительность из startTime и endTime
        val duration = calculateDuration(startTime, endTime)
        
        return Route(
            id = id ?: "",
            title = "Route ${id?.take(8) ?: ""}",
            coordinates = geoPoints,
            photoUris = emptyList(), // TODO: конвертировать photos из DTO
            distanceMeters = distance,
            durationSeconds = duration
        )
    }

    fun Route.toDto(): RouteDto {
        val locationDtos = coordinates.map { 
            LocationDto(
                id = null,
                latitude = it.latitude,
                longitude = it.longitude,
                timestamp = null, // TODO: добавить timestamp к координатам
                photo = null
            )
        }
        
        return RouteDto(
            id = id.takeIf { it.isNotEmpty() },
            totalDistance = distanceMeters,
            startTime = null, // TODO: добавить время начала маршрута
            endTime = null, // TODO: добавить время окончания маршрута
            locations = locationDtos
        )
    }

    private fun calculateDistance(points: List<GeoPoint>): Double {
        var sum = 0.0
        for (i in 0 until points.size - 1) {
            sum += points[i].distanceToAsDouble(points[i + 1])
        }
        return sum
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDuration(startTime: String?, endTime: String?): Long? {
        if (startTime == null || endTime == null) return null
        
        return try {
            val start = Instant.parse(startTime)
            val end = Instant.parse(endTime)
            Duration.between(start, end).seconds
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
