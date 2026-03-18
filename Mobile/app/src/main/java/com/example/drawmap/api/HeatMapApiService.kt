package com.example.drawmap.api

import retrofit2.http.GET

interface HeatMapApiService {
    @GET("api/heatmap")
    suspend fun getHeatMap(): List<HeatMapDto>
}
