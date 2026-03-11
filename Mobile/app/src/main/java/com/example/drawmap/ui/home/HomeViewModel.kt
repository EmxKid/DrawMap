package com.example.drawmap.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class HomeViewModel : ViewModel() {

    private var isRecording = false
    private val recordedPoints = mutableListOf<GeoPoint>()

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