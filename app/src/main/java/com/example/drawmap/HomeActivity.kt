package com.example.drawmap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Кнопка начала маршрута
        val startRouteButton = findViewById<Button>(R.id.startRouteButton)
        val pauseButton = findViewById<Button>(R.id.pauseButton)
        val stopButton = findViewById<Button>(R.id.stopButton)
        val photoButton = findViewById<Button>(R.id.photoButton)

        startRouteButton.setOnClickListener {
            Toast.makeText(this, "Начинаем запись маршрута!", Toast.LENGTH_SHORT).show()

            startRouteButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
            photoButton.visibility = View.VISIBLE
        }

        pauseButton.setOnClickListener {
            Toast.makeText(this, "Пауза маршрута (заглушка)", Toast.LENGTH_SHORT).show()
        }

        stopButton.setOnClickListener {
            Toast.makeText(this, "Останавливаем запись маршрута!", Toast.LENGTH_SHORT).show()

            startRouteButton.visibility = View.VISIBLE
            pauseButton.visibility = View.GONE
            stopButton.visibility = View.GONE
            photoButton.visibility = View.GONE
        }

        photoButton.setOnClickListener {
            Toast.makeText(this, "Фото сохранено (заглушка)", Toast.LENGTH_SHORT).show()
        }

        // Нижняя навигация
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gallery -> {
                    // Переход на экран галереи
                    val intent = Intent(this, GalleryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_home -> {
                    true
                }
                R.id.navigation_heatmap -> {
                    Toast.makeText(this, "Открываем heatmap", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Инициализация фрагмента карты
        val existingFragment =
            supportFragmentManager.findFragmentById(R.id.contentFrame) as? SupportMapFragment
        val mapFragment = existingFragment ?: SupportMapFragment.newInstance().also { fragment ->
            supportFragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit()
        }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Пример: центрируем карту на Москве
        val moscow = LatLng(55.7558, 37.6173)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscow, 12f))
    }
}