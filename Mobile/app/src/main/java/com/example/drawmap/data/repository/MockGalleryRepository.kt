package com.example.drawmap.data.repository

import com.example.drawmap.R
import com.example.drawmap.data.model.GalleryItem
import kotlinx.coroutines.delay

class MockGalleryRepository : GalleryRepository {
    override suspend fun getGalleryItems(): List<GalleryItem> {
        // Имитируем задержку сети/БД
        delay(100)
        return listOf(
            GalleryItem("r1", "Morning Walk", "Photo", R.drawable.ic_launcher_background),
            GalleryItem("r2", "Park Loop", "Photo", R.drawable.ic_launcher_background),
            GalleryItem("r3", "Evening Stroll", "Photo", R.drawable.ic_launcher_background)
        )
    }
}
