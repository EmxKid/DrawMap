package com.example.drawmap.ui.utils

import android.graphics.*
import org.osmdroid.util.GeoPoint
import kotlin.math.max
import kotlin.math.min

/**
 * Утилита для рендеринга векторных изображений карт без использования тайлов
 * Используется как fallback когда реальные тайлы недоступны
 */
object MapFallbackRenderer {

    /**
     * Рендерит маршрут на векторной карте-заглушке
     * 
     * @param points Точки маршрута
     * @param width Ширина изображения в пикселях
     * @param height Высота изображения в пикселях
     * @param includeRiver Добавить реку на карту
     * @param includePark Добавить парк на карту
     * @return Bitmap с отрендеренной картой
     */
    fun renderRouteOnMap(
        points: List<GeoPoint>,
        width: Int = 800,
        height: Int = 600,
        includeRiver: Boolean = true,
        includePark: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Базовый фон
        canvas.drawColor(Color.parseColor("#F2EFEA"))

        // Рисуем сетку улиц (блоки зданий)
        drawCityBlocks(canvas, width, height)

        // Рисуем парк если нужен
        if (includePark) {
            drawPark(canvas, width, height)
        }

        // Рисуем реку если нужна
        if (includeRiver) {
            drawRiver(canvas, width, height)
        }

        // Рисуем маршрут поверх карты
        if (points.isNotEmpty()) {
            drawRoute(canvas, points, width, height)
        }

        return bitmap
    }

    /**
     * Рисует блоки зданий (имитация улиц)
     */
    private fun drawCityBlocks(canvas: Canvas, width: Int, height: Int) {
        val blockPaint = Paint().apply {
            color = Color.parseColor("#EDE6DF")
            style = Paint.Style.FILL
        }
        val strokePaint = Paint().apply {
            color = Color.parseColor("#DDD6CF")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val cols = (width / 150).coerceAtLeast(4)
        val rows = (height / 100).coerceAtLeast(4)
        val padX = (width * 0.04).toInt()
        val padY = (height * 0.04).toInt()
        val cellW = (width - padX * 2) / cols
        val cellH = (height - padY * 2) / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val offsetX = if ((r + c) % 2 == 0) 6 else 0
                val offsetY = if ((r + c) % 3 == 0) 4 else 0
                val left = padX + c * cellW + offsetX
                val top = padY + r * cellH + offsetY
                val rect = RectF(
                    left.toFloat(),
                    top.toFloat(),
                    (left + cellW - 8).toFloat(),
                    (top + cellH - 8).toFloat()
                )
                canvas.drawRoundRect(rect, 8f, 8f, blockPaint)
                canvas.drawRoundRect(rect, 8f, 8f, strokePaint)
            }
        }
    }

    /**
     * Рисует парк
     */
    private fun drawPark(canvas: Canvas, width: Int, height: Int) {
        val parkPaint = Paint().apply {
            color = Color.parseColor("#DFF2D8")
        }
        val parkRect = RectF(
            (width * 0.08).toFloat(),
            (height * 0.18).toFloat(),
            (width * 0.45).toFloat(),
            (height * 0.5).toFloat()
        )
        canvas.drawRoundRect(parkRect, 30f, 30f, parkPaint)
    }

    /**
     * Рисует реку
     */
    private fun drawRiver(canvas: Canvas, width: Int, height: Int) {
        val riverPaint = Paint().apply {
            color = Color.parseColor("#C7EAFB")
            style = Paint.Style.STROKE
            strokeWidth = (height * 0.06).toFloat()
            isAntiAlias = true
        }
        
        val riverPath = Path().apply {
            moveTo((width * 0.6).toFloat(), (height * 0.1).toFloat())
            quadTo(
                (width * 0.75).toFloat(),
                (height * 0.35).toFloat(),
                (width * 0.6).toFloat(),
                (height * 0.6).toFloat()
            )
            quadTo(
                (width * 0.45).toFloat(),
                (height * 0.85).toFloat(),
                (width * 0.8).toFloat(),
                (height * 0.9).toFloat()
            )
        }
        canvas.drawPath(riverPath, riverPaint)
    }

    /**
     * Рисует маршрут на карте
     */
    private fun drawRoute(canvas: Canvas, points: List<GeoPoint>, width: Int, height: Int) {
        // Вычисляем границы маршрута
        var minLat = points[0].latitude
        var maxLat = points[0].latitude
        var minLon = points[0].longitude
        var maxLon = points[0].longitude

        for (p in points) {
            minLat = min(minLat, p.latitude)
            maxLat = max(maxLat, p.latitude)
            minLon = min(minLon, p.longitude)
            maxLon = max(maxLon, p.longitude)
        }

        // Добавляем отступы
        val latPad = (maxLat - minLat) * 0.1 + 1e-6
        val lonPad = (maxLon - minLon) * 0.1 + 1e-6
        minLat -= latPad
        maxLat += latPad
        minLon -= lonPad
        maxLon += lonPad

        // Функции преобразования координат
        fun xOf(lon: Double) = ((lon - minLon) / (maxLon - minLon) * (width - 64) + 32).toFloat()
        fun yOf(lat: Double) = ((maxLat - lat) / (maxLat - minLat) * (height - 64) + 32).toFloat()

        // Рисуем линию маршрута
        val pathPaint = Paint().apply {
            color = Color.parseColor("#7C4DFF")
            style = Paint.Style.STROKE
            strokeWidth = (height * 0.015).toFloat().coerceAtLeast(6f)
            isAntiAlias = true
        }

        val path = Path()
        for ((i, p) in points.withIndex()) {
            val x = xOf(p.longitude)
            val y = yOf(p.latitude)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, pathPaint)

        // Рисуем стартовую точку
        val startX = xOf(points.first().longitude)
        val startY = yOf(points.first().latitude)
        val markerRadius = (height * 0.015).toFloat().coerceAtLeast(8f)
        
        canvas.drawCircle(
            startX,
            startY,
            markerRadius,
            Paint().apply { color = Color.parseColor("#FFD54F") }
        )
        canvas.drawCircle(
            startX,
            startY,
            markerRadius,
            Paint().apply {
                color = Color.parseColor("#E6E0FF")
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
        )
    }

    /**
     * Проверяет, является ли bitmap в основном зеленым (признак незагруженных тайлов)
     */
    fun isMostlyGreen(bitmap: Bitmap, sampleSize: Int = 8): Boolean {
        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, false)
        var greenCount = 0
        var total = 0

        for (y in 0 until sampledBitmap.height) {
            for (x in 0 until sampledBitmap.width) {
                val pixel = sampledBitmap.getPixel(x, y)
                val green = Color.green(pixel)
                val red = Color.red(pixel)
                val blue = Color.blue(pixel)
                
                if (green > 200 && red < 100 && blue < 120) {
                    greenCount++
                }
                total++
            }
        }

        sampledBitmap.recycle()
        return if (total == 0) true else (greenCount.toDouble() / total) > 0.6
    }
}
