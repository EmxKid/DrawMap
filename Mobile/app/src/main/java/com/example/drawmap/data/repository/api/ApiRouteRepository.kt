package com.example.drawmap.data.repository.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.drawmap.api.ApiClient
import com.example.drawmap.data.common.Result
import com.example.drawmap.data.mapper.RouteMapper.toDomainModel
import com.example.drawmap.data.mapper.RouteMapper.toDto
import com.example.drawmap.data.model.Route
import com.example.drawmap.data.repository.RouteRepository

/**
 * Реализация RouteRepository, которая обращается к API бэкенда
 */
class ApiRouteRepository : RouteRepository {
    
    private val apiService = ApiClient.routeApiService

    override suspend fun getRouteIdsForUser(userId: String): List<String> {
        return emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getRouteById(id: String): Route? {
        return try {
            val routeDto = apiService.getRoute(id)
            routeDto.toDomainModel()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRouteByIdResult(id: String): Result<Route> {
        return Result.suspendRunCatching {
            val routeDto = apiService.getRoute(id)
            routeDto.toDomainModel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createRoute(route: Route): Route? {
        return try {
            val routeDto = route.toDto()
            val createdDto = apiService.addRoute(routeDto)
            createdDto.toDomainModel()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createRouteResult(route: Route): Result<Route> {
        return Result.suspendRunCatching {
            val routeDto = route.toDto()
            val createdDto = apiService.addRoute(routeDto)
            createdDto.toDomainModel()
        }
    }

    suspend fun updateRoute(routeId: String, route: Route): Boolean {
        return try {
            val routeDto = route.toDto()
            apiService.updateRoute(routeId, routeDto)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateRouteResult(routeId: String, route: Route): Result<Boolean> {
        return Result.suspendRunCatching {
            val routeDto = route.toDto()
            apiService.updateRoute(routeId, routeDto)
        }
    }

    suspend fun deleteRoute(routeId: String): Boolean {
        return try {
            apiService.deleteRoute(routeId)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteRouteResult(routeId: String): Result<Boolean> {
        return Result.suspendRunCatching {
            apiService.deleteRoute(routeId)
        }
    }
}
