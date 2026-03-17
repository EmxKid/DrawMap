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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.core.graphics.toColorInt

/**
 * Activity для отображения деталей маршрута
 * Показывает маршрут на карте с возможностью повтора
 */
class RouteDetailActivity : BaseActivity() {

    companion object {
        fun start(context: Context, routeId: String) {
            val intent = Intent(context, RouteDetailActivity::class.java).apply {
                putExtra(UiConstants.IntentKeys.ROUTE_ID, routeId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var viewModel: RouteDetailViewModel
    private lateinit var mapView: MapView
    private lateinit var tvOffline: TextView
    private lateinit var ivFallback: ImageView
    private lateinit var btnRepeat: Button

    private var currentRoute: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Настройка osmdroid
        Configuration.getInstance().userAgentValue = packageName
        
        setContentView(R.layout.activity_route_detail)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[RouteDetailViewModel::class.java]

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
        mapView = findViewById(R.id.mapViewDetail)
        tvOffline = findViewById(R.id.tvOfflineDetail)
        ivFallback = findViewById(R.id.ivMapFallback)
        btnRepeat = findViewById(R.id.btnRepeat)
    }

    private fun setupMap() {
        // Всегда показываем карту и fallback
        tvOffline.visibility = android.view.View.GONE
        mapView.visibility = android.view.View.VISIBLE
        ivFallback.visibility = android.view.View.VISIBLE

        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBackgroundColor("#ECEFF1".toColorInt())
            controller.setZoom(UiConstants.Map.DEFAULT_ZOOM)
        }
        
        // Показываем предупреждение если нет интернета
        if (!hasInternetConnection()) {
            showMessage("⚠️ Нет интернета. Тайлы карты могут не загрузиться")
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

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is RouteDetailViewModel.UiState.Loading -> {
                        // TODO: Показать прогресс
                    }
                    is RouteDetailViewModel.UiState.Success -> {
                        currentRoute = state.route
                        showRouteOnMap(state.route)
                    }
                    is RouteDetailViewModel.UiState.Error -> {
                        showError(state.message)
                    }
                    is RouteDetailViewModel.UiState.Idle -> {
                        // Ничего не делаем
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        btnRepeat.setOnClickListener {
            val routeId = intent.getStringExtra(UiConstants.IntentKeys.ROUTE_ID) ?: return@setOnClickListener
            val intent = Intent(this, com.example.drawmap.ui.home.HomeActivity::class.java).apply {
                putExtra(UiConstants.IntentKeys.REPEAT_ROUTE_ID, routeId)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun showRouteOnMap(route: Route) {
        // Генерируем и показываем fallback на заднем плане (пока карта грузится)
        lifecycleScope.launch {
            try {
                val fallbackBitmap = MapFallbackRenderer.renderRouteOnMap(
                    points = route.coordinates,
                    width = 800,
                    height = 600
                )
                ivFallback.setImageBitmap(fallbackBitmap)
                // Fallback будет видно только если карта не загрузится
            } catch (e: Exception) {
                // Игнорируем ошибку рендеринга fallback
            }
        }

        // Добавляем маршрут на карту
        val polyline = Polyline(mapView).apply {
            setPoints(route.coordinates)
            outlinePaint.color = 0xFF6200EE.toInt() // Фиолетовый цвет
            outlinePaint.strokeWidth = 6f
            setOnClickListener { _, _, _ ->
                RouteFullScreenActivity.start(this@RouteDetailActivity, route.id)
                true
            }
        }
        mapView.overlays.add(polyline)

        // Добавляем маркеры фотографий
        route.photoUris.forEach { uri ->
            val marker = Marker(mapView).apply {
                position = route.coordinates.firstOrNull() ?: return@apply
                setOnMarkerClickListener { _, _ ->
                    com.example.drawmap.ui.photo.PhotoActivity.start(
                        this@RouteDetailActivity,
                        uri
                    )
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        // Центрируем карту
        if (route.coordinates.isNotEmpty()) {
            mapView.controller.setZoom(UiConstants.Map.DEFAULT_ZOOM)
            mapView.controller.setCenter(route.coordinates[0])
        }

        // Проверяем загрузку тайлов через задержку
        mapView.postDelayed({
            checkTilesAndSwitchView()
        }, 2500) // Даем время на загрузку тайлов

        mapView.invalidate()
    }

    private fun checkTilesAndSwitchView() {
        try {
            val sampleBitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(sampleBitmap)
            mapView.draw(canvas)

            if (!MapFallbackRenderer.isMostlyGreen(sampleBitmap)) {
                // Тайлы загружены успешно - скрываем fallback
                ivFallback.visibility = android.view.View.GONE
                mapView.visibility = android.view.View.VISIBLE
            } else {
                // Тайлы не загрузились - показываем fallback поверх
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
}
