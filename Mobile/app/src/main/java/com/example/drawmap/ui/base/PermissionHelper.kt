package com.example.drawmap.ui.base

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionHelper(private val activity: AppCompatActivity) {
    
    private var onPermissionsGranted: (() -> Unit)? = null
    private var onPermissionsDenied: ((List<String>) -> Unit)? = null
    
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }.keys.toList()
            
            if (deniedPermissions.isEmpty()) {
                onPermissionsGranted?.invoke()
            } else {
                onPermissionsDenied?.invoke(deniedPermissions)
            }
        }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(
        permissions: Array<String>,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        onPermissionsGranted = onGranted
        onPermissionsDenied = onDenied

        // Фильтруем уже предоставленные разрешения
        val missingPermissions = permissions.filter { permission ->
            !hasPermission(permission)
        }.toTypedArray()
        
        if (missingPermissions.isEmpty()) {
            onGranted()
        } else {
            permissionLauncher.launch(missingPermissions)
        }
    }

    fun requestLocationPermissions(
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        requestPermissions(
            UiConstants.Permissions.LOCATION_PERMISSIONS,
            onGranted,
            onDenied
        )
    }

    fun requestAllRequiredPermissions(
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit = {}
    ) {
        requestPermissions(
            UiConstants.Permissions.ALL_REQUIRED_PERMISSIONS,
            onGranted,
            onDenied
        )
    }
    
    companion object {
        fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
            return permissions.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun getMissingPermissions(context: Context, permissions: Array<String>): List<String> {
            return permissions.filter { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            }
        }
    }
}
