package com.example.drawmap.ui.heatmap

import android.graphics.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import androidx.core.graphics.createBitmap

class HeatmapOverlay(mapView: MapView) : Overlay() {

    private var points: List<HeatmapPoint> = emptyList()

    // НАСТРОЙКИ ВИЗУАЛА
    private val baseRadius = 60f  // Увеличен радиус для более заметных зон
    private val intensityMultiplier = 1.2f

    // ГРАДИЕНТ ОТ ЗЕЛЕНОГО (редкие) К КРАСНОМУ (частые)
    private val gradientColors = intArrayOf(
        Color.argb(0, 0, 255, 0),      // Прозрачный (края)
        Color.argb(100, 0, 255, 0),    // Зеленый полупрозрачный (редко посещаемые)
        Color.argb(140, 100, 255, 0),  // Желто-зеленый
        Color.argb(170, 255, 255, 0),  // Желтый (средняя посещаемость)
        Color.argb(200, 255, 150, 0),  // Оранжевый
        Color.argb(230, 255, 0, 0)     // Красный (часто посещаемые)
    )
    private val gradientPositions = floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f)

    // КЭШИРОВАНИЕ С ОТСЛЕЖИВАНИЕМ ПОЗИЦИИ КАРТЫ
    private var cachedBitmap: Bitmap? = null
    private var lastZoomLevel = -1.0
    private var lastMapCenter: GeoPoint? = null
    private var lastWidth = 0
    private var lastHeight = 0

    fun setPoints(points: List<HeatmapPoint>) {
        this.points = points
        clearCache()
    }

    fun clearCache() {
        cachedBitmap?.recycle()
        cachedBitmap = null
        lastZoomLevel = -1.0
        lastMapCenter = null
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow || points.isEmpty()) return

        val projection = mapView.projection
        val zoomLevel = mapView.zoomLevelDouble
        val mapCenter = mapView.mapCenter
        val width = mapView.width
        val height = mapView.height

        // ПРОВЕРЯЕМ: изменилась ли позиция карты достаточно для перерисовки
        val zoomChanged = Math.abs(zoomLevel - lastZoomLevel) > 0.1
        val centerChanged = lastMapCenter?.let {
            it.distanceToAsDouble(mapCenter) > 0.001  // ~100 метров
        } ?: true
        val sizeChanged = width != lastWidth || height != lastHeight

        if (zoomChanged || centerChanged || sizeChanged || cachedBitmap == null) {
            // Перерисовываем bitmap
            cachedBitmap?.recycle()
            cachedBitmap = renderHeatmapSimple(projection, width, height)
            lastZoomLevel = zoomLevel
            lastMapCenter = mapCenter as GeoPoint?
            lastWidth = width
            lastHeight = height
        }

        // Рисуем кэшированный bitmap
        cachedBitmap?.let { bitmap ->
            if (!bitmap.isRecycled) {
                canvas.drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }

    private fun renderHeatmapSimple(projection: Projection, width: Int, height: Int): Bitmap {
        val heatmapBitmap = createBitmap(width, height)
        val canvas = Canvas(heatmapBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (point in points) {
            val screenPoint = Point()
            projection.toPixels(point.location, screenPoint)
            val x = screenPoint.x.toFloat()
            val y = screenPoint.y.toFloat()

            // Пропускаем точки далеко за экраном
            if (x < -baseRadius * 3 || x > width + baseRadius * 3 ||
                y < -baseRadius * 3 || y > height + baseRadius * 3) continue

            val intensity = point.intensity * intensityMultiplier
            val radius = baseRadius * intensity

            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL

            val gradient = RadialGradient(
                x, y, radius,
                gradientColors,
                gradientPositions,
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient

            canvas.drawCircle(x, y, radius, paint)
        }

        return heatmapBitmap
    }
}

/**
 * Точка тепловой карты
 * @param location координаты точки
 * @param intensity интенсивность посещения (0.0 - 1.0), основана на visitFrequency
 */
data class HeatmapPoint(
    val location: GeoPoint,
    val intensity: Float = 1.0f
)