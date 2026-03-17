package com.example.drawmap.ui.navigation

import android.content.Context
import android.content.Intent
import com.example.drawmap.ui.gallery.GalleryActivity
import com.example.drawmap.ui.heatmap.HeatmapActivity
import com.example.drawmap.ui.home.HomeActivity
import com.example.drawmap.ui.splash.SplashActivity

class Navigator(private val context: Context) {

    companion object {
        val SCREEN_HEATMAP = "heatmap"
        const val SCREEN_SPLASH = "splash"
        const val SCREEN_HOME = "home"

        const val SCREEN_GALLERY = "gallery"
        const val SCREEN_ROUTE = "route"
        const val SCREEN_PHOTO = "photo"
    }

    fun navigateTo(screen: String) {
        val intent = when (screen) {
            SCREEN_HOME -> Intent(context, HomeActivity::class.java)
            SCREEN_GALLERY -> Intent(context, GalleryActivity::class.java)
            SCREEN_SPLASH -> Intent(context, SplashActivity::class.java)
            SCREEN_HEATMAP -> Intent(context, HeatmapActivity::class.java)
            SCREEN_ROUTE, SCREEN_PHOTO -> {
                // TODO: Добавить экраны позже
                return
            }
            else -> return
        }

        context.startActivity(intent)
    }
}