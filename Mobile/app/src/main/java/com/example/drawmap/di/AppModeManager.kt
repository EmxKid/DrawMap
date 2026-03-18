package com.example.drawmap.di

import android.content.Context
import android.util.Log
import com.example.drawmap.utils.ApiConnectivityStatus
import com.example.drawmap.utils.NetworkConnectivityChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppModeManager {
    
    private const val TAG = "AppModeManager"
    
    private val _isOnlineMode = MutableStateFlow(false)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()
    
    private val _connectivityStatus = MutableStateFlow(ApiConnectivityStatus.API_UNAVAILABLE)
    val connectivityStatus: StateFlow<ApiConnectivityStatus> = _connectivityStatus.asStateFlow()

    suspend fun checkAndSetMode(context: Context) {
        Log.d(TAG, "Checking connectivity...")
        val status = NetworkConnectivityChecker.checkApiConnectivity(context)
        updateStatus(status)
        Log.d(TAG, "Connectivity status: $status, Online mode: ${_isOnlineMode.value}")
    }

    private fun updateStatus(status: ApiConnectivityStatus) {
        _connectivityStatus.value = status
        _isOnlineMode.value = (status == ApiConnectivityStatus.CONNECTED)
    }

    fun forceOfflineMode() {
        Log.d(TAG, "Forcing offline mode")
        updateStatus(ApiConnectivityStatus.API_UNAVAILABLE)
    }

    fun forceOnlineMode() {
        Log.d(TAG, "Forcing online mode")
        updateStatus(ApiConnectivityStatus.CONNECTED)
    }

    fun getStatusMessage(): String {
        return NetworkConnectivityChecker.getStatusMessage(_connectivityStatus.value)
    }

    fun isOnline(): Boolean = _isOnlineMode.value

    fun getCurrentStatus(): ApiConnectivityStatus = _connectivityStatus.value
}
