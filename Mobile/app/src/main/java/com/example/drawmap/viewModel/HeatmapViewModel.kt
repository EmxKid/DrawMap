package com.example.drawmap.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawmap.di.ServiceLocator
import com.example.drawmap.ui.heatmap.HeatmapPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class HeatmapViewModel : ViewModel() {
    
    private val heatmapRepository = ServiceLocator.provideHeatmapRepository()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)

    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    /**
     * Загрузить данные тепловой карты
     */
    fun loadHeatmapData() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val points = heatmapRepository.getHeatmapData()
                
                if (points.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(points)
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Ошибка загрузки данных"
                )
            }
        }
    }

    /**
     * Обновление текущего местоположения пользователя
     */
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = GeoPoint(latitude, longitude)
    }

    /**
     * Состояния UI для тепловой карты
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val points: List<HeatmapPoint>) : UiState()
        object Empty : UiState()
        data class Error(val message: String) : UiState()
    }
}
