package com.example.drawmap.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.example.drawmap.R
import com.example.drawmap.ui.components.BottomNavBar
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.navigation.Navigator
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var navigator: Navigator
    private lateinit var mapView: MapView
    private lateinit var bottomNavBar: BottomNavBar
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fabStartRecording: FloatingActionButton
    private lateinit var permissionOverlay: FrameLayout
    private lateinit var btnRequestLocation: Button

    // 🔹 FusedLocationProviderClient для получения локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCancellationToken = CancellationTokenSource()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔧 Инициализация OSMDroid
        Configuration.getInstance().apply {
            load(this@HomeActivity, PreferenceManager.getDefaultSharedPreferences(this@HomeActivity))
            userAgentValue = "${packageName}/1.0"
        }

        setContentView(R.layout.activity_home)

        // Инициализация
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        navigator = Navigator(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Привязка видов
        mapView = findViewById(R.id.mapView)
        bottomNavBar = findViewById(R.id.bottomNavBar)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        fabStartRecording = findViewById(R.id.fabStartRecording)
        permissionOverlay = findViewById(R.id.locationPermissionOverlay)
        btnRequestLocation = findViewById(R.id.btnRequestLocation)

        // Настройка
        setupMap()
        setupNavigation()
        setupButtons()
        setupLocationPermission()

        // Обрабатываем intent: если пришло repeat_route_id — наложим призрачный маршрут
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun handleIntent(intent: Intent) {
        val repeatId = intent.getStringExtra("repeat_route_id")
        // show/hide offline placeholder
        findViewById<android.view.View>(R.id.tvOffline)?.let { placeholder ->
            placeholder.visibility = if (isOnline()) android.view.View.GONE else android.view.View.VISIBLE
        }

        if (!repeatId.isNullOrEmpty()) {
            lifecycleScope.launch {
                val route = ServiceLocator.routeRepository.getRouteById(repeatId)
                if (route != null) {
                    applyGhostRoute(route)
                    // подготовить ViewModel к повтору
                    viewModel.prepareForRepeat(route)
                    Toast.makeText(this@HomeActivity, "Наложен призрачный маршрут: ${route.title}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@HomeActivity, "Не найден маршрут для повтора", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMap() {
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)

            // По умолчанию центрируем на Москве (заглушка)
            val moscow = GeoPoint(55.7558, 37.6173)
            controller.setCenter(moscow)

            // Маркер "Вы здесь" (пока тестовый)
            val userMarker = Marker(this).apply {
                position = moscow
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "📍 Москва (заглушка)"
            }
            overlays.add(userMarker)
        }
    }

    private fun applyGhostRoute(route: com.example.drawmap.data.model.Route) {
        val points = route.coordinates
        val ghost = Polyline(mapView).apply {
            setPoints(points)
            outlinePaint.color = 0x550000FF // полупрозрачный синий
            outlinePaint.strokeWidth = 6f
        }
        mapView.overlays.add(ghost)

        // Маркер старта
        route.coordinates.firstOrNull()?.let { start ->
            val startMarker = Marker(mapView).apply {
                position = start
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Старт: ${route.title}"
            }
            mapView.overlays.add(startMarker)
        }

        mapView.invalidate()
    }

    private fun setupNavigation() {
        bottomNavBar.setSelectedItem(R.id.nav_home)
        bottomNavBar.onItemSelected { itemId ->
            when (itemId) {
                R.id.nav_home -> true
                R.id.nav_gallery -> {
                    navigator.navigateTo(Navigator.SCREEN_GALLERY)
                    true
                }
                R.id.nav_heatmap -> {
                    Toast.makeText(this, "🔥 Heatmap (заглушка)", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        // Кнопка "Моя локация" — работает только при наличии разрешения
        fabMyLocation.setOnClickListener {
            requestCurrentLocation { geoPoint ->
                mapView.controller.animateTo(geoPoint, 15.0, 500)
                Toast.makeText(this, "📍 Центрируем на вас", Toast.LENGTH_SHORT).show()
            }
        }

        // Кнопка "Начать запись"
        fabStartRecording.setOnClickListener {
            if (hasLocationPermission()) {
                viewModel.onStartRecordingClick {
                    Toast.makeText(this, "▶ Запись маршрута", Toast.LENGTH_SHORT).show()
                }
            } else {
                showPermissionOverlay()
                Toast.makeText(this, "🔓 Сначала разрешите доступ к локации", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔐 Логика работы с разрешениями
    private fun setupLocationPermission() {
        if (hasLocationPermission()) {
            // ✅ Разрешение есть — показываем карту и кнопки
            hidePermissionOverlay()
            enableMapControls(true)
            requestCurrentLocation { geoPoint ->
                centerMapOnUser(geoPoint)
            }
        } else {
            // ❌ Разрешения нет — показываем заглушку
            showPermissionOverlay()
            enableMapControls(false)
        }

        // Обработчик кнопки "Разрешить доступ"
        btnRequestLocation.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (granted) {
                // ✅ Пользователь дал разрешение
                Toast.makeText(this, "🎉 Доступ к локации разрешён", Toast.LENGTH_SHORT).show()
                hidePermissionOverlay()
                enableMapControls(true)
                requestCurrentLocation { geoPoint ->
                    centerMapOnUser(geoPoint)
                }
            } else {
                // ❌ Пользователь отклонил
                Toast.makeText(
                    this,
                    "⚠️ Без локации карта будет показывать Москву",
                    Toast.LENGTH_LONG
                ).show()
                // Не скрываем overlay — пользователь может попробовать снова
            }
        }
    }

    // 📍 Получение текущей локации
    private fun requestCurrentLocation(onLocationReceived: (GeoPoint) -> Unit) {
        if (!hasLocationPermission()) {
            showPermissionOverlay()
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
                    // Локация не получена — используем заглушку
                    Toast.makeText(this, "📡 Не удалось получить локацию", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "❌ Ошибка получения локации", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            // На всякий случай — если разрешение отозвали
            showPermissionOverlay()
        }
    }

    // 🎯 Центрирование карты на пользователе
    private fun centerMapOnUser(geoPoint: GeoPoint) {
        // Убираем старый маркер (если есть)
        val oldMarker = mapView.overlays.firstOrNull { it is Marker && it.title?.contains("Вы здесь") == true }
        if (oldMarker != null) mapView.overlays.remove(oldMarker)

        // Добавляем новый маркер
        val userMarker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "📍 Вы здесь"
        }
        mapView.overlays.add(userMarker)

        // Анимация перехода
        mapView.controller.animateTo(geoPoint, 15.0, 500)
        mapView.invalidate()
    }

    // 👁 Показ/скрытие overlay
    private fun showPermissionOverlay() {
        permissionOverlay.visibility = FrameLayout.VISIBLE
        fabMyLocation.visibility = FloatingActionButton.GONE
        fabStartRecording.visibility = FloatingActionButton.GONE
    }

    private fun hidePermissionOverlay() {
        permissionOverlay.visibility = FrameLayout.GONE
        fabMyLocation.visibility = FloatingActionButton.VISIBLE
        fabStartRecording.visibility = FloatingActionButton.VISIBLE
    }

    // 🔘 Включение/отключение элементов карты
    private fun enableMapControls(enabled: Boolean) {
        mapView.isClickable = enabled
        mapView.setMultiTouchControls(enabled)
    }

    // 🔁 Жизненный цикл OSMDroid
    override fun onResume() {
        super.onResume()
        mapView.onResume()

        // Если разрешение появилось (например, в настройках), обновляем UI
        if (hasLocationPermission() && permissionOverlay.visibility == FrameLayout.VISIBLE) {
            hidePermissionOverlay()
            enableMapControls(true)
            requestCurrentLocation { geoPoint ->
                centerMapOnUser(geoPoint)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //mapView.onDestroy()
        locationCancellationToken.cancel() // Отменяем запрос локации
        Configuration.getInstance().save(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
    }
}