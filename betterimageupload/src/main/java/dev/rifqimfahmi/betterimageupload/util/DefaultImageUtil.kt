package dev.rifqimfahmi.betterimageupload.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.FileInputStream
import java.io.InputStream

class DefaultImageUtil: ImageUtil {
    override suspend fun getImageFileSize(
        ctx: Context,
        imageUri: Uri
    ): Long {
        var input: FileInputStream? = null
        try {
            input = ctx.contentResolver.openInputStream(imageUri) as FileInputStream
            return input.channel.size()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
        }
        return 0
    }

    override suspend fun decodeImageMetaData(
        ctx: Context,
        imageUri: Uri
    ): BitmapFactory.Options? {
        var input: InputStream? = null
        try {
            val bmOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            input = ctx.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(input, null, bmOptions)
            return bmOptions
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            input?.close()
        }
        return null
    }
}