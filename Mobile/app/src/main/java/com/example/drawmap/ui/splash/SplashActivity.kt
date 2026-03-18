package com.example.drawmap.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.R
import com.example.drawmap.di.AppModeManager
import com.example.drawmap.ui.base.BaseActivity
import com.example.drawmap.ui.base.UiConstants
import com.example.drawmap.ui.navigation.Navigator
import com.example.drawmap.viewModel.SplashViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Splash экран приложения
 * Проверяет разрешения, загружает данные и проверяет подключение к API
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var viewModel: SplashViewModel
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Инициализация
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        navigator = Navigator(this)

        // Наблюдаем за состоянием загрузки
        observeLoadingState()
        
        // Запрос разрешений и загрузка
        checkPermissionsAndLoad()
    }

    /**
     * Наблюдение за состоянием загрузки
     */
    private fun observeLoadingState() {
        lifecycleScope.launch {
            viewModel.loadingState.collectLatest { state ->
                when (state) {
                    is SplashViewModel.LoadingState.Loading -> {
                        // TODO: Показать прогресс с сообщением state.message
                    }
                    is SplashViewModel.LoadingState.Success -> {
                        if (!AppModeManager.isOnline()) {
                            showMessage(" ${state.message}\nПриложение работает в офлайн режиме")
                        }
                    }
                    is SplashViewModel.LoadingState.Error -> {
                        showError(state.message)
                    }
                    is SplashViewModel.LoadingState.Idle -> {
                        // Ничего не делаем
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isDataLoaded.collectLatest { isLoaded ->
                if (isLoaded) {
                    navigateToHome()
                }
            }
        }
    }
    
    /**
     * Проверка и запрос разрешений
     */
    private fun checkPermissionsAndLoad() {
        permissionHelper.requestAllRequiredPermissions(
            onGranted = {
                loadDataAndNavigate()
            },
            onDenied = { deniedPermissions ->
                showMessage("Для работы карты нужны разрешения \nОтклонено: ${deniedPermissions.size}")
                loadDataAndNavigate()
            }
        )
    }

    private fun loadDataAndNavigate() {
        viewModel.loadUserData(this)
    }

    private fun navigateToHome() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigator.navigateTo(Navigator.SCREEN_HOME)
            finish() // Закрываем Splash, чтобы не вернуться назад
        }, UiConstants.Timing.SPLASH_DURATION)
    }
}