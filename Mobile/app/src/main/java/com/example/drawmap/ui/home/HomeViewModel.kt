package com.example.drawmap.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawmap.data.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

/**
 * ViewModel для главного экрана с картой
 * Управляет состоянием записи маршрута и режимом повтора
 */
class HomeViewModel : ViewModel() {

    // Состояние записи маршрута
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val recordedPoints = mutableListOf<GeoPoint>()

    // Режим повтора маршрута
    private val _repeatRoute = MutableStateFlow<Route?>(null)
    val repeatRoute: StateFlow<Route?> = _repeatRoute.asStateFlow()

    private val _isRepeatMode = MutableStateFlow(false)
    val isRepeatMode: StateFlow<Boolean> = _isRepeatMode.asStateFlow()

    // Текущее местоположение пользователя
    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    /**
     * Подготовить экран для повтора маршрута
     */
    fun prepareForRepeat(route: Route) {
        _repeatRoute.value = route
        _isRepeatMode.value = true
    }

    /**
     * Сброс режима повтора
     */
    fun clearRepeat() {
        _repeatRoute.value = null
        _isRepeatMode.value = false
    }

    /**
     * Запуск записи маршрута
     */
    fun startRecording() {
        if (_recordingState.value !is RecordingState.Recording) {
            _recordingState.value = RecordingState.Recording
            recordedPoints.clear()
        }
    }
    
    /**
     * Запуск записи маршрута (старый API для совместимости)
     */
    fun onStartRecordingClick(onStart: () -> Unit) {
        if (_recordingState.value !is RecordingState.Recording) {
            _recordingState.value = RecordingState.Recording
            recordedPoints.clear()
            onStart()
        }
    }
    
    /**
     * Пауза записи маршрута
     */
    fun pauseRecording() {
        if (_recordingState.value is RecordingState.Recording) {
            _recordingState.value = RecordingState.Paused(recordedPoints.size)
        }
    }
    
    /**
     * Возобновление записи маршрута
     */
    fun resumeRecording() {
        if (_recordingState.value is RecordingState.Paused) {
            _recordingState.value = RecordingState.Recording
        }
    }

    /**
     * Остановка записи маршрута
     */
    fun stopRecording(): List<GeoPoint> {
        val points = recordedPoints.toList()
        _recordingState.value = RecordingState.Completed(points.size)
        recordedPoints.clear()
        return points
    }

    /**
     * Добавление точки в записываемый маршрут
     */
    fun addPoint(point: GeoPoint) {
        if (_recordingState.value is RecordingState.Recording) {
            recordedPoints.add(point)
        }
    }
    
    /**
     * Добавление точки по координатам
     */
    fun addPoint(latitude: Double, longitude: Double) {
        addPoint(GeoPoint(latitude, longitude))
    }

    /**
     * Обновление текущего местоположения пользователя
     */
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = GeoPoint(latitude, longitude)
    }
    
    /**
     * Обновление местоположения из GeoPoint
     */
    fun updateUserLocation(location: GeoPoint) {
        _userLocation.value = location
    }

    /**
     * Получить количество записанных точек
     */
    fun getRecordedPointsCount(): Int = recordedPoints.size
    
    /**
     * Проверить, идет ли запись
     */
    fun isRecordingActive(): Boolean = _recordingState.value is RecordingState.Recording

    /**
     * Состояния записи маршрута
     */
    sealed class RecordingState {
        object Idle : RecordingState()
        object Recording : RecordingState()
        data class Paused(val pointsRecorded: Int) : RecordingState()
        data class Completed(val totalPoints: Int) : RecordingState()
    }
}
