package com.example.drawmap.data.mock

import com.example.drawmap.data.model.Location
import com.example.drawmap.data.model.Photo
import com.example.drawmap.data.model.Route

object MockData {

    val mockRoutes = listOf(
        Route(
            id = "route_001",
            name = "Прогулка по парку",
            coordinates = listOf(
                Location(55.751244, 37.618423),
                Location(55.752244, 37.619423),
                Location(55.753244, 37.620423)
            ),
            photoIds = listOf("photo_001", "photo_002"),
            createdAt = "2025-03-01T10:00:00Z"
        ),
        Route(
            id = "route_002",
            name = "Велопрогулка по набережной",
            coordinates = listOf(
                Location(55.755244, 37.615423),
                Location(55.756244, 37.616423)
            ),
            photoIds = listOf("photo_003"),
            createdAt = "2025-03-05T14:30:00Z"
        )
    )

    val mockPhotos = listOf(
        Photo(id = "photo_001", routeId = "route_001", imageUrl = "mock_image_1"),
        Photo(id = "photo_002", routeId = "route_001", imageUrl = "mock_image_2"),
        Photo(id = "photo_003", routeId = "route_002", imageUrl = "mock_image_3")
    )
}