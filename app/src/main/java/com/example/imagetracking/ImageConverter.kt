package com.example.imagetracking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

class ImageConverter {

    fun convertImageToBitmap(context: Context, image: String): Bitmap {
        return context.assets.open(image).use { `is` ->
            BitmapFactory.decodeStream(`is`)
        }
    }

    fun convertImageUriToBitmap(context: Context, uri: Uri): Bitmap {
        return context.contentResolver.openInputStream(uri).use { `is` ->
            BitmapFactory.decodeStream(`is`)
        }
    }
}