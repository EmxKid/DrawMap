package com.example.drawmap.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import com.example.drawmap.data.model.Route

class HomeViewModel : ViewModel() {

    private var isRecording = false
    private val recordedPoints = mutableListOf<GeoPoint>()

    // --- repeat mode state ---
    private val _repeatRoute = MutableLiveData<Route?>(null)
    val repeatRoute: LiveData<Route?> = _repeatRoute

    private val _isRepeatMode = MutableLiveData<Boolean>(false)
    val isRepeatMode: LiveData<Boolean> = _isRepeatMode
    // -------------------------

    private val _userLocation = MutableLiveData<GeoPoint?>(null)
    val userLocation: LiveData<GeoPoint?> = _userLocation

    /**
     * Подготовить экран для повтора маршрута (наложение призрака и ожидание старта)
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
    fun onStartRecordingClick(onStart: () -> Unit) {
        if (!isRecording) {
            isRecording = true
            viewModelScope.launch {
                // TODO: Запуск GPS-трекинга
                onStart()
            }
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = GeoPoint(latitude, longitude)
    }

    /**
     * Остановка записи
     */
    fun stopRecording() {
        isRecording = false
        recordedPoints.clear()
    }

    /**
     * Добавление точки в маршрут (для будущего GPS-трекинга)
     */
    fun addPoint(latitude: Double, longitude: Double) {
        recordedPoints.add(GeoPoint(latitude, longitude))
    }

    /**
     * Статус записи
     */
    fun isRecordingActive(): Boolean = isRecording
}