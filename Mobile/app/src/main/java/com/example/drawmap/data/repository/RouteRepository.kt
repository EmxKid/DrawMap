package com.example.drawmap.data.repository

import com.example.drawmap.data.model.Route

interface RouteRepository {
    suspend fun getRouteIdsForUser(userId: String): List<String>
    suspend fun getRouteById(id: String): Route?
}

