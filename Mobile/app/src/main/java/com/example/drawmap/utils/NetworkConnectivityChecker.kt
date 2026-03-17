package com.example.drawmap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.drawmap.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Утилита для проверки подключения к API
 */
object NetworkConnectivityChecker {
    
    private const val TAG = "NetworkChecker"
    
    /**
     * Проверяет наличие интернет соединения на устройстве
     */
    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Проверяет доступность API, перебирая возможные URL адреса
     */
    suspend fun isApiAvailable(): Boolean = withContext(Dispatchers.IO) {
        for (urlString in AppConfig.Network.POSSIBLE_API_URLS) {
            val result = checkSingleUrl(urlString)
            if (result != null) {
                if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                    Log.d(TAG, "API Check: SUCCESS with $urlString")
                }
                return@withContext result
            }
        }
        
        if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
            Log.w(TAG, "API Check: All URLs failed")
        }
        false
    }
    
    /**
     * Проверяет один конкретный URL
     * @return true если доступен, false если недоступен, null если нужно попробовать следующий URL
     */
    private fun checkSingleUrl(urlString: String): Boolean? {
        var connection: HttpURLConnection? = null
        
        return try {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: Trying $urlString")
            }
            
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = AppConfig.Api.API_CHECK_TIMEOUT
                readTimeout = AppConfig.Api.API_CHECK_TIMEOUT
                useCaches = false
                doOutput = false
            }
            
            val responseCode = connection.responseCode
            
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Response code = $responseCode")
            }
            
            // Считаем API доступным, если получили любой HTTP ответ (даже ошибку)
            responseCode in 200..599
            
        } catch (e: SocketTimeoutException) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Timeout, trying next URL")
            }
            null // Пробуем следующий URL
        } catch (e: Exception) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Exception: ${e.message}")
            }
            null // Пробуем следующий URL
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Проверяет подключение к API и возвращает статус подключения
     */
    suspend fun checkApiConnectivity(context: Context): ApiConnectivityStatus {
        if (!hasInternetConnection(context)) {
            return ApiConnectivityStatus.NO_INTERNET
        }
        
        return if (isApiAvailable()) {
            ApiConnectivityStatus.CONNECTED
        } else {
            ApiConnectivityStatus.API_UNAVAILABLE
        }
    }
    
    /**
     * Получить человекочитаемое описание статуса
     */
    fun getStatusMessage(status: ApiConnectivityStatus): String {
        return when (status) {
            ApiConnectivityStatus.CONNECTED -> "Подключено к серверу"
            ApiConnectivityStatus.NO_INTERNET -> "Нет подключения к интернету"
            ApiConnectivityStatus.API_UNAVAILABLE -> "Сервер недоступен"
        }
    }
}

enum class ApiConnectivityStatus {
    CONNECTED,           // API доступен
    NO_INTERNET,         // Нет интернета
    API_UNAVAILABLE      // Интернет есть, но API недоступен
}
