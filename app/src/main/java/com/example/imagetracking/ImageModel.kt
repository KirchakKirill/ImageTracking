package com.example.imagetracking

import android.graphics.Bitmap
import java.util.UUID

data class ImageModel(
    val name: String = UUID.randomUUID().toString(),
    val imageWithInMeters: Float = 0.1f,
    var bitmap: Bitmap? = null
)