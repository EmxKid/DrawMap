package com.example.drawmap.ui.route

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.R
import com.example.drawmap.data.model.Route
import com.example.drawmap.ui.base.BaseActivity
import com.example.drawmap.ui.base.UiConstants
import com.example.drawmap.ui.utils.MapFallbackRenderer
import com.example.drawmap.viewModel.RouteFullScreenViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Activity для полноэкранного отображения маршрута
 */
class RouteFullScreenActivity : BaseActivity() {

    companion object {
        fun start(context: Context, routeId: String) {
            val intent = Intent(context, RouteFullScreenActivity::class.java).apply {
                putExtra(UiConstants.IntentKeys.ROUTE_ID, routeId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var viewModel: RouteFullScreenViewModel
    private lateinit var mapView: MapView
    private lateinit var tvTitle: TextView
    private lateinit var tvStats: TextView
    private lateinit var ivFallback: ImageView
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_route_fullscreen)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[RouteFullScreenViewModel::class.java]

        // Инициализация views
        initViews()
        
        // Настройка карты
        setupMap()
        
        // Наблюдение за состоянием
        observeUiState()
        
        // Настройка кнопок
        setupButtons()

        // Загрузка маршрута
        val routeId = intent.getStringExtra(UiConstants.IntentKeys.ROUTE_ID)
        if (routeId != null) {
            viewModel.loadRoute(routeId)
        } else {
            showError("ID маршрута не указан")
            finish()
        }
    }

    private fun initViews() {
        mapView = findViewById(R.id.mapViewFull)
        tvTitle = findViewById(R.id.tvTitleFull)
        tvStats = findViewById(R.id.tvStats)
        ivFallback = findViewById(R.id.ivFullFallback)
        btnClose = findViewById(R.id.btnCloseFull)
    }

    private fun setupMap() {
        // Всегда показываем карту
        mapView.visibility = android.view.View.VISIBLE
        ivFallback.visibility = android.view.View.VISIBLE
        
        mapView.apply {
            setMultiTouchControls(true)
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(16.0)
        }
        
        // Показываем предупреждение если нет интернета
        if (!hasInternetConnection()) {
            showMessage("⚠️ Нет интернета. Тайлы карты могут не загрузиться")
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is RouteFullScreenViewModel.UiState.Loading -> {
                        // TODO: Показать прогресс
                    }
                    is RouteFullScreenViewModel.UiState.Success -> {
                        displayRoute(state.route)
                    }
                    is RouteFullScreenViewModel.UiState.Error -> {
                        showError(state.message)
                    }
                    is RouteFullScreenViewModel.UiState.Idle -> {
                        // Ничего не делаем
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun displayRoute(route: Route) {
        // Отображаем информацию о маршруте
        val routeInfo = viewModel.getRouteInfo(route)
        tvTitle.text = routeInfo.title
        tvStats.text = "Distance: ${routeInfo.distanceFormatted} • Duration: ${routeInfo.durationFormatted}"

        // Показываем карту сразу
        mapView.visibility = android.view.View.VISIBLE
        ivFallback.visibility = android.view.View.VISIBLE

        // Генерируем fallback изображение на заднем плане
        lifecycleScope.launch {
            try {
                val fallbackBitmap = MapFallbackRenderer.renderRouteOnMap(
                    points = route.coordinates,
                    width = 1200,
                    height = 900
                )
                ivFallback.setImageBitmap(fallbackBitmap)
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }

        // Настраиваем overlay карты
        val polyline = Polyline(mapView).apply {
            setPoints(route.coordinates)
            outlinePaint.strokeWidth = 14f
            outlinePaint.color = 0xFF303F9F.toInt()
        }
        mapView.overlays.add(polyline)

        // Центрируем карту
        if (route.coordinates.isNotEmpty()) {
            val midPoint = route.coordinates[route.coordinates.size / 2]
            mapView.controller.setZoom(16.0)
            mapView.controller.setCenter(midPoint)

            // Добавляем маркер старта
            val startMarker = Marker(mapView).apply {
                position = route.coordinates.first()
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mapView.overlays.add(startMarker)
        }
        
        mapView.invalidate()

        // Проверяем загрузку тайлов
        mapView.postDelayed({
            checkTilesAndSwitchView()
        }, 2200)
    }

    private fun checkTilesAndSwitchView() {
        try {
            val sampleBitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(sampleBitmap)
            mapView.draw(canvas)

            if (!MapFallbackRenderer.isMostlyGreen(sampleBitmap)) {
                // Тайлы загружены - скрываем fallback
                ivFallback.visibility = android.view.View.GONE
                mapView.visibility = android.view.View.VISIBLE
            } else {
                // Тайлы не загрузились - показываем fallback
                ivFallback.visibility = android.view.View.VISIBLE
                mapView.visibility = android.view.View.GONE
            }
            
            sampleBitmap.recycle()
        } catch (e: Exception) {
            // В случае ошибки показываем карту
            mapView.visibility = android.view.View.VISIBLE
            ivFallback.visibility = android.view.View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mapView.onResume()
        } catch (e: Exception) {
            // Игнорируем ошибки
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mapView.onPause()
        } catch (e: Exception) {
            // Игнорируем ошибки
        }
    }
    
    /**
     * Проверяет наличие интернет соединения (для загрузки тайлов карты)
     */
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
