package com.example.drawmap.config

/**
 * Централизованная конфигурация приложения
 */
object AppConfig {
    
    /**
     * Конфигурация API
     */
    object Api {
        // В эмуляторе Android localhost компьютера доступен по адресу 10.0.2.2
        // На реальном устройстве нужно использовать IP адрес компьютера в сети
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/"
        const val LOCALHOST_BASE_URL = "http://localhost:8080/"
        
        // Используем по умолчанию URL для эмулятора
        const val DEFAULT_BASE_URL = EMULATOR_BASE_URL
        
        // Endpoints
        const val ROUTE_ENDPOINT = "api/route"
        const val HEATMAP_ENDPOINT = "api/heatmap"
        
        // Таймауты (в миллисекундах)
        const val CONNECT_TIMEOUT = 30_000L
        const val READ_TIMEOUT = 30_000L
        const val WRITE_TIMEOUT = 30_000L
        
        // Таймаут для проверки доступности API (в миллисекундах)
        const val API_CHECK_TIMEOUT = 5_000
    }
    
    /**
     * Конфигурация сети и подключений
     */
    object Network {
        val POSSIBLE_API_URLS = listOf(
            Api.EMULATOR_BASE_URL + Api.HEATMAP_ENDPOINT,
            Api.LOCALHOST_BASE_URL + Api.HEATMAP_ENDPOINT
        )
        
        const val MAX_RETRY_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1000L
    }
    
    /**
     * Конфигурация кеширования
     */
    object Cache {
        const val MAX_CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
        const val CACHE_DIR_NAME = "http_cache"
    }
    
    /**
     * Конфигурация логирования
     */
    object Logging {
        const val ENABLE_HTTP_LOGGING = true
        const val ENABLE_NETWORK_LOGGING = true
    }
}
