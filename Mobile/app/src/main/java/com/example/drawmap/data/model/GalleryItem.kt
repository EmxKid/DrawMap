package com.example.drawmap.data.model

import androidx.annotation.DrawableRes

/**
 * Простая модель для элемента галереи (карточки)
 */
data class GalleryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val imageRes: Int
)

