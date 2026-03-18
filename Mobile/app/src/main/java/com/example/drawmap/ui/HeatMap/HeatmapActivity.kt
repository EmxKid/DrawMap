package com.example.drawmap.ui.HeatMap

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.R
import com.example.drawmap.config.AppConfig
import com.example.drawmap.ui.base.BaseActivity
import com.example.drawmap.ui.components.BottomNavBar
import com.example.drawmap.ui.heatmap.HeatmapOverlay
import com.example.drawmap.ui.heatmap.HeatmapPoint
import com.example.drawmap.ui.navigation.Navigator
import com.example.drawmap.viewModel.HeatmapViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.drawmap.ui.base.UiConstants

class HeatmapActivity : BaseActivity() {

    private lateinit var viewModel: HeatmapViewModel
    private lateinit var navigator: Navigator
    private lateinit var bottomNav: BottomNavBar
    private lateinit var mapView: MapView
    private lateinit var heatmapOverlay: HeatmapOverlay
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCancellationToken = CancellationTokenSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_heatmap)

        viewModel = ViewModelProvider(this)[HeatmapViewModel::class.java]
        navigator = Navigator(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initViews()

        // Map
        setupMap()

        // Heatmap overlay
        setupHeatmapOverlay()

        // Bottom navigation
        setupBottomNavigation()

        // Setup FAB button
        setupFabButton()

        observeUiState()

        viewModel.loadHeatmapData()
    }

    private fun initViews() {
        bottomNav = findViewById(R.id.bottomNavigationView)
        mapView = findViewById(R.id.mapView)
        fabMyLocation = findViewById(R.id.fabMyLocation)
    }

    private fun setupMap() {
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(56.4977, 84.9744)) // Томск
        }
    }

    private fun setupHeatmapOverlay() {
        heatmapOverlay = HeatmapOverlay(mapView)
        mapView.overlays.add(heatmapOverlay)
    }

    private fun setupBottomNavigation() {
        bottomNav.setSelectedItem(R.id.nav_heatmap)

        bottomNav.onItemSelected { itemId ->
            when (itemId) {
                R.id.nav_home -> {
                    navigator.navigateTo(Navigator.SCREEN_HOME)
                    true
                }

                R.id.nav_gallery -> {
                    navigator.navigateTo(Navigator.SCREEN_GALLERY)
                    true
                }

                R.id.nav_heatmap -> true
                else -> false
            }
        }
    }

    private fun setupFabButton() {
        fabMyLocation.setOnClickListener {
            requestCurrentLocation { geoPoint ->
                mapView.controller.animateTo(geoPoint, 15.0, 500)
                showMessage("📍 Центрируем на вас")
            }
        }
    }

    private fun requestCurrentLocation(onLocationReceived: (GeoPoint) -> Unit) {
        if (!permissionHelper.hasPermissions(UiConstants.Permissions.LOCATION_PERMISSIONS)) {
            showMessage("🔓 Необходим доступ к локации")
            permissionHelper.requestPermissions(
                UiConstants.Permissions.LOCATION_PERMISSIONS,
                onGranted = {
                    showMessage("🎉 Доступ к локации разрешён")
                    requestCurrentLocation(onLocationReceived)
                },
                onDenied = {
                    showError("⚠️ Без доступа к локации невозможно центрировать карту")
                }
            )
            return
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                locationCancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    onLocationReceived(geoPoint)
                } else {
                    showMessage("📡 Не удалось получить локацию")
                }
            }.addOnFailureListener {
                showError("❌ Ошибка получения локации")
            }
        } catch (e: SecurityException) {
            showMessage("🔓 Необходим доступ к локации")
        }
    }

    private fun loadHeatmapData(points: List<HeatmapPoint> ) {
        lifecycleScope.launch {
            heatmapOverlay.setPoints(points)
            mapView.invalidate()
            showMessage("🔥 Загружено ${points.size} точек посещений")
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is HeatmapViewModel.UiState.Success -> {
                        loadHeatmapData(state.points)
                    }
                    is HeatmapViewModel.UiState.Empty -> {
                        showMessage("Точек нет")
                    }
                    is HeatmapViewModel.UiState.Error -> {
                        showError(state.message)
                    }
                    is HeatmapViewModel.UiState.Idle -> {
                        // Ничего не делаем
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем кэш тепловой карты
        heatmapOverlay.clearCache()
    }
}