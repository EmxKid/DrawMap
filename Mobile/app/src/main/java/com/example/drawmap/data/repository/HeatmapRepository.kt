package com.example.drawmap.data.repository

import com.example.drawmap.ui.heatmap.HeatmapPoint

interface HeatmapRepository {
    suspend fun getHeatmapData(): List<HeatmapPoint>
}