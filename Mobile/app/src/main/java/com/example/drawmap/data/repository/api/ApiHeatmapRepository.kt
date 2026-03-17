package com.example.drawmap.data.repository.api

import com.example.drawmap.api.ApiClient
import com.example.drawmap.data.common.Result
import com.example.drawmap.data.repository.HeatmapRepository
import com.example.drawmap.ui.heatmap.HeatmapPoint
import org.osmdroid.util.GeoPoint

class ApiHeatmapRepository : HeatmapRepository {
    
    private val apiService = ApiClient.heatMapApiService

    override suspend fun getHeatmapData(): List<HeatmapPoint> {
        return try {
            val heatMapDtos = apiService.getHeatMap()
            
            // Маппинг DTO -> HeatmapPoint
            heatMapDtos.map { dto ->
                HeatmapPoint(
                    location = GeoPoint(dto.location.latitude, dto.location.longitude),
                    intensity = dto.visitFrequency.toFloat(),
                    timestamp = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getHeatmapDataResult(): Result<List<HeatmapPoint>> {
        return Result.suspendRunCatching {
            val heatMapDtos = apiService.getHeatMap()
            
            // Маппинг DTO -> HeatmapPoint
            heatMapDtos.map { dto ->
                HeatmapPoint(
                    location = GeoPoint(dto.location.latitude, dto.location.longitude),
                    intensity = dto.visitFrequency.toFloat(),
                    timestamp = null
                )
            }
        }
    }
}
