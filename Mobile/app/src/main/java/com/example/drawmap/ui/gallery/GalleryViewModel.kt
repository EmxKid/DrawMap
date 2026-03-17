package com.example.drawmap.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawmap.data.model.GalleryItem
import com.example.drawmap.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана галереи
 */
class GalleryViewModel : ViewModel() {
    
    private val galleryRepository = ServiceLocator.provideGalleryRepository()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadGalleryItems() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val items = galleryRepository.getGalleryItems()
                
                if (items.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(items)
                }
                
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Ошибка загрузки галереи"
                )
            }
        }
    }
    
    /**
     * Состояния UI для галереи
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(
            val items: List<GalleryItem>,
            val allItems: List<GalleryItem> = items
        ) : UiState()
        object Empty : UiState()
        data class Error(val message: String) : UiState()
    }
}
