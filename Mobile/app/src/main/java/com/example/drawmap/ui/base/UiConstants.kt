package com.example.drawmap.ui.base

/**
 * UI константы для активностей и фрагментов
 */
object UiConstants {
    
    /**
     * Разрешения приложения
     */
    object Permissions {
        const val LOCATION_REQUEST_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
        const val STORAGE_REQUEST_CODE = 1003
        const val ALL_PERMISSIONS_REQUEST_CODE = 1000
        
        val LOCATION_PERMISSIONS = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val CAMERA_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
        
        val STORAGE_PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        val ALL_REQUIRED_PERMISSIONS = LOCATION_PERMISSIONS
    }
    
    /**
     * Конфигурация карты
     */
    object Map {
        const val DEFAULT_ZOOM = 15.0
        const val MIN_ZOOM = 5.0
        const val MAX_ZOOM = 20.0
        
        // Москва по умолчанию
        const val DEFAULT_LATITUDE = 55.7558
        const val DEFAULT_LONGITUDE = 37.6173
        
        const val ROUTE_LINE_WIDTH = 8f
        const val GHOST_ROUTE_LINE_WIDTH = 8f
        const val PATH_LINE_WIDTH = 8f
    }
    
    /**
     * Цвета маршрутов и элементов карты
     */
    object Colors {
        const val ROUTE_COLOR = 0x220000FF
        const val GHOST_ROUTE_COLOR = "#7C4DFF"
        const val PATH_TO_START_COLOR = "#FFEB3B"
        const val USER_MARKER_COLOR = 0xFF2196F3.toInt()
    }
    
    /**
     * Анимация и тайминги
     */
    object Timing {
        const val SPLASH_DURATION = 2000L
        const val MAP_LOAD_DELAY = 300L
        const val TOAST_DURATION_SHORT = 2000
        const val TOAST_DURATION_LONG = 3500
        const val CONNECTION_STATUS_DISPLAY_DURATION = 5000L // 5 секунд
    }
    
    /**
     * Intent extras ключи
     */
    object IntentKeys {
        const val REPEAT_ROUTE_ID = "repeat_route_id"
        const val ROUTE_ID = "route_id"
        const val PHOTO_URI = "photo_uri"
        const val GALLERY_ITEM_ID = "gallery_item_id"
    }
    
    /**
     * SharedPreferences ключи
     */
    object PrefsKeys {
        const val FIRST_LAUNCH = "first_launch"
        const val USER_ID = "user_id"
        const val ONLINE_MODE = "online_mode"
        const val LAST_LOCATION_LAT = "last_location_lat"
        const val LAST_LOCATION_LON = "last_location_lon"
    }
}
