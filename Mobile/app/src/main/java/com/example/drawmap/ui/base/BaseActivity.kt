package com.example.drawmap.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.drawmap.di.AppModeManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Базовый класс для всех Activity в приложении
 * Предоставляет общую функциональность
 */
abstract class BaseActivity : AppCompatActivity() {
    
    protected lateinit var permissionHelper: PermissionHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHelper = PermissionHelper(this)
        setupConnectionObserver()
    }

    private fun setupConnectionObserver() {
        lifecycleScope.launch {
            AppModeManager.connectivityStatus.collectLatest { status ->
                onConnectivityStatusChanged(status)
            }
        }
    }

    protected open fun onConnectivityStatusChanged(status: com.example.drawmap.utils.ApiConnectivityStatus) {
        // По умолчанию ничего не делаем
    }

    protected fun isOnline(): Boolean {
        return AppModeManager.isOnline()
    }
    
    /**
     * Получить текущий статус подключения
     */
    protected fun getConnectivityStatus(): com.example.drawmap.utils.ApiConnectivityStatus {
        return AppModeManager.getCurrentStatus()
    }
    
    /**
     * Получить сообщение о статусе подключения
     */
    protected fun getStatusMessage(): String {
        return AppModeManager.getStatusMessage()
    }
    
    /**
     * Показать сообщение (переопределите для кастомного отображения)
     */
    protected open fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Показать ошибку (переопределите для кастомного отображения)
     */
    protected open fun showError(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }
}
