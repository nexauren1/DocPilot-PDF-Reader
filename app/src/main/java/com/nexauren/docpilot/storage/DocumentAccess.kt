package com.nexauren.docpilot.storage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.nexauren.docpilot.model.DocumentItem

object DocumentAccess {
    fun hasAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }

    fun scanPdfs(context: Context): List<DocumentItem> {
        if (!hasAccess(context)) return emptyList()
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
        )
        val selection = "(" + MediaStore.Files.FileColumns.MIME_TYPE + " = ? OR " +
            MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?)"
        val selectionArgs = arrayOf("application/pdf", "%.pdf")
        val sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        val found = mutableListOf<DocumentItem>()
        try {
            context.contentResolver.query(
                collection, projection, selection, selectionArgs, sortOrder,
            )?.use { rows ->
                val idColumn = rows.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = rows.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                while (rows.moveToNext()) {
                    val id = rows.getLong(idColumn)
                    val name = rows.getString(nameColumn) ?: "Documento.pdf"
                    found += DocumentItem(
                        name = name,
                        uri = android.content.ContentUris.withAppendedId(collection, id).toString(),
                    )
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: IllegalArgumentException) {
            return emptyList()
        }
        return found.distinctBy { it.uri }
    }
}
