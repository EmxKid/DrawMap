package com.example.drawmap.data.repository.mock

import com.example.drawmap.data.repository.HeatmapRepository
import com.example.drawmap.ui.heatmap.HeatmapPoint
import org.osmdroid.util.GeoPoint

class MockHeatmapRepository : HeatmapRepository {

    override suspend fun getHeatmapData(): List<HeatmapPoint> {
        val points = mutableListOf<HeatmapPoint>()

        // Зона 1: Центр Москвы (высокая интенсивность)
        for (i in 0..4) {
            points.add(HeatmapPoint(
                location = GeoPoint(
                    55.7558 + (Math.random() - 0.5) * 0.005,
                    37.6173 + (Math.random() - 0.5) * 0.005
                ),
                intensity = 0.8f + (Math.random() * 0.2).toFloat()
            ))
        }

        // Зона 2: Юго-запад (средняя интенсивность)
        for (i in 0..3) {
            points.add(HeatmapPoint(
                location = GeoPoint(
                    55.7310 + (Math.random() - 0.5) * 0.01,
                    37.6010 + (Math.random() - 0.5) * 0.01
                ),
                intensity = 0.6f + (Math.random() * 0.3).toFloat()
            ))
        }

        // Зона 3: Север (средне-низкая интенсивность)
        for (i in 0..2) {
            points.add(HeatmapPoint(
                location = GeoPoint(
                    55.8260 + (Math.random() - 0.5) * 0.015,
                    37.6410 + (Math.random() - 0.5) * 0.015
                ),
                intensity = 0.5f + (Math.random() * 0.3).toFloat()
            ))
        }

        // Зона 4: Запад (средне-высокая интенсивность)
        for (i in 0..2) {
            points.add(HeatmapPoint(
                location = GeoPoint(
                    55.7500 + (Math.random() - 0.5) * 0.008,
                    37.5900 + (Math.random() - 0.5) * 0.008
                ),
                intensity = 0.7f + (Math.random() * 0.2).toFloat()
            ))
        }

        return points
    }
}