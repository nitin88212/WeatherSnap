package util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

data class CompressedImage(
    val path: String,
    val originalSize: Long,
    val compressedSize: Long
)

fun compressImageFile(source: File, destination: File, quality: Int = 72): CompressedImage {
    val bitmap = BitmapFactory.decodeFile(source.absolutePath)
    FileOutputStream(destination).use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
    }
    return CompressedImage(
        path = destination.absolutePath,
        originalSize = source.length(),
        compressedSize = destination.length()
    )
}
