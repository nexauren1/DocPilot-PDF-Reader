package com.nexauren.docpilot.ui

import android.content.Context
import android.content.Intent
import android.Manifest
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.nexauren.docpilot.storage.DocumentAccess
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.nexauren.docpilot.ui.localization.LText
import com.nexauren.docpilot.ui.localization.DocPilotLocalization
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.screens.HomeScreen
import com.nexauren.docpilot.ui.screens.LibraryScreen
import com.nexauren.docpilot.ui.screens.PdfReaderScreen
import com.nexauren.docpilot.ui.screens.SettingsScreen
import com.nexauren.docpilot.ui.screens.ToolsScreen
import com.nexauren.docpilot.ui.components.DocumentPickerDialog
import com.nexauren.docpilot.ui.components.DocumentPickerType
import com.nexauren.docpilot.widget.PdfToolsWidgetProvider

private const val PREFS = "docpilot_documents"
private const val KEY_URIS = "uris"
private const val KEY_URIS_ORDERED = "uris_ordered"
private const val KEY_DOCUMENTS_JSON = "documents_json"

private enum class Destination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Início", Icons.Outlined.Home),
    LIBRARY("Biblioteca", Icons.Outlined.Folder),
    TOOLS("Ferramentas", Icons.Outlined.Tune),
    SETTINGS("Definições", Icons.Outlined.Settings),
}

