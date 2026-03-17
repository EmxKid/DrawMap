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
 * ViewModel для экрана деталей маршрута
 * Управляет загрузкой маршрута и его отображением
 */
class RouteDetailViewModel : ViewModel() {

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
     * Состояния UI для экрана деталей маршрута
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val route: Route) : UiState()
        data class Error(val message: String) : UiState()
    }
}
