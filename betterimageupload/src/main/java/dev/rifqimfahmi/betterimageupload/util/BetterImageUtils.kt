package dev.rifqimfahmi.betterimageupload.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore

object BetterImageUtils {

    fun getImageFilePath(context: Context?, uri: Uri): String? {
        try {
            if (DocumentsContract.isDocumentUri(context, uri) && isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                if (split[0] != "image") return null
                val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDataColumn(
        context: Context?,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            context?.contentResolver?.query(
                uri!!, projection, selection, selectionArgs, null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    val value = cursor.getString(columnIndex)
                    return if (
                        value.startsWith("content://") ||
                        !value.startsWith("/") &&
                        !value.startsWith("file://")
                    ) {
                        null
                    } else value
                }
            }
        } catch (ignore: Exception) {
        }
        return null
    }

}