@Composable
fun DocPilotApp(
    initialDestination: String? = null,
    initialToolCategory: String? = null,
    navigationRequestId: Int = 0,
) {
    val context = LocalContext.current
    var destination by remember { mutableStateOf(Destination.HOME) }
    var language by remember { mutableStateOf(DocPilotLocalization.language(context)) }
    var documents by remember { mutableStateOf(loadDocuments(context)) }
    var selectedDocument by remember { mutableStateOf<DocumentItem?>(null) }
    var hasDocumentAccess by remember { mutableStateOf(DocumentAccess.hasAccess(context)) }
    var isScanningDocuments by remember { mutableStateOf(false) }
    var showImportPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = selectedDocument != null) {
        selectedDocument = null
    }
    BackHandler(enabled = selectedDocument == null && destination != Destination.HOME) {
        destination = Destination.HOME
    }

    LaunchedEffect(initialDestination, navigationRequestId) {
        if (initialDestination == "tools") {
            // A widget shortcut must also exit the reader screen if the activity is already open.
            selectedDocument = null
            destination = Destination.TOOLS
        }
    }

    fun refreshDeviceDocuments() {
        hasDocumentAccess = DocumentAccess.hasAccess(context)
        if (!hasDocumentAccess || isScanningDocuments) return

        isScanningDocuments = true
        scope.launch {
            try {
                val discovered = withContext(Dispatchers.IO) {
                    DocumentAccess.scanPdfs(context)
                }
                documents = (discovered + documents).distinctBy { it.uri }
                saveDocuments(context, documents)
                snackbarHostState.showSnackbar(
                    "Biblioteca atualizada: ${discovered.size} documento(s) encontrados."
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("Não foi possível atualizar a biblioteca.")
            } finally {
                isScanningDocuments = false
            }
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

    if (selectedDocument != null) {
        PdfReaderScreen(
            document = selectedDocument!!,
            onBack = { selectedDocument = null },
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 22.dp, vertical = 24.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Icon(
                                    androidx.compose.material.icons.Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp)) {
                            LText("DocPilot", style = MaterialTheme.typography.headlineSmall)
                            LText(
                                "PDF Reader & Tools",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LText(
                                "PRIVADO • NO DISPOSITIVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    HorizontalDivider()
                    LText(
                        "ESPAÇO DE TRABALHO",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                    )
                    Destination.values().filter { it != Destination.SETTINGS }.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { LText(item.label) },
                            selected = destination == item,
                            onClick = {
                                selectedDocument = null
                                destination = item
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                    LText(
                        "PREFERÊNCIAS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 8.dp),
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Destination.SETTINGS.icon, contentDescription = null) },
                        label = { LText(Destination.SETTINGS.label) },
                        selected = destination == Destination.SETTINGS,
                        onClick = {
                            selectedDocument = null
                            destination = Destination.SETTINGS
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(3.dp)) {
                            LText("Os ficheiros ficam contigo", style = MaterialTheme.typography.titleSmall)
                            LText(
                                "Leitura e ferramentas executadas localmente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    ) {
        Scaffold(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    listOf(Destination.HOME, Destination.LIBRARY, Destination.TOOLS).forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { LText(item.label) },
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            AnimatedContent(
                targetState = destination,
                modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(paddingValues),
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith
                        fadeOut(animationSpec = tween(120))
                },
                label = "DocPilot destination transition",
            ) { targetDestination ->
                when (targetDestination) {
                    Destination.HOME -> HomeScreen(
                        documents = documents,
                        onImportPdf = { showImportPicker = true },
                        hasDocumentAccess = hasDocumentAccess,
                        onRequestDocumentAccess = { requestDocumentAccess() },
                        onRefreshDeviceDocuments = { refreshDeviceDocuments() },
                        onOpenDocument = { openDocument(it) },
                        onViewLibrary = { destination = Destination.LIBRARY },
                        onViewTools = { destination = Destination.TOOLS },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                    )

                    Destination.LIBRARY -> LibraryScreen(
                        documents = documents,
                        onOpenDocument = { openDocument(it) },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                    )

                    Destination.TOOLS -> ToolsScreen(
                        initialCategory = initialToolCategory,
                        categoryRequestId = navigationRequestId,
                        availableDocuments = documents,
                        hasStorageAccess = hasDocumentAccess,
                        onRequestDocumentAccess = { requestDocumentAccess() },
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
                        onOpenMenu = { scope.launch { drawerState.open() } },
                    )

                    Destination.SETTINGS -> SettingsScreen(
                        hasDocumentAccess = hasDocumentAccess,
                        selectedLanguage = language,
                        onLanguageChanged = { newLanguage ->
                            DocPilotLocalization.setLanguage(context, newLanguage)
                            language = newLanguage
                            PdfToolsWidgetProvider.refreshAll(context)
                        },
                        onRequestDocumentAccess = { requestDocumentAccess() },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                    )
                }
            }
        }
    }

    if (showImportPicker) {
        DocumentPickerDialog(
            title = "Abrir PDF",
            type = DocumentPickerType.PDF,
            multiple = false,
            knownDocuments = documents,
            hasStorageAccess = hasDocumentAccess,
            onRequestStorageAccess = { requestDocumentAccess() },
            onDismiss = { showImportPicker = false },
            onConfirm = { selected ->
                showImportPicker = false
                selected.firstOrNull()?.let { openDocument(it) }
            },
        )
    }
}

private fun loadDocuments(context: Context): List<DocumentItem> {
    val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Newer builds restore names directly and avoid one ContentResolver query per document
    // on every application start.
    val cachedDocuments = preferences.getString(KEY_DOCUMENTS_JSON, null)?.let { serialized ->
        runCatching {
            val array = org.json.JSONArray(serialized)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                DocumentItem(
                    name = item.optString("name", "Documento.pdf"),
                    uri = item.getString("uri"),
                )
            }
        }.getOrNull()
    }
    if (cachedDocuments != null) return cachedDocuments.distinctBy { it.uri }

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
    val distinctDocuments = documents.distinctBy { it.uri }
    val uris = distinctDocuments.map { it.uri }
    val documentJson = org.json.JSONArray().apply {
        distinctDocuments.forEach { document ->
            put(
                org.json.JSONObject()
                    .put("name", document.name)
                    .put("uri", document.uri)
            )
        }
    }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_DOCUMENTS_JSON, documentJson.toString())
        .putString(KEY_URIS_ORDERED, org.json.JSONArray(uris).toString())
        // Keep the legacy keys during migration so older builds can still read the library.
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
