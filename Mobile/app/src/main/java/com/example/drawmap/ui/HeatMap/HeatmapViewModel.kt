package com.example.drawmap.ui.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawmap.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeatmapViewModel : ViewModel() {
    
    private val heatmapRepository = ServiceLocator.provideHeatmapRepository()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
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
     * Перезагрузить данные
     */
    fun refresh() {
        loadHeatmapData()
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
