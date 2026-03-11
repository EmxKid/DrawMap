package com.example.drawmap.ui.route

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.R
import com.example.drawmap.data.model.Route
import com.example.drawmap.di.ServiceLocator
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class RouteDetailActivity : AppCompatActivity() {

    // FOR DEBUG: если поставить в true, всегда показываем векторный fallback вместо MapView
    private val forceFallback = false

    companion object {
        private const val EXTRA_ROUTE_ID = "extra_route_id"
        fun start(context: Context, routeId: String) {
            val i = Intent(context, RouteDetailActivity::class.java)
            i.putExtra(EXTRA_ROUTE_ID, routeId)
            context.startActivity(i)
        }
    }

    private lateinit var mapView: MapView
    private var currentRoute: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ensure osmdroid is configured
        Configuration.getInstance().userAgentValue = packageName
        // NOTE: don't call Configuration.load(...) here to avoid depending on androidx.preference

        setContentView(R.layout.activity_route_detail)

        mapView = findViewById(R.id.mapViewDetail)
        val tvOffline = findViewById<TextView>(R.id.tvOfflineDetail)
        val ivFallback = findViewById<ImageView>(R.id.ivMapFallback)

        // Если нет сети, показываем оффлайн-заглушку и прерываем инициализацию карты
        if (!hasNetwork()) {
            tvOffline.visibility = android.view.View.VISIBLE
            mapView.visibility = android.view.View.GONE
            ivFallback.visibility = android.view.View.GONE
        } else {
            tvOffline.visibility = android.view.View.GONE
            // скрываем mapView изначально — показываем fallback пока тайлы не подтвердятся
            mapView.visibility = android.view.View.GONE
            ivFallback.visibility = android.view.View.VISIBLE // показываем пока тайлы грузятся
            // set explicit tile source
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setMultiTouchControls(true)
            // neutral background while tiles load (avoid bright green)
            mapView.setBackgroundColor(android.graphics.Color.parseColor("#ECEFF1"))
        }

        val routeId = intent.getStringExtra(EXTRA_ROUTE_ID) ?: return

        // load route (mock) in coroutine
        lifecycleScope.launch {
            val route = ServiceLocator.routeRepository.getRouteById(routeId)
            if (route != null) {
                currentRoute = route
                if (hasNetwork()) showRouteOnMap(route)
            } else {
                Toast.makeText(this@RouteDetailActivity, "Route not found", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        findViewById<Button>(R.id.btnRepeat)?.setOnClickListener {
            // стартуем HomeActivity с extra, чтобы тот накрыл маршрут как "призрак"
            val i = Intent(this, com.example.drawmap.ui.home.HomeActivity::class.java)
            i.putExtra("repeat_route_id", routeId)
            startActivity(i)
            finish()
        }
    }

    private fun hasNetwork(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showRouteOnMap(route: Route) {
        val ivFallback = findViewById<ImageView>(R.id.ivMapFallback)

        // Если отладочный флаг включён, показываем сразу векторный fallback и не используем MapView
        if (forceFallback) {
            lifecycleScope.launch {
                try {
                    val fb = renderFallbackBitmap(route)
                    ivFallback.post {
                        ivFallback.setImageBitmap(fb)
                        ivFallback.visibility = android.view.View.VISIBLE
                        mapView.visibility = android.view.View.GONE
                    }
                } catch (_: Exception) {
                }
            }
            return
        }

        // generate fallback image in background and show immediately
        lifecycleScope.launch {
            try {
                val fb = renderFallbackBitmap(route)
                ivFallback.post {
                    ivFallback.setImageBitmap(fb)
                    ivFallback.visibility = android.view.View.VISIBLE
                }
            } catch (_: Exception) {
            }
        }

        val polyline = Polyline(mapView).apply {
            setPoints(route.coordinates)
            outlinePaint.color = 0xFF6200EE.toInt()
            outlinePaint.strokeWidth = 6f
            setOnClickListener { _, _, _ ->
                // открыть fullscreen
                RouteFullScreenActivity.start(this@RouteDetailActivity, route.id)
                true
            }
        }
        mapView.overlays.add(polyline)

        // photo markers
        route.photoUris.forEach { uri ->
            val m = Marker(mapView).apply {
                position = route.coordinates.firstOrNull() ?: return@apply
                setOnMarkerClickListener { _, _ ->
                    com.example.drawmap.ui.photo.PhotoActivity.start(this@RouteDetailActivity, uri)
                    true
                }
            }
            mapView.overlays.add(m)
        }

        // center
        if (route.coordinates.isNotEmpty()) {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(route.coordinates[0])
        }

        // проверяем через задержку — если tiles загружены, прячем fallback
        mapView.postDelayed({
            try {
                val bmp = android.graphics.Bitmap.createBitmap(
                    8,
                    8,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val c = android.graphics.Canvas(bmp)
                mapView.draw(c)
                // sample several pixels to decide
                var greenCount = 0
                var total = 0
                for (yy in 0 until bmp.height step 2) {
                    for (xx in 0 until bmp.width step 2) {
                        val px = bmp.getPixel(xx, yy)
                        val g = android.graphics.Color.green(px)
                        val r = android.graphics.Color.red(px)
                        val b = android.graphics.Color.blue(px)
                        if (g > 200 && r < 100 && b < 120) greenCount++
                        total++
                    }
                }
                val greenRatio = if (total == 0) 1.0 else greenCount.toDouble() / total.toDouble()
                if (greenRatio < 0.6) {
                    // tiles seem ok -> show map, hide fallback
                    ivFallback.visibility = android.view.View.GONE
                    mapView.visibility = android.view.View.VISIBLE
                } else {
                    // оставляем fallback видимым
                    ivFallback.visibility = android.view.View.VISIBLE
                    mapView.visibility = android.view.View.GONE
                }
            } catch (_: Exception) {
            }
        }, 2500)

        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        try {
            mapView.onResume()
        } catch (_: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mapView.onPause()
        } catch (_: Exception) {
        }
    }

    // simple fallback renderer (similar to Gallery's vector thumbnail but larger)
    private fun renderFallbackBitmap(
        route: Route,
        w: Int = 800,
        h: Int = 600
    ): android.graphics.Bitmap {
        val points = route.coordinates
        val bmp =
            android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.parseColor("#F2EFEA"))
        val blockPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#EDE6DF"); style =
            android.graphics.Paint.Style.FILL
        }
        val stroke = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#DDD6CF"); style =
            android.graphics.Paint.Style.STROKE; strokeWidth = 2f
        }
        val cols = 6
        val rows = 8
        val padX = (w * 0.04).toInt()
        val padY = (h * 0.04).toInt()
        val cellW = (w - padX * 2) / cols
        val cellH = (h - padY * 2) / rows
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = padX + c * cellW + (if ((r + c) % 2 == 0) 6 else 0)
                val top = padY + r * cellH + (if ((r + c) % 3 == 0) 4 else 0)
                val rect = android.graphics.RectF(
                    left.toFloat(),
                    top.toFloat(),
                    (left + cellW - 8).toFloat(),
                    (top + cellH - 8).toFloat()
                )
                canvas.drawRoundRect(rect, 8f, 8f, blockPaint)
                canvas.drawRoundRect(rect, 8f, 8f, stroke)
            }
        }
        val parkPaint =
            android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#DFF2D8") }
        val parkRect = android.graphics.RectF(
            (w * 0.08).toFloat(),
            (h * 0.18).toFloat(),
            (w * 0.45).toFloat(),
            (h * 0.5).toFloat()
        )
        canvas.drawRoundRect(parkRect, 30f, 30f, parkPaint)

        // draw river
        val riverPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#C7EAFB"); style =
            android.graphics.Paint.Style.STROKE; strokeWidth = (h * 0.06).toFloat(); isAntiAlias =
            true
        }
        val riverPath = android.graphics.Path()
        riverPath.moveTo((w * 0.6).toFloat(), (h * 0.1).toFloat())
        riverPath.quadTo(
            (w * 0.75).toFloat(),
            (h * 0.35).toFloat(),
            (w * 0.6).toFloat(),
            (h * 0.6).toFloat()
        )
        riverPath.quadTo(
            (w * 0.45).toFloat(),
            (h * 0.85).toFloat(),
            (w * 0.8).toFloat(),
            (h * 0.9).toFloat()
        )
        canvas.drawPath(riverPath, riverPaint)

        if (points.isNotEmpty()) {
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
            val latPad = (maxLat - minLat) * 0.1 + 1e-6
            val lonPad = (maxLon - minLon) * 0.1 + 1e-6
            minLat -= latPad; maxLat += latPad; minLon -= lonPad; maxLon += lonPad
            fun xOf(lon: Double) = ((lon - minLon) / (maxLon - minLon) * (w - 64) + 32).toFloat()
            fun yOf(lat: Double) =
                ((maxLat - lat) / (maxLat - minLat) * (h - 64) + 32).toFloat() // inverted

            val pathPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#7C4DFF"); style =
                android.graphics.Paint.Style.STROKE; strokeWidth = 10f; isAntiAlias = true
            }
            val path = android.graphics.Path()
            for ((i, p) in points.withIndex()) {
                val x = xOf(p.longitude)
                val y = yOf(p.latitude)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, pathPaint)
            val sx = xOf(points.first().longitude)
            val sy = yOf(points.first().latitude)
            canvas.drawCircle(
                sx,
                sy,
                12f,
                android.graphics.Paint()
                    .apply { color = android.graphics.Color.parseColor("#FFD54F") })
            canvas.drawCircle(
                sx,
                sy,
                12f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#E6E0FF"); style =
                    android.graphics.Paint.Style.STROKE; strokeWidth = 4f
                })
        }

        return bmp
    }
}


