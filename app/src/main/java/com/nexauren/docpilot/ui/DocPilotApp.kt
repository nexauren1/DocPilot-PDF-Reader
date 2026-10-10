package com.nexauren.docpilot.ui

import android.content.Context
import android.content.Intent
import android.Manifest
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.nexauren.docpilot.storage.DocumentAccess
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.screens.HomeScreen
import com.nexauren.docpilot.ui.screens.LibraryScreen
import com.nexauren.docpilot.ui.screens.PdfReaderScreen
import com.nexauren.docpilot.ui.screens.SettingsScreen
import com.nexauren.docpilot.ui.screens.ToolsScreen

private const val PREFS = "docpilot_documents"
private const val KEY_URIS = "uris"
private const val KEY_URIS_ORDERED = "uris_ordered"

private enum class Destination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Início", Icons.Outlined.Home),
    LIBRARY("Biblioteca", Icons.Outlined.Folder),
    TOOLS("Ferramentas", Icons.Outlined.Tune),
    SETTINGS("Definições", Icons.Outlined.Settings),
}

@Composable
fun DocPilotApp() {
    val context = LocalContext.current
    var destination by remember { mutableStateOf(Destination.HOME) }
    var documents by remember { mutableStateOf(loadDocuments(context)) }
    var selectedDocument by remember { mutableStateOf<DocumentItem?>(null) }
    var hasDocumentAccess by remember { mutableStateOf(DocumentAccess.hasAccess(context)) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun refreshDeviceDocuments() {
        hasDocumentAccess = DocumentAccess.hasAccess(context)
        if (hasDocumentAccess) {
            val discovered = DocumentAccess.scanPdfs(context)
            documents = (discovered + documents).distinctBy { it.uri }
            saveDocuments(context, documents)
        }
    }

    val broadAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        refreshDeviceDocuments()
    }

    val legacyStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasDocumentAccess = granted || DocumentAccess.hasAccess(context)
        if (hasDocumentAccess) refreshDeviceDocuments()
    }

    fun requestDocumentAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (DocumentAccess.hasAccess(context)) {
                refreshDeviceDocuments()
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                )
                broadAccessLauncher.launch(intent)
            }
        } else {
            legacyStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun openDocument(document: DocumentItem) {
        documents = listOf(document) + documents.filterNot { it.uri == document.uri }
        saveDocuments(context, documents)
        selectedDocument = document
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            val document = DocumentItem(
                name = queryDisplayName(context, uri.toString()),
                uri = uri.toString(),
            )
            openDocument(document)
        }
    }

    if (selectedDocument != null) {
        PdfReaderScreen(
            document = selectedDocument!!,
            onBack = { selectedDocument = null },
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { androidx.compose.material3.Text(item.label) },
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            when (destination) {
                Destination.HOME -> HomeScreen(
                    documents = documents,
                    onImportPdf = { picker.launch(arrayOf("application/pdf")) },
                    hasDocumentAccess = hasDocumentAccess,
                    onRequestDocumentAccess = { requestDocumentAccess() },
                    onRefreshDeviceDocuments = { refreshDeviceDocuments() },
                    onOpenDocument = { openDocument(it) },
                    onViewLibrary = { destination = Destination.LIBRARY },
                    onViewTools = { destination = Destination.TOOLS },
                )
                Destination.LIBRARY -> LibraryScreen(
                    documents = documents,
                    onOpenDocument = { openDocument(it) },
                )
                Destination.TOOLS -> ToolsScreen(
                    onOutputSaved = { uriString ->
                        val outputUri = android.net.Uri.parse(uriString)
                        runCatching {
                            context.contentResolver.takePersistableUriPermission(
                                outputUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                            )
                        }
                        val document = DocumentItem(
                            name = queryDisplayName(context, uriString),
                            uri = uriString,
                        )
                        documents = listOf(document) + documents.filterNot { it.uri == document.uri }
                        saveDocuments(context, documents)
                    },
                )
                Destination.SETTINGS -> SettingsScreen(
                    hasDocumentAccess = hasDocumentAccess,
                    onRequestDocumentAccess = { requestDocumentAccess() },
                )
            }
        }
    }
}

private fun loadDocuments(context: Context): List<DocumentItem> {
    val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val orderedUris = preferences.getString(KEY_URIS_ORDERED, null)?.let { serialized ->
        runCatching {
            val array = org.json.JSONArray(serialized)
            List(array.length()) { index -> array.getString(index) }
        }.getOrNull()
    }
    // Migrate installations that only have the original unordered StringSet.
    val raw = orderedUris ?: preferences.getStringSet(KEY_URIS, emptySet()).orEmpty().toList()
    return raw.distinct().mapNotNull { uriString ->
        runCatching {
            DocumentItem(
                name = queryDisplayName(context, uriString),
                uri = uriString,
            )
        }.getOrNull()
    }
}

private fun saveDocuments(context: Context, documents: List<DocumentItem>) {
    val uris = documents.map { it.uri }.distinct()
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_URIS_ORDERED, org.json.JSONArray(uris).toString())
        // Keep the legacy key during migration so older builds can still read the library.
        .putStringSet(KEY_URIS, uris.toSet())
        .apply()
}

private fun queryDisplayName(context: Context, uriString: String): String {
    val uri = android.net.Uri.parse(uriString)
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) return cursor.getString(index)
        }
    }
    return "Documento.pdf"
}
