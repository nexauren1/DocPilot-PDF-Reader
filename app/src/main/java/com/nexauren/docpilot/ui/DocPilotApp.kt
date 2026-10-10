package com.nexauren.docpilot.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.screens.HomeScreen
import com.nexauren.docpilot.ui.screens.LibraryScreen
import com.nexauren.docpilot.ui.screens.PdfReaderScreen
import com.nexauren.docpilot.ui.screens.SettingsScreen
import com.nexauren.docpilot.ui.screens.ToolsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

private const val PREFS = "docpilot_documents"
private const val KEY_URIS = "uris"
private const val KEY_URIS_ORDERED = "uris_ordered"
private const val KEY_FOLDER_URI = "documents_folder_uri"
private const val KEY_FOLDER_SCANNED_URIS = "documents_folder_scanned_uris"
private const val KEY_FOLDER_INTRO_SHOWN = "documents_folder_intro_shown"

private enum class Destination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Início", Icons.Outlined.Home),
    LIBRARY("Biblioteca", Icons.Outlined.Folder),
    TOOLS("Ferramentas", Icons.Outlined.Tune),
    SETTINGS("Definições", Icons.Outlined.Settings),
}

@Composable
fun DocPilotApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var destination by remember { mutableStateOf(Destination.HOME) }
    var documents by remember { mutableStateOf(loadDocuments(context)) }
    var selectedDocument by remember { mutableStateOf<DocumentItem?>(null) }
    var folderUriString by remember { mutableStateOf(loadFolderUri(context)) }
    var scanning by remember { mutableStateOf(false) }
    var showFolderIntro by remember {
        mutableStateOf(folderUriString == null && !preferences.getBoolean(KEY_FOLDER_INTRO_SHOWN, false))
    }
    val snackbarHostState = remember { SnackbarHostState() }

    fun openDocument(document: DocumentItem) {
        documents = listOf(document) + documents.filterNot { it.uri == document.uri }
        saveDocuments(context, documents)
        selectedDocument = document
    }

    fun scanFolder(treeUri: Uri) {
        folderUriString = treeUri.toString()
        preferences.edit().putString(KEY_FOLDER_URI, treeUri.toString()).apply()
        scanning = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { scanPdfFolder(context, treeUri) }
            }
            result.onSuccess { found ->
                val previouslyIndexed = preferences.getStringSet(KEY_FOLDER_SCANNED_URIS, emptySet()).orEmpty()
                val foundUris = found.map { it.uri }.toSet()
                val combined = LinkedHashMap<String, DocumentItem>()
                found.forEach { combined[it.uri] = it }
                // Keep individually opened PDFs that did not originate from the connected folder.
                documents.filterNot { it.uri in previouslyIndexed || it.uri in foundUris }
                    .forEach { if (!combined.containsKey(it.uri)) combined[it.uri] = it }
                documents = combined.values.toList()
                saveDocuments(context, documents)
                preferences.edit().putStringSet(KEY_FOLDER_SCANNED_URIS, foundUris).apply()
                snackbarHostState.showSnackbar(
                    if (found.isEmpty()) "Não foram encontrados PDFs nesta pasta."
                    else "${found.size} PDFs encontrados e adicionados à biblioteca."
                )
            }.onFailure { error ->
                snackbarHostState.showSnackbar(
                    "Não foi possível ler esta pasta. Escolhe-a novamente nas definições. " +
                        (error.message?.take(100) ?: "")
                )
            }
            scanning = false
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { treeUri ->
        if (treeUri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            scanFolder(treeUri)
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            openDocument(
                DocumentItem(
                    name = queryDisplayName(context, uri.toString()),
                    uri = uri.toString(),
                )
            )
        }
    }

    fun chooseFolder() {
        folderPicker.launch(folderUriString?.let(Uri::parse))
    }

    fun refreshFolder() {
        val savedUri = folderUriString?.let(Uri::parse)
        if (savedUri == null) chooseFolder() else scanFolder(savedUri)
    }

    fun addSavedOutput(uriString: String) {
        val uri = Uri.parse(uriString)
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        val document = DocumentItem(
            name = queryDisplayName(context, uriString),
            uri = uriString,
        )
        documents = listOf(document) + documents.filterNot { it.uri == document.uri }
        saveDocuments(context, documents)
    }

    if (showFolderIntro) {
        AlertDialog(
            onDismissRequest = {
                preferences.edit().putBoolean(KEY_FOLDER_INTRO_SHOWN, true).apply()
                showFolderIntro = false
            },
            title = { Text("Liga a tua biblioteca") },
            text = {
                Text(
                    "Escolhe uma pasta como Downloads ou Documentos. O DocPilot procura PDFs " +
                        "nessa pasta e nas subpastas, para os mostrar numa biblioteca única. " +
                        "Só terá acesso à pasta que escolheres; os ficheiros são processados no dispositivo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferences.edit().putBoolean(KEY_FOLDER_INTRO_SHOWN, true).apply()
                        showFolderIntro = false
                        chooseFolder()
                    }
                ) { Text("Escolher pasta") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        preferences.edit().putBoolean(KEY_FOLDER_INTRO_SHOWN, true).apply()
                        showFolderIntro = false
                    }
                ) { Text("Agora não") }
            },
        )
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
                        label = { Text(item.label) },
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
                    folderConnected = folderUriString != null,
                    folderName = folderUriString?.let { folderDisplayName(context, Uri.parse(it)) },
                    scanning = scanning,
                    onChooseFolder = { chooseFolder() },
                    onRefreshFolder = { refreshFolder() },
                    onOpenSinglePdf = { pdfPicker.launch(arrayOf("application/pdf")) },
                    onOpenDocument = { openDocument(it) },
                    onViewLibrary = { destination = Destination.LIBRARY },
                    onViewTools = { destination = Destination.TOOLS },
                )
                Destination.LIBRARY -> LibraryScreen(
                    documents = documents,
                    folderConnected = folderUriString != null,
                    folderName = folderUriString?.let { folderDisplayName(context, Uri.parse(it)) },
                    scanning = scanning,
                    onChooseFolder = { chooseFolder() },
                    onRefreshFolder = { refreshFolder() },
                    onOpenSinglePdf = { pdfPicker.launch(arrayOf("application/pdf")) },
                    onOpenDocument = { openDocument(it) },
                )
                Destination.TOOLS -> ToolsScreen(onOutputSaved = { addSavedOutput(it) })
                Destination.SETTINGS -> SettingsScreen(
                    folderConnected = folderUriString != null,
                    folderName = folderUriString?.let { folderDisplayName(context, Uri.parse(it)) },
                    scanning = scanning,
                    onChooseFolder = { chooseFolder() },
                    onRefreshFolder = { refreshFolder() },
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
    // Migrate installations that only stored an unordered URI set.
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
        // Keep the legacy key so older preview builds can still read the library.
        .putStringSet(KEY_URIS, uris.toSet())
        .apply()
}

