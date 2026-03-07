package com.example.drawmap

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        // Элементы
        val routesFolderCard = findViewById<CardView>(R.id.routesFolderCard)
        val photosFolderCard = findViewById<CardView>(R.id.photosFolderCard)
        val routesCountText = findViewById<TextView>(R.id.routesCountText)
        val photosCountText = findViewById<TextView>(R.id.photosCountText)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Устанавливаем количество из моковых данных (пока хардкод)
        routesCountText.text = "3 маршрута"
        photosCountText.text = "12 фото"

        // Клик на папку "Сохраненные маршруты"
        routesFolderCard.setOnClickListener {
            Toast.makeText(this, "Открываем сохраненные маршруты", Toast.LENGTH_SHORT).show()
            // Здесь будет переход на экран списка маршрутов
            // val intent = Intent(this, RoutesListActivity::class.java)
            // startActivity(intent)
        }

        // Клик на папку "Сохраненные фото"
        photosFolderCard.setOnClickListener {
            Toast.makeText(this, "Открываем сохраненные фото", Toast.LENGTH_SHORT).show()
            // Здесь будет переход на экран списка фото
            // val intent = Intent(this, PhotosListActivity::class.java)
            // startActivity(intent)
        }

        // Нижняя навигация
        bottomNavigationView.selectedItemId = R.id.navigation_gallery

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gallery -> {
                    true
                }
                R.id.navigation_home -> {
                    // Переход на главный экран
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_heatmap -> {
                    Toast.makeText(this, "Открываем heatmap", Toast.LENGTH_SHORT).show()
                    // Здесь переход на HeatmapActivity
                    true
                }
                else -> false
            }
        }
    }
}