package com.example.drawmap.di

import com.example.drawmap.data.repository.GalleryRepository
import com.example.drawmap.data.repository.HeatmapRepository
import com.example.drawmap.data.repository.RouteRepository
import com.example.drawmap.data.repository.api.ApiHeatmapRepository
import com.example.drawmap.data.repository.api.ApiRouteRepository
import com.example.drawmap.data.repository.mock.MockGalleryRepository
import com.example.drawmap.data.repository.mock.MockHeatmapRepository
import com.example.drawmap.data.repository.mock.MockRouteRepository

object ServiceLocator {

    // ========== API репозитории ==========
    
    private val apiRouteRepository: RouteRepository by lazy { 
        ApiRouteRepository() 
    }
    
    private val apiHeatmapRepository: HeatmapRepository by lazy { 
        ApiHeatmapRepository() 
    }

    // ========== Mock репозитории ==========
    
    private val mockRouteRepository: RouteRepository by lazy { 
        MockRouteRepository() 
    }
    
    private val mockHeatmapRepository: HeatmapRepository by lazy { 
        MockHeatmapRepository() 
    }
    
    private val mockGalleryRepository: GalleryRepository by lazy { 
        MockGalleryRepository() 
    }

    // ========== Публичные методы доступа ==========

    /**
     * Возвращает репозиторий маршрутов в зависимости от режима
     */
    fun provideRouteRepository(): RouteRepository {
        return if (AppModeManager.isOnline()) {
            apiRouteRepository
        } else {
            mockRouteRepository
        }
    }
    
    /**
     * Возвращает репозиторий тепловой карты в зависимости от режима
     */
    fun provideHeatmapRepository(): HeatmapRepository {
        return if (AppModeManager.isOnline()) {
            apiHeatmapRepository
        } else {
            mockHeatmapRepository
        }
    }
    
    /**
     * Возвращает репозиторий галереи
     * Пока работает только с моками
     */
    fun provideGalleryRepository(): GalleryRepository {
        return mockGalleryRepository
    }
    
    // ========== Устаревшие свойства (для обратной совместимости) ==========
    
    /**
     * @deprecated Используйте provideRouteRepository() вместо прямого доступа
     */
    @Deprecated(
        message = "Use provideRouteRepository() instead",
        replaceWith = ReplaceWith("provideRouteRepository()")
    )
    val routeRepository: RouteRepository
        get() = provideRouteRepository()
    
    /**
     * @deprecated Используйте provideHeatmapRepository() вместо прямого доступа
     */
    @Deprecated(
        message = "Use provideHeatmapRepository() instead",
        replaceWith = ReplaceWith("provideHeatmapRepository()")
    )
    val heatmapRepository: HeatmapRepository
        get() = provideHeatmapRepository()
    
    /**
     * @deprecated Используйте provideGalleryRepository() вместо прямого доступа
     */
    @Deprecated(
        message = "Use provideGalleryRepository() instead",
        replaceWith = ReplaceWith("provideGalleryRepository()")
    )
    val galleryRepository: GalleryRepository
        get() = provideGalleryRepository()
    
    // ========== Утилитарные методы ==========
    
    /**
     * Очистить все кеши (если они есть в репозиториях)
     */
    fun clearCaches() {
        // TODO: Добавить очистку кешей в репозиториях при необходимости
    }
}
