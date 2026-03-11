package com.example.drawmap.ui.gallery

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.MeasureSpec
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drawmap.R
import com.example.drawmap.data.model.GalleryItem
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.navigation.Navigator
import com.example.drawmap.ui.route.RouteDetailActivity
import com.example.drawmap.ui.photo.PhotoActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import kotlin.math.max
import kotlin.math.min

class GalleryActivity : AppCompatActivity() {

    private lateinit var navigator: Navigator
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var rvGallery: RecyclerView

    private val adapter = GalleryAdapter(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init osmdroid user agent to allow tile downloads
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_gallery)

        navigator = Navigator(this)

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // RecyclerView
        rvGallery = findViewById(R.id.rvGallery)
        rvGallery.layoutManager = LinearLayoutManager(this)
        rvGallery.adapter = adapter

        // Привязка нижней панели
        bottomNav = findViewById(R.id.bottomNavigationView)
        // Отмечаем текущий элемент как галерею
        bottomNav.selectedItemId = R.id.nav_gallery

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Перейдём обратно на главный экран
                    navigator.navigateTo(Navigator.SCREEN_HOME)
                    true
                }
                R.id.nav_gallery -> {
                    // Уже на галерее
                    true
                }
                R.id.nav_heatmap -> {
                    Toast.makeText(this, "🔥 Heatmap (заглушка)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Кнопка "Назад" оставляем в ToolBar как Up
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Загружаем данные из мок-репозитория
        lifecycleScope.launch {
            val items = ServiceLocator.galleryRepository.getGalleryItems()
            adapter.submitList(items)
        }
    }

    private fun isMostlySingleColor(bitmap: Bitmap, sampleStep: Int = 8, threshold: Double = 0.95): Boolean {
        val colorCount = HashMap<Int, Int>()
        val w = bitmap.width
        val h = bitmap.height
        var total = 0
        for (y in 0 until h step sampleStep) {
            for (x in 0 until w step sampleStep) {
                val c = bitmap.getPixel(x, y)
                colorCount[c] = (colorCount[c] ?: 0) + 1
                total++
            }
        }
        if (total == 0) return true
        val max = colorCount.values.maxOrNull() ?: 0
        return max.toDouble() / total.toDouble() >= threshold
    }

    // Попытка отрисовать миниатюру с оффскрин MapView (с загрузкой тайлов), затем наложить polylines
    private suspend fun renderRouteThumbWithMap(points: List<GeoPoint>, w: Int, h: Int): Bitmap = withContext(Dispatchers.Main) {
        // создаём MapView offscreen
        val mv = MapView(this@GalleryActivity)
        mv.setTileSource(TileSourceFactory.MAPNIK)
        mv.setMultiTouchControls(false)

        if (points.isNotEmpty()) {
            val mid = points[points.size / 2]
            mv.controller.setZoom(15.0)
            mv.controller.setCenter(mid)
        }

        // измеряем и раскладываем
        mv.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
        mv.layout(0, 0, w, h)

        // даём небольшую задержку, чтобы тайлы начали загружаться
        // (в некоторых случаях потребуется больше времени/кэширование)
        delay(400)

        // нарисуем MapView в bitmap
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        // draw map background
        mv.draw(canvas)

        // если mapView отрисовал только однотонный фон (проверяем частоту цвета), падаем к fallback
        if (isMostlySingleColor(bmp)) {
            throw IllegalStateException("map tiles not rendered in offscreen MapView")
        }

        // далее рисуем полилинию поверх
        if (points.isNotEmpty()) {
            // compute bounds
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
            fun xOf(lon: Double) = ((lon - minLon) / (maxLon - minLon) * (w - 32) + 16).toFloat()
            fun yOf(lat: Double) = ((maxLat - lat) / (maxLat - minLat) * (h - 32) + 16).toFloat() // inverted

            val pathPaint = Paint().apply { color = Color.parseColor("#7C4DFF"); style = Paint.Style.STROKE; strokeWidth = 6f; isAntiAlias = true }
            val path = Path()
            for ((i, p) in points.withIndex()) {
                val x = xOf(p.longitude)
                val y = yOf(p.latitude)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, pathPaint)

            // draw start marker
            val startPaint = Paint().apply { color = Color.parseColor("#FFD54F"); style = Paint.Style.FILL }
            val sx = xOf(points.first().longitude)
            val sy = yOf(points.first().latitude)
            canvas.drawCircle(sx, sy, 8f, startPaint)
            canvas.drawCircle(sx, sy, 8f, Paint().apply { color = Color.parseColor("#E6E0FF"); style = Paint.Style.STROKE; strokeWidth = 3f })
        }

        return@withContext bmp
    }

    // fallback: векторная миниатюра без тайлов (быстрее, надежнее) — улучшенный, выглядит как карта
    private suspend fun renderRouteThumb(points: List<GeoPoint>, w: Int, h: Int): Bitmap = withContext(Dispatchers.Default) {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // базовый фон — светлый
        canvas.drawColor(Color.parseColor("#F2EFEA"))

        // рисуем блоки улиц — светлые прямоугольники
        val blockPaint = Paint().apply { color = Color.parseColor("#EDE6DF"); style = Paint.Style.FILL }
        val stroke = Paint().apply { color = Color.parseColor("#DDD6CF"); style = Paint.Style.STROKE; strokeWidth = 2f }
        val cols = 4
        val rows = 6
        val padX = (w * 0.06).toInt()
        val padY = (h * 0.06).toInt()
        val cellW = (w - padX * 2) / cols
        val cellH = (h - padY * 2) / rows
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = padX + c * cellW + (if ((r + c) % 2 == 0) 6 else 0)
                val top = padY + r * cellH + (if ((r + c) % 3 == 0) 4 else 0)
                val rect = RectF(left.toFloat(), top.toFloat(), (left + cellW - 8).toFloat(), (top + cellH - 8).toFloat())
                canvas.drawRoundRect(rect, 8f, 8f, blockPaint)
                canvas.drawRoundRect(rect, 8f, 8f, stroke)
            }
        }

        // рисуем парк — зеленая овальная область
        val parkPaint = Paint().apply { color = Color.parseColor("#DFF2D8") }
        val parkRect = RectF((w * 0.1).toFloat(), (h * 0.2).toFloat(), (w * 0.45).toFloat(), (h * 0.55).toFloat())
        canvas.drawRoundRect(parkRect, 30f, 30f, parkPaint)

        // река как изогнутая голубая линия
        val riverPaint = Paint().apply { color = Color.parseColor("#C7EAFB"); style = Paint.Style.STROKE; strokeWidth = (h * 0.08).toFloat(); isAntiAlias = true }
        val riverPath = Path()
        riverPath.moveTo((w * 0.6).toFloat(), (h * 0.1).toFloat())
        riverPath.quadTo((w * 0.75).toFloat(), (h * 0.35).toFloat(), (w * 0.6).toFloat(), (h * 0.6).toFloat())
        riverPath.quadTo((w * 0.45).toFloat(), (h * 0.85).toFloat(), (w * 0.8).toFloat(), (h * 0.9).toFloat())
        canvas.drawPath(riverPath, riverPaint)

        // draw route polyline on top
        if (points.isEmpty()) return@withContext bmp

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

        fun xOf(lon: Double) = ((lon - minLon) / (maxLon - minLon) * (w - 32) + 16).toFloat()
        fun yOf(lat: Double) = ((maxLat - lat) / (maxLat - minLat) * (h - 32) + 16).toFloat() // inverted

        val pathPaint = Paint().apply { color = Color.parseColor("#7C4DFF"); style = Paint.Style.STROKE; strokeWidth = 6f; isAntiAlias = true }
        val path = Path()
        for ((i, p) in points.withIndex()) {
            val x = xOf(p.longitude)
            val y = yOf(p.latitude)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, pathPaint)

        val sx = xOf(points.first().longitude)
        val sy = yOf(points.first().latitude)
        canvas.drawCircle(sx, sy, 8f, Paint().apply { color = Color.parseColor("#FFD54F") })
        canvas.drawCircle(sx, sy, 8f, Paint().apply { color = Color.parseColor("#E6E0FF"); style = Paint.Style.STROKE; strokeWidth = 3f })

        return@withContext bmp
    }

    private inner class GalleryAdapter(private var items: List<GalleryItem>) : RecyclerView.Adapter<GalleryAdapter.VH>() {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val ivPreview: ImageView = view.findViewById(R.id.ivPreview)
            val tvTitle: TextView = view.findViewById(R.id.tvCardTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvCardSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_card, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvTitle.text = item.title
            holder.tvSubtitle.text = item.subtitle
            // сначала ставим нейтральную картинку
            holder.ivPreview.setImageResource(R.drawable.ic_launcher_background)

            // Загружаем маршрут и генерируем миниатюру
            this@GalleryActivity.lifecycleScope.launch {
                val route = ServiceLocator.routeRepository.getRouteById(item.id)
                if (route != null && route.coordinates.isNotEmpty()) {
                    // надежно: используем векторный рендерfallback вместо offscreen MapView
                    val bmp = renderRouteThumb(route.coordinates, 480, 260)
                    holder.ivPreview.setImageBitmap(bmp)
                } else {
                    // оставляем placeholder
                    holder.ivPreview.setImageResource(R.drawable.ic_launcher_background)
                }
            }

            holder.itemView.setOnClickListener {
                // Открыть детали маршрута
                RouteDetailActivity.start(this@GalleryActivity, item.id)
            }
            holder.ivPreview.setOnClickListener {
                // всегда открываем детали маршрута на клик (избегаем показа placeholder фото)
                RouteDetailActivity.start(this@GalleryActivity, item.id)
            }
        }

        override fun getItemCount(): Int = items.size

        fun submitList(newItems: List<GalleryItem>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}