private fun loadFolderUri(context: Context): String? {
    val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FOLDER_URI, null)
        ?: return null
    val permissionStillHeld = context.contentResolver.persistedUriPermissions.any {
        it.uri.toString() == saved && it.isReadPermission
    }
    return saved.takeIf { permissionStillHeld }
}

private fun folderDisplayName(context: Context, treeUri: Uri): String =
    runCatching { DocumentFile.fromTreeUri(context, treeUri)?.name }.getOrNull()
        ?.takeIf { it.isNotBlank() } ?: "Pasta selecionada"

private fun scanPdfFolder(context: Context, treeUri: Uri): List<DocumentItem> {
    val root = DocumentFile.fromTreeUri(context, treeUri)
        ?: throw IllegalArgumentException("A pasta não está disponível.")
    require(root.isDirectory) { "Escolhe uma pasta válida." }

    val queue = ArrayDeque<Pair<DocumentFile, Int>>()
    val found = LinkedHashMap<String, DocumentItem>()
    queue.addLast(root to 0)

    // Defensive bounds keep very large or unusual provider trees from consuming excessive resources.
    while (queue.isNotEmpty() && found.size < 3000) {
        val (directory, depth) = queue.removeFirst()
        val children = runCatching { directory.listFiles().toList() }.getOrDefault(emptyList())
        children.forEach { child ->
            runCatching {
                if (child.isDirectory && depth < 12) {
                    queue.addLast(child to depth + 1)
                } else if (child.isFile) {
                    val name = child.name.orEmpty()
                    val isPdf = name.endsWith(".pdf", ignoreCase = true) ||
                        child.type.equals("application/pdf", ignoreCase = true)
                    if (isPdf && child.uri.toString() !in found) {
                        found[child.uri.toString()] = DocumentItem(
                            name = name.ifBlank { queryDisplayName(context, child.uri.toString()) },
                            uri = child.uri.toString(),
                        )
                    }
                }
            }
        }
    }
    return found.values.sortedBy { it.name.lowercase() }
}

private fun queryDisplayName(context: Context, uriString: String): String {
    val uri = Uri.parse(uriString)
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
    return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "Documento.pdf"
}
