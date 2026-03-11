package com.example.drawmap.ui.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.drawmap.R
import com.example.drawmap.ui.navigation.Navigator

class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: SplashViewModel
    private lateinit var navigator: Navigator

    companion object {
        private const val SPLASH_DURATION = 2000L // 2 секунды показа
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Инициализация
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        navigator = Navigator(this)

        // Запрос разрешений и загрузка
        checkPermissionsAndLoad()
    }

    private fun checkPermissionsAndLoad() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // Все разрешения есть — загружаем данные
            loadDataAndNavigate()
        } else {
            // Запрашиваем недостающие разрешения
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                loadDataAndNavigate()
            } else {
                // Если пользователь отклонил критичные разрешения
                Toast.makeText(
                    this,
                    "Для работы карты нужны разрешения 🗺️",
                    Toast.LENGTH_LONG
                ).show()
                loadDataAndNavigate() // Всё равно продолжаем (для тестов)
            }
        }
    }

    private fun loadDataAndNavigate() {
        // Загружаем данные пользователя (кэш, настройки)
        viewModel.loadUserData()

        // Задержка для показа сплеша + навигация
        Handler(Looper.getMainLooper()).postDelayed({
            navigator.navigateTo(Navigator.SCREEN_HOME)
            finish() // Закрываем Splash, чтобы не вернуться назад
        }, SPLASH_DURATION)
    }
}