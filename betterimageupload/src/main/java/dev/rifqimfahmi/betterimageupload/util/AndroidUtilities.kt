package dev.rifqimfahmi.betterimageupload.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object AndroidUtilities {

    fun getPath(context: Context?, uri: Uri): String? {
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                    return getDataColumn(
                        context,
                        contentUri,
                        null,
                        null
                    )
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
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
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getDataColumn(
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
            context?.contentResolver?.query(uri!!, projection, selection, selectionArgs, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        val value = cursor.getString(column_index)
                        return if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith(
                                "file://"
                            )
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