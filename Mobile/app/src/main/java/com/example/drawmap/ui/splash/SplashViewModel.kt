package com.example.drawmap.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    // Простой флаг загрузки (можно расширить позже)
    private var _isDataLoaded = false
    val isDataLoaded: Boolean
        get() = _isDataLoaded

    /**
     * Загрузка данных при старте приложения
     */
    fun loadUserData() {
        viewModelScope.launch {
            // TODO: Здесь будет реальная загрузка:
            // 1. Проверка авторизации (если есть бэкенд)
            // 2. Загрузка кэшированных маршрутов из Room
            // 3. Инициализация настроек пользователя

            // Для примера просто имитируем загрузку
            _isDataLoaded = true
        }
    }

    /**
     * Очистка данных при выходе (опционально)
     */
    fun clearCache() {
        // TODO: Очистка временных данных
    }
}