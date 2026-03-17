package com.example.drawmap.ui.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawmap.data.model.Route
import com.example.drawmap.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для полноэкранного отображения маршрута
 */
class RouteFullScreenViewModel : ViewModel() {

    private val routeRepository = ServiceLocator.provideRouteRepository()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Загрузить маршрут по ID
     */
    fun loadRoute(routeId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                val route = routeRepository.getRouteById(routeId)

                if (route != null) {
                    _uiState.value = UiState.Success(route)
                } else {
                    _uiState.value = UiState.Error("Маршрут не найден")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Ошибка загрузки маршрута"
                )
            }
        }
    }

    /**
     * Получить информацию о маршруте для отображения
     */
    fun getRouteInfo(route: Route): RouteInfo {
        val distance = route.distanceMeters ?: 0.0
        val duration = route.durationSeconds ?: 0L
        
        return RouteInfo(
            title = route.title,
            distanceFormatted = formatDistance(distance),
            durationFormatted = formatDuration(duration)
        )
    }

    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000.0) {
            String.format("%.2f km", meters / 1000.0)
        } else {
            String.format("%.0f m", meters)
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    /**
     * Информация о маршруте для отображения
     */
    data class RouteInfo(
        val title: String,
        val distanceFormatted: String,
        val durationFormatted: String
    )

    /**
     * Состояния UI для полноэкранного режима
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val route: Route) : UiState()
        data class Error(val message: String) : UiState()
    }
}
