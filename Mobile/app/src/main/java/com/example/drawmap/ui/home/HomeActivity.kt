package com.example.drawmap.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.R
import com.example.drawmap.data.model.Route
import com.example.drawmap.di.AppModeManager
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.components.BottomNavBar
import com.example.drawmap.ui.navigation.Navigator
import com.example.drawmap.utils.RouteBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var navigator: Navigator
    private lateinit var mapView: MapView
    private lateinit var bottomNavBar: BottomNavBar
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fabStartRecording: FloatingActionButton
    private lateinit var permissionOverlay: FrameLayout
    private lateinit var btnRequestLocation: Button
    private lateinit var connectionStatusCard: MaterialCardView
    private lateinit var connectionStatusText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCancellationToken = CancellationTokenSource()
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private var ghostOverlays = mutableListOf<Any>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().apply {
            load(this@HomeActivity, PreferenceManager.getDefaultSharedPreferences(this@HomeActivity))
            userAgentValue = "${packageName}/1.0"
        }

        setContentView(R.layout.activity_home)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        navigator = Navigator(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById(R.id.mapView)
        bottomNavBar = findViewById(R.id.bottomNavBar)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        fabStartRecording = findViewById(R.id.fabStartRecording)
        permissionOverlay = findViewById(R.id.locationPermissionOverlay)
        btnRequestLocation = findViewById(R.id.btnRequestLocation)
        connectionStatusCard = findViewById(R.id.connectionStatusCard)
        connectionStatusText = findViewById(R.id.connectionStatusText)

        setupMap()
        setupNavigation()
        setupButtons()
        setupLocationPermission()
        setupConnectionStatusObserver()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun handleIntent(intent: Intent) {
        val repeatId = intent.getStringExtra("repeat_route_id")

        findViewById<View>(R.id.tvOffline)?.let { placeholder ->
            placeholder.visibility = if (isOnline()) View.GONE else View.VISIBLE
        }

        if (!repeatId.isNullOrEmpty()) {
            lifecycleScope.launch {
                val route = ServiceLocator.routeRepository.getRouteById(repeatId)
                if (route != null) {
                    clearGhostOverlays()
                    applyGhostRoute(route)
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

            myLocationOverlay = MyLocationNewOverlay(mapView)
            val directionArrow = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_marker_blue)
            if (directionArrow != null) {
                val bitmap = drawableToBitmap(directionArrow)
                val personBitmap = android.graphics.BitmapFactory.decodeResource(
                    resources,
                    org.osmdroid.library.R.drawable.person
                )
                myLocationOverlay.setDirectionArrow(personBitmap, bitmap)
            }
            overlays.add(myLocationOverlay)

            val moscow = GeoPoint(55.7558, 37.6173)
            controller.setCenter(moscow)
        }
    }

    private fun applyGhostRoute(route: Route) {
        val points = route.coordinates
        if (points.isEmpty()) return

        val routePolyline = Polyline(mapView).apply {
            setPoints(points)
            outlinePaint.color = "#7C4DFF".toColorInt()
            outlinePaint.strokeWidth = 8f
            outlinePaint.isAntiAlias = true
        }
        mapView.overlays.add(routePolyline)
        ghostOverlays.add(routePolyline)

        val startMarker = Marker(mapView).apply {
            position = points.first()
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "🚩 Старт: ${route.title}"
            val greenMarker = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_marker_green)
            if (greenMarker != null) {
                icon = greenMarker
            }
            setInfoWindow(null)
        }
        mapView.overlays.add(startMarker)
        ghostOverlays.add(startMarker)

        requestCurrentLocation { userLocation ->
            drawPathToStart(userLocation, points.first())
        }

        mapView.invalidate()
    }

    private fun drawPathToStart(from: GeoPoint, to: GeoPoint) {
        RouteBuilder.buildRouteAlongRoads(from, to) { routePoints ->
            if (routePoints != null && routePoints.isNotEmpty()) {
                val pathPolyline = Polyline(mapView).apply {
                    setPoints(routePoints)
                    outlinePaint.apply {
                        color = "#FFEB3B".toColorInt()
                        strokeWidth = 8f
                        isAntiAlias = true
                        pathEffect = DashPathEffect(floatArrayOf(30f, 15f), 0f)
                        alpha = 255
                    }
                }
                mapView.overlays.add(pathPolyline)
                ghostOverlays.add(pathPolyline)
            } else {
                val pathPoints = listOf(from, to)
                val pathPolyline = Polyline(mapView).apply {
                    setPoints(pathPoints)
                    outlinePaint.apply {
                        color = Color.parseColor("#FFEB3B")
                        strokeWidth = 8f
                        isAntiAlias = true
                        pathEffect = DashPathEffect(floatArrayOf(30f, 15f), 0f)
                        alpha = 255
                    }
                }
                mapView.overlays.add(pathPolyline)
                ghostOverlays.add(pathPolyline)
            }
            mapView.invalidate()
        }
    }

    private fun clearGhostOverlays() {
        ghostOverlays.forEach { overlay ->
            mapView.overlays.remove(overlay)
        }
        ghostOverlays.clear()
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
                    navigator.navigateTo(Navigator.SCREEN_HEATMAP)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        fabMyLocation.setOnClickListener {
            requestCurrentLocation { geoPoint ->
                mapView.controller.animateTo(geoPoint, 15.0, 500)
                Toast.makeText(this, "📍 Центрируем на вас", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun setupLocationPermission() {
        if (hasLocationPermission()) {
            hidePermissionOverlay()
            enableMapControls(true)
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.enableFollowLocation()
            requestCurrentLocation { geoPoint ->
                centerMapOnUser(geoPoint)
            }
        } else {
            showPermissionOverlay()
            enableMapControls(false)
            myLocationOverlay.disableMyLocation()
            myLocationOverlay.disableFollowLocation()
        }

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
                Toast.makeText(this, "🎉 Доступ к локации разрешён", Toast.LENGTH_SHORT).show()
                hidePermissionOverlay()
                enableMapControls(true)
                myLocationOverlay.enableMyLocation()
                myLocationOverlay.enableFollowLocation()
                requestCurrentLocation { geoPoint ->
                    centerMapOnUser(geoPoint)
                }
            } else {
                Toast.makeText(
                    this,
                    "⚠️ Без локации карта будет показывать Москву",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

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
                    viewModel.updateUserLocation(location.latitude, location.longitude)
                    onLocationReceived(geoPoint)
                } else {
                    Toast.makeText(this, "📡 Не удалось получить локацию", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "❌ Ошибка получения локации", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            showPermissionOverlay()
        }
    }

    private fun centerMapOnUser(geoPoint: GeoPoint) {
        mapView.controller.animateTo(geoPoint, 15.0, 500)
        mapView.invalidate()
    }

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

    private fun enableMapControls(enabled: Boolean) {
        mapView.isClickable = enabled
        mapView.setMultiTouchControls(enabled)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        myLocationOverlay.onResume()

        if (hasLocationPermission() && permissionOverlay.visibility == FrameLayout.VISIBLE) {
            hidePermissionOverlay()
            enableMapControls(true)
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.enableFollowLocation()
            requestCurrentLocation { geoPoint ->
                centerMapOnUser(geoPoint)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        myLocationOverlay.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCancellationToken.cancel()
        Configuration.getInstance().save(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    private fun setupConnectionStatusObserver() {
        lifecycleScope.launch {
            AppModeManager.isOnlineMode.collect { isOnline ->
                if (!isOnline) {
                    connectionStatusCard.visibility = View.VISIBLE
                    connectionStatusText.text = AppModeManager.getStatusMessage()
                } else {
                    connectionStatusCard.visibility = View.GONE
                }
            }
        }
    }
}