package com.example.drawmap.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawmap.di.AppModeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для Splash экрана
 * Управляет загрузкой данных и проверкой подключения
 */
class SplashViewModel : ViewModel() {

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    /**
     * Загрузка данных при старте приложения
     */
    fun loadUserData(context: Context) {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.Loading("Инициализация...")
                
                // 1. Проверка подключения к API
                _loadingState.value = LoadingState.Loading("Проверка подключения...")
                AppModeManager.checkAndSetMode(context)
                
                // 2. Загрузка настроек пользователя
                _loadingState.value = LoadingState.Loading("Загрузка настроек...")
                delay(300) // Имитация загрузки
                
                // TODO: Реальная загрузка:
                // - Проверка авторизации (если есть бэкенд)
                // - Загрузка кэшированных маршрутов из Room
                // - Инициализация настроек пользователя
                
                _isDataLoaded.value = true
                _loadingState.value = LoadingState.Success(
                    message = AppModeManager.getStatusMessage()
                )
                
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error(
                    message = "Ошибка инициализации: ${e.message}"
                )
                _isDataLoaded.value = true // Все равно продолжаем
            }
        }
    }

    /**
     * Очистка данных при выходе (опционально)
     */
    fun clearCache() {
        viewModelScope.launch {
            // TODO: Очистка временных данных
        }
    }
    
    /**
     * Состояния загрузки
     */
    sealed class LoadingState {
        object Idle : LoadingState()
        data class Loading(val message: String) : LoadingState()
        data class Success(val message: String) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }
}