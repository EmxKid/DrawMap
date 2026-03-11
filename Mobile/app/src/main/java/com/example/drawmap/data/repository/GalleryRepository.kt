package com.example.drawmap.data.repository

import com.example.drawmap.data.model.GalleryItem

interface GalleryRepository {
    suspend fun getGalleryItems(): List<GalleryItem>
}

