package dev.rifqimfahmi.betterimageupload.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object BetterImageUtils {

    // TODO: use this https://github.com/Javernaut/WhatTheCodec/blob/46adaf56aa3a5f9731be2dfc5a1f95c21d0e2d01/app/src/main/java/com/javernaut/whatthecodec/PathUtil.java
    fun getImageFilePath(context: Context?, uri: Uri): String? {
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
                        context, contentUri, null, null
                    )
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    if (split[0] != "image") return null
                    val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(
                        context, contentUri, selection, selectionArgs
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
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