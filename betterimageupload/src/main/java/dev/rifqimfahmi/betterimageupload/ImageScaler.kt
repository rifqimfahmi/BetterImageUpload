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

object ImageScaler {

    fun scaleBitmap(
        context: Context,
        imageUri: Uri,
        maxWidth: Float,
        maxHeight: Float,
        useMaxScale: Boolean
    ): Bitmap? {
        val bmOptions = decodeBitmapFromUri(context, imageUri)
        val photoW = bmOptions.outWidth.toFloat()
        val photoH = bmOptions.outHeight.toFloat()
        val scaleFactor = calculateScaleFactor(useMaxScale, photoW, maxWidth, photoH, maxHeight)
        calculateBmOptionInSampleSize(bmOptions, scaleFactor)
        val matrix = calculateImageMatrix(context, imageUri, scaleFactor, bmOptions) ?: return null
        return generateNewBitmap(context, imageUri, bmOptions, matrix)
    }

    private fun generateNewBitmap(
        context: Context,
        imageUri: Uri,
        bmOptions: BitmapFactory.Options,
        matrix: Matrix
    ): Bitmap? {
        var bitmap: Bitmap? = null
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        try {
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions)
            if (bitmap != null) {
                val newBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (newBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = newBitmap
                }
            }
            inputStream?.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun calculateImageMatrix(
        context: Context,
        imageUri: Uri,
        scaleFactor: Float,
        bmOptions: BitmapFactory.Options
    ): Matrix? {
        var scaleFactor1 = scaleFactor
        val input: InputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val exif = ExifInterface(input)
        val matrix = Matrix()
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
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

        scaleFactor1 /= bmOptions.inSampleSize.toFloat()
        if (scaleFactor1 > 1) {
            matrix.postScale(1.0f / scaleFactor1, 1.0f / scaleFactor1)
        }
        input.close()
        return matrix
    }

    private fun calculateBmOptionInSampleSize(
        bmOptions: BitmapFactory.Options,
        scaleFactor: Float
    ) {
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor.toInt()
        if (bmOptions.inSampleSize % 2 != 0) { // check if sample size is divisible by 2
            var sample = 1
            while (sample * 2 < bmOptions.inSampleSize) {
                sample *= 2
            }
            bmOptions.inSampleSize = sample
        }
    }

    private fun decodeBitmapFromUri(
        context: Context,
        imageUri: Uri
    ): BitmapFactory.Options {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val input: InputStream? = context.contentResolver.openInputStream(imageUri)
        BitmapFactory.decodeStream(input, null, bmOptions)
        input?.close()
        return bmOptions
    }

    private fun calculateScaleFactor(
        useMaxScale: Boolean,
        photoW: Float,
        maxWidth: Float,
        photoH: Float,
        maxHeight: Float
    ): Float {
        var scaleFactor = if (useMaxScale) max(
            photoW / maxWidth,
            photoH / maxHeight
        ) else min(photoW / maxWidth, photoH / maxHeight)
        if (scaleFactor < 1) {
            scaleFactor = 1f
        }
        return scaleFactor
    }

}