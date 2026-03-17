package com.example.drawmap.utils

import org.osmdroid.util.GeoPoint
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object RouteBuilder {

    fun buildRouteAlongRoads(
        from: GeoPoint,
        to: GeoPoint,
        callback: (List<GeoPoint>?) -> Unit
    ) {
        thread {
            try {
                // OSRM API endpoint
                val urlString = "https://router.project-osrm.org/route/v1/foot/" +
                        "${from.longitude},${from.latitude};" +
                        "${to.longitude},${to.latitude}" +
                        "?overview=full&geometries=geojson"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Парсим ответ и получаем координаты
                    val routePoints = parseOSRMResponse(response)
                    callback(routePoints)
                } else {
                    callback(null)
                }
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }

    private fun parseOSRMResponse(response: String): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()

        try {
            // Ищем координаты в JSON ответе
            // Формат: "coordinates":[[lon,lat],[lon,lat],...]
            val coordinatesStart = response.indexOf("\"coordinates\":[[")
            if (coordinatesStart == -1) return points

            val coordinatesEnd = response.indexOf("]]", coordinatesStart)
            if (coordinatesEnd == -1) return points

            val coordinatesString = response.substring(
                coordinatesStart + 15,
                coordinatesEnd + 1
            )

            // Разбираем каждую пару [lon,lat]
            val coordPairs = coordinatesString.split("],[")
            for (pair in coordPairs) {
                val cleanPair = pair.replace("[", "").replace("]", "")
                val parts = cleanPair.split(",")
                if (parts.size >= 2) {
                    val lon = parts[0].toDoubleOrNull() ?: continue
                    val lat = parts[1].toDoubleOrNull() ?: continue
                    points.add(GeoPoint(lat, lon))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return points
    }
}