package com.example.drawmap.data.repositories

import com.example.drawmap.data.mock.MockData
import com.example.drawmap.data.model.Route
import kotlinx.coroutines.delay

class MockRouteRepository {

    suspend fun getRoutes(): Result<List<Route>> {
        delay(500) // Имитация сети
        return Result.success(MockData.mockRoutes)
    }

    suspend fun getRouteById(routeId: String): Result<Route?> {
        delay(300)
        val route = MockData.mockRoutes.find { it.id == routeId }
        return Result.success(route)
    }
}