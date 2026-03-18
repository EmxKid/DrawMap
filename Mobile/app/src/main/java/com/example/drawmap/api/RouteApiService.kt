package com.example.drawmap.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RouteApiService {
    @POST("api/route")
    suspend fun addRoute(@Body route: RouteDto): RouteDto

    @GET("api/route/{routeId}")
    suspend fun getRoute(@Path("routeId") routeId: String): RouteDto

    @DELETE("api/route/{routeId}")
    suspend fun deleteRoute(@Path("routeId") routeId: String): Boolean

    @PUT("api/route/{routeId}")
    suspend fun updateRoute(@Path("routeId") routeId: String, @Body route: RouteDto): Boolean
}
