package com.example.drawmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Кнопка начала маршрута
        val startRouteButton = findViewById<Button>(R.id.startRouteButton)
        startRouteButton.setOnClickListener {
            Toast.makeText(this, "Начинаем запись маршрута!", Toast.LENGTH_SHORT).show()
            // Здесь будет переход на экран записи
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
    }
}