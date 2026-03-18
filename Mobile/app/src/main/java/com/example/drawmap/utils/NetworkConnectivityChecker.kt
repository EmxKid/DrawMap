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
        if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
            Log.d(TAG, "API Check: Starting check for ${AppConfig.Network.POSSIBLE_API_URLS.size} URLs")
        }
        
        for (urlString in AppConfig.Network.POSSIBLE_API_URLS) {
            val result = checkSingleUrl(urlString)
            if (result != null) {
                if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                    Log.d(TAG, "API Check: SUCCESS with $urlString - result=$result")
                }
                return@withContext result
            }
        }
        
        if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
            Log.w(TAG, "API Check: All URLs failed or timed out")
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
                setRequestProperty("Accept", "*/*")
            }
            
            connection.connect()
            val responseCode = connection.responseCode
            
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Response code = $responseCode")
            }
            
            // Считаем API доступным, если получили любой HTTP ответ (даже ошибку)
            // Важно: даже 404 означает, что сервер доступен
            when (responseCode) {
                in 200..299 -> true  // Успех
                in 300..599 -> {
                    Log.d(TAG, "API Check: Server responded with $responseCode (server is reachable)")
                    true  // Сервер отвечает, хотя и с ошибкой
                }
                else -> null  // Неизвестный код, пробуем следующий URL
            }
            
        } catch (e: SocketTimeoutException) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Timeout (${e.message}), trying next URL")
            }
            null // Пробуем следующий URL
        } catch (e: java.net.UnknownHostException) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Unknown host (${e.message}), trying next URL")
            }
            null // Пробуем следующий URL
        } catch (e: java.net.ConnectException) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Connection refused (${e.message}), trying next URL")
            }
            null // Пробуем следующий URL
        } catch (e: Exception) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "API Check: $urlString - Exception: ${e.javaClass.simpleName}: ${e.message}")
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
        // Сначала проверяем наличие активного сетевого подключения
        if (!hasInternetConnection(context)) {
            if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                Log.d(TAG, "No active network connection")
            }
            return ApiConnectivityStatus.NO_INTERNET
        }
        
        // Затем пробуем подключиться к API
        val apiAvailable = isApiAvailable()
        
        if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
            Log.d(TAG, "API availability check result: $apiAvailable")
        }
        
        return if (apiAvailable) {
            ApiConnectivityStatus.CONNECTED
        } else {
            // API недоступен, но сеть есть - проверяем реальное интернет-соединение
            val hasRealInternet = checkRealInternetConnection()
            if (hasRealInternet) {
                if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                    Log.d(TAG, "Internet is available but API is unreachable")
                }
                ApiConnectivityStatus.API_UNAVAILABLE
            } else {
                if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                    Log.d(TAG, "No real internet connection")
                }
                ApiConnectivityStatus.NO_INTERNET
            }
        }
    }
    
    /**
     * Проверяет реальное интернет-соединение через публичные DNS серверы
     */
    private suspend fun checkRealInternetConnection(): Boolean = withContext(Dispatchers.IO) {
        // Пробуем подключиться к Google DNS или Cloudflare DNS
        val testUrls = listOf(
            "https://www.google.com",
            "https://www.cloudflare.com",
            "https://1.1.1.1"
        )
        
        for (url in testUrls) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "HEAD"
                    connectTimeout = 3000
                    readTimeout = 3000
                    useCaches = false
                }
                
                connection.connect()
                val code = connection.responseCode
                connection.disconnect()
                
                if (code in 200..399) {
                    if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                        Log.d(TAG, "Real internet check: SUCCESS via $url")
                    }
                    return@withContext true
                }
            } catch (e: Exception) {
                if (AppConfig.Logging.ENABLE_NETWORK_LOGGING) {
                    Log.d(TAG, "Real internet check: Failed for $url - ${e.message}")
                }
            }
        }
        
        false
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
