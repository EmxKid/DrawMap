package com.example.drawmap.ui.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.navigation.Navigator
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var navigator: Navigator
    private lateinit var mapView: MapView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fabStartRecording: FloatingActionButton

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

        // Привязка видов
        mapView = findViewById(R.id.mapView)
        bottomNav = findViewById(R.id.bottomNavigationView)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        fabStartRecording = findViewById(R.id.fabStartRecording)

        // Настройка карты
        setupMap()

        // Настройка навигации
        setupNavigation()

        // Обработчики кнопок
        setupButtons()

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

            // Центрируем на тестовой локации (Москва)
            val moscow = GeoPoint(55.7558, 37.6173)
            controller.setCenter(moscow)

            // Маркер пользователя
            val userMarker = Marker(this).apply {
                position = moscow
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Вы здесь"
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
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Уже на главном
                    true
                }
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
        // Кнопка "Моя локация"
        fabMyLocation.setOnClickListener {
            val moscow = GeoPoint(55.7558, 37.6173)
            mapView.controller.animateTo(moscow, 15.0, 500)
            Toast.makeText(this, "📍 Центрируем на вас", Toast.LENGTH_SHORT).show()
        }

        // Кнопка "Начать запись"
        fabStartRecording.setOnClickListener {
            viewModel.onStartRecordingClick {
                Toast.makeText(this, "▶ Запись маршрута (заглушка)", Toast.LENGTH_SHORT).show()
                // TODO: navigator.navigateTo(Navigator.SCREEN_RECORDING)
            }
        }
    }

    // 🔁 Жизненный цикл OSMDroid — обязательно!
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
        Configuration.getInstance().save(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
    }
}