package com.example.drawmap.ui.heatmap

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.R
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.components.BottomNavBar
import com.example.drawmap.ui.navigation.Navigator
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class HeatmapActivity : AppCompatActivity() {

    private lateinit var navigator: Navigator
    private lateinit var bottomNav: BottomNavBar
    private lateinit var mapView: MapView
    private lateinit var heatmapOverlay: HeatmapOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_heatmap)

        navigator = Navigator(this)

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Map
        mapView = findViewById(R.id.mapView)
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(55.7558, 37.6173)) // Москва
        }

        // Heatmap overlay
        heatmapOverlay = HeatmapOverlay(mapView)
        mapView.overlays.add(heatmapOverlay)

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNavigationView)
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

        // Загружаем данные с задержкой чтобы не блокировать UI
        mapView.postDelayed({
            loadMockHeatmapData()
        }, 300)
    }

    private fun loadMockHeatmapData() {
        lifecycleScope.launch {
            val visitPoints = ServiceLocator.provideHeatmapRepository().getHeatmapData()
            heatmapOverlay.setPoints(visitPoints)
            mapView.invalidate()

            Toast.makeText(
                this@HeatmapActivity,
                "🔥 Загружено ${visitPoints.size} точек посещений",
                Toast.LENGTH_SHORT
            ).show()
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