package com.example.drawmap.di

import com.example.drawmap.data.repository.GalleryRepository
import com.example.drawmap.data.repository.HeatmapRepository
import com.example.drawmap.data.repository.MockGalleryRepository
import com.example.drawmap.data.repository.MockHeatmapRepository
import com.example.drawmap.data.repository.MockRouteRepository
import com.example.drawmap.data.repository.RouteRepository

/** Простой ServiceLocator для моков */
object ServiceLocator {
    // В будущем сюда можно подставлять реальные реализации
    val galleryRepository: GalleryRepository by lazy { MockGalleryRepository() }
    val routeRepository: RouteRepository by lazy { MockRouteRepository() }

    val heatmapRepository: HeatmapRepository by lazy { MockHeatmapRepository() }
}
