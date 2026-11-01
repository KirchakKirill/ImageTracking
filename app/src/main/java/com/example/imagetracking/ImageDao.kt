package com.example.imagetracking

import com.google.ar.core.AugmentedImageDatabase

class ImageDao(
    private val imageDb: AugmentedImageDatabase
) {
    private val imagesToIndexes = mutableMapOf<Int, ImageModel>()

    fun insertImage(model: ImageModel) {
        val index = imageDb.addImage(model.name, model.bitmap, model.imageWithInMeters)
        imagesToIndexes[index] = model.apply {
            bitmap = null
        }
    }
}