package com.example.drawmap.data.repository.mock

import com.example.drawmap.R
import com.example.drawmap.data.model.Route
import com.example.drawmap.data.repository.RouteRepository
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.delay

class MockRouteRepository : RouteRepository {
    private val sampleRoutes: List<Route>

    init {
        val resUri = "android.resource://com.example.drawmap/" + R.drawable.ic_launcher_background

        fun computeDistance(points: List<GeoPoint>): Double {
            var sum = 0.0
            for (i in 0 until points.size - 1) {
                sum += points[i].distanceToAsDouble(points[i + 1])
            }
            return sum
        }

        val r1Points = listOf(
            GeoPoint(55.7558, 37.6173),
            GeoPoint(55.7560, 37.6180),
            GeoPoint(55.7565, 37.6190)
        )
        val r2Points = listOf(
            GeoPoint(55.7500, 37.6100),
            GeoPoint(55.7510, 37.6110),
            GeoPoint(55.7520, 37.6120)
        )
        val r3Points = listOf(
            GeoPoint(55.7600, 37.6200),
            GeoPoint(55.7610, 37.6210),
            GeoPoint(55.7620, 37.6220)
        )

        val r1dist = computeDistance(r1Points)
        val r2dist = computeDistance(r2Points)
        val r3dist = computeDistance(r3Points)

        fun estDurationForMeters(meters: Double): Long {
            val speed = 1.3888889
            return (meters / speed).toLong()
        }

        sampleRoutes = listOf(
            Route(
                id = "r1",
                title = "Morning Walk",
                coordinates = r1Points,
                photoUris = listOf(resUri),
                distanceMeters = r1dist,
                durationSeconds = estDurationForMeters(r1dist)
            ),
            Route(
                id = "r2",
                title = "Park Loop",
                coordinates = r2Points,
                photoUris = listOf(resUri),
                distanceMeters = r2dist,
                durationSeconds = estDurationForMeters(r2dist)
            ),
            Route(
                id = "r3",
                title = "Evening Stroll",
                coordinates = r3Points,
                photoUris = listOf(resUri),
                distanceMeters = r3dist,
                durationSeconds = estDurationForMeters(r3dist)
            )
        )
    }



    override suspend fun getRouteIdsForUser(userId: String): List<String> {
        delay(50)
        return sampleRoutes.map { it.id }
    }

    override suspend fun getRouteById(id: String): Route? {
        delay(100)
        return sampleRoutes.find { it.id == id }
    }
}
