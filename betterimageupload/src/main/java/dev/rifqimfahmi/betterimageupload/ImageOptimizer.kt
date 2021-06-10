package dev.rifqimfahmi.betterimageupload

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.max

object ImageOptimizer {

    fun scaleAndSaveImage(
        bitmap: Bitmap?,
        compressFormat: Bitmap.CompressFormat?,
        maxWidth: Float,
        maxHeight: Float,
        quality: Int,
        minWidth: Int,
        minHeight: Int
    ): String? {
        bitmap ?: return null
        val photoW = bitmap.width.toFloat()
        val photoH = bitmap.height.toFloat()
        if (photoW == 0f || photoH == 0f) {
            return null
        }
        var scaleAnyway = false
        var scaleFactor = max(photoW / maxWidth, photoH / maxHeight)
        if (minWidth != 0 && minHeight != 0 && (photoW < minWidth || photoH < minHeight)) {
            scaleFactor = if (photoW < minWidth && photoH > minHeight) {
                photoW / minWidth
            } else if (photoW > minWidth && photoH < minHeight) {
                photoH / minHeight
            } else {
                max(photoW / minWidth, photoH / minHeight)
            }
            scaleAnyway = true
        }
        val w = (photoW / scaleFactor).toInt()
        val h = (photoH / scaleFactor).toInt()
        return scaleAndSaveImageInternal(
            bitmap, compressFormat, w, h,
            scaleFactor, quality, scaleAnyway
        )
    }

    private fun scaleAndSaveImageInternal(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat?,
        w: Int,
        h: Int,
        scaleFactor: Float,
        quality: Int,
        scaleAnyway: Boolean
    ): String? {
        val scaledBitmap: Bitmap = if (scaleFactor > 1 || scaleAnyway) {
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }
        val uniqueID = UUID.randomUUID().toString()
        val fileName = "test_optim_$uniqueID.jpg"
        val fileDir = File("/storage/emulated/0/Download/")
        val imageFile = File(fileDir, fileName)
        val stream = FileOutputStream(imageFile)
        scaledBitmap.compress(compressFormat, quality, stream) // compress bitmap here
        stream.close()
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        return imageFile.absolutePath
    }
}