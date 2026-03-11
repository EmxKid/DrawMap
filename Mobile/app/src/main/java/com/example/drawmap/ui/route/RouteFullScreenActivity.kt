package com.example.drawmap.ui.route

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.remote.creation.max
import com.example.drawmap.R
import com.example.drawmap.data.model.Route
import com.example.drawmap.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker
import kotlin.math.max
import kotlin.math.min

class RouteFullScreenActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ROUTE_ID = "extra_route_id"
        fun start(context: Context, routeId: String) {
            val i = Intent(context, RouteFullScreenActivity::class.java)
            i.putExtra(EXTRA_ROUTE_ID, routeId)
            context.startActivity(i)
        }
    }

    private lateinit var mapView: MapView
    private lateinit var tvTitle: TextView
    private lateinit var tvStats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_route_fullscreen)

        mapView = findViewById(R.id.mapViewFull)
        mapView.setMultiTouchControls(true)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        tvTitle = findViewById(R.id.tvTitleFull)
        tvStats = findViewById(R.id.tvStats)

        val ivFallback = findViewById<ImageView>(R.id.ivFullFallback)

        val routeId = intent.getStringExtra(EXTRA_ROUTE_ID) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val route: Route? = ServiceLocator.routeRepository.getRouteById(routeId)
            if (route != null) {
                tvTitle.text = route.title
                val dist = route.distanceMeters ?: 0.0
                val dur = route.durationSeconds ?: 0L
                tvStats.text = "Distance: ${formatMeters(dist)} • Duration: ${formatDuration(dur)}"

                // generate and show fallback immediately to avoid green screen
                try {
                    val fb = renderFallbackBitmap(route, 1200, 900)
                    ivFallback.setImageBitmap(fb)
                    ivFallback.visibility = android.view.View.VISIBLE
                    mapView.visibility = android.view.View.GONE
                } catch (_: Exception) {
                    // ignore
                }

                // prepare map overlay
                val poly = Polyline(mapView)
                poly.setPoints(route.coordinates)
                poly.outlinePaint.strokeWidth = 14f
                poly.outlinePaint.color = 0xFF303F9F.toInt()
                mapView.overlays.add(poly)

                // center
                if (route.coordinates.isNotEmpty()) {
                    val mid = route.coordinates[route.coordinates.size / 2]
                    mapView.controller.setZoom(16.0)
                    mapView.controller.setCenter(mid)

                    val m = Marker(mapView)
                    m.position = route.coordinates.first()
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(m)
                }
                mapView.invalidate()

                // проверяем: если тайлы загрузились — показываем map и скрываем fallback
                mapView.postDelayed({
                    try {
                        val bmp = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
                        val c = android.graphics.Canvas(bmp)
                        mapView.draw(c)
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
                            ivFallback.visibility = android.view.View.GONE
                            mapView.visibility = android.view.View.VISIBLE
                        }
                    } catch (_: Exception) {}
                }, 2200)
            }
        }

        findViewById<Button>(R.id.btnCloseFull).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        try { mapView.onResume() } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        try { mapView.onPause() } catch (_: Exception) {}
    }

    // reuse the same fallback renderer as detail (simpler implementation)
    private fun renderFallbackBitmap(route: Route, w: Int = 1200, h: Int = 900): android.graphics.Bitmap {
        val points = route.coordinates
        val bmp = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.parseColor("#F2EFEA"))
        val blockPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#EDE6DF"); style = android.graphics.Paint.Style.FILL }
        val stroke = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#DDD6CF"); style = android.graphics.Paint.Style.STROKE; strokeWidth = 2f }
        val cols = 8
        val rows = 10
        val padX = (w * 0.03).toInt()
        val padY = (h * 0.03).toInt()
        val cellW = (w - padX * 2) / cols
        val cellH = (h - padY * 2) / rows
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = padX + c * cellW + (if ((r + c) % 2 == 0) 6 else 0)
                val top = padY + r * cellH + (if ((r + c) % 3 == 0) 4 else 0)
                val rect = android.graphics.RectF(left.toFloat(), top.toFloat(), (left + cellW - 8).toFloat(), (top + cellH - 8).toFloat())
                canvas.drawRoundRect(rect, 8f, 8f, blockPaint)
                canvas.drawRoundRect(rect, 8f, 8f, stroke)
            }
        }
        val parkPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#DFF2D8") }
        val parkRect = android.graphics.RectF((w * 0.08).toFloat(), (h * 0.18).toFloat(), (w * 0.45).toFloat(), (h * 0.5).toFloat())
        canvas.drawRoundRect(parkRect, 30f, 30f, parkPaint)

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
            fun yOf(lat: Double) = ((maxLat - lat) / (maxLat - minLat) * (h - 64) + 32).toFloat() // inverted
            val pathPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#7C4DFF"); style = android.graphics.Paint.Style.STROKE; strokeWidth = 14f; isAntiAlias = true }
            val path = android.graphics.Path()
            for ((i, p) in points.withIndex()) {
                val x = xOf(p.longitude)
                val y = yOf(p.latitude)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, pathPaint)
            val sx = xOf(points.first().longitude)
            val sy = yOf(points.first().latitude)
            canvas.drawCircle(sx, sy, 18f, android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#FFD54F") })
            canvas.drawCircle(sx, sy, 18f, android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#E6E0FF"); style = android.graphics.Paint.Style.STROKE; strokeWidth = 4f })
        }

        return bmp
    }

    private fun formatMeters(m: Double): String {
        return if (m >= 1000.0) {
            String.format("%.2f km", m / 1000.0)
        } else {
            String.format("%.0f m", m)
        }
    }

    private fun formatDuration(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

}
