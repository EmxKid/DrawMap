package com.example.drawmap.ui.gallery

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drawmap.R
import com.example.drawmap.ui.navigation.Navigator

class GalleryActivity : AppCompatActivity() {

    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        navigator = Navigator(this)

        // Кнопка "Назад"
        findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            finish() // Просто закрываем экран
        }

        // Заглушка для контента
        findViewById<Button>(R.id.btnViewRoute)?.setOnClickListener {
            Toast.makeText(this, "Просмотр маршрута (заглушка)", Toast.LENGTH_SHORT).show()
        }
    }
}