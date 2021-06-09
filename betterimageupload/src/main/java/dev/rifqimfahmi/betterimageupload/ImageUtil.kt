package dev.rifqimfahmi.betterimageupload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.math.max
import kotlin.math.min

object ImageUtil {

    fun loadBitmap(
        context: Context,
        imageUri: Uri,
        maxWidth: Float,
        maxHeight: Float,
        useMaxScale: Boolean
    ): Bitmap? {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        BitmapFactory.decodeStream(inputStream, null, bmOptions)
        val photoW = bmOptions.outWidth.toFloat()
        val photoH = bmOptions.outHeight.toFloat() // original photo. add log below
        var scaleFactor = if (useMaxScale) Math.max(
            photoW / maxWidth,
            photoH / maxHeight
        ) else min(photoW / maxWidth, photoH / maxHeight)
        if (scaleFactor < 1) {
            scaleFactor = 1f
        }
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor.toInt()
        if (bmOptions.inSampleSize % 2 != 0) { // check if sample size is divisible by 2
            var sample = 1
            while (sample * 2 < bmOptions.inSampleSize) {
                sample *= 2
            }
            bmOptions.inSampleSize = sample
        }
        var matrix: Matrix? = null
        try {
            val exif = ExifInterface(inputStream!!)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                1
            )
            matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(
                    90f
                )
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(
                    180f
                )
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(
                    270f
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        scaleFactor /= bmOptions.inSampleSize.toFloat()
        if (scaleFactor > 1) {
            if (matrix == null) {
                matrix = Matrix()
            }
            matrix.postScale(1.0f / scaleFactor, 1.0f / scaleFactor)
        }
        var bitmap: Bitmap? = null
        inputStream?.close()
        val inputStream2: InputStream? = context.contentResolver.openInputStream(imageUri)
        try {
            bitmap = BitmapFactory.decodeStream(inputStream2, null, bmOptions)
            if (bitmap != null) {
                val newBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (newBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = newBitmap
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return bitmap
    }

    fun scaleAndSaveImage(
        bitmap: Bitmap?,
        maxWidth: Float,
        maxHeight: Float,
        quality: Int,
        minWidth: Int,
        minHeight: Int
    ): String? {
        return scaleAndSaveImage(
            bitmap, CompressFormat.JPEG, maxWidth, maxHeight,
            quality, minWidth, minHeight
        )
    }

    fun scaleAndSaveImage(
        bitmap: Bitmap?,
        compressFormat: CompressFormat?,
        maxWidth: Float,
        maxHeight: Float,
        quality: Int,
        minWidth: Int,
        minHeight: Int
    ): String? {
        if (bitmap == null) {
            return null
        }
        val photoW = bitmap.width.toFloat()
        val photoH = bitmap.height.toFloat()
        if (photoW == 0f || photoH == 0f) {
            return null
        }
        var scaleAnyway = false
        var scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight)
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
        if (h == 0 || w == 0) {
            null
        } else try {
            return scaleAndSaveImageInternal(
                bitmap, compressFormat, w, h,
                scaleFactor, quality, scaleAnyway
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return ""
    }

    private fun scaleAndSaveImageInternal(
        bitmap: Bitmap,
        compressFormat: CompressFormat?,
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