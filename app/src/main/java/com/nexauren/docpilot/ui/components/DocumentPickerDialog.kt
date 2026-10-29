package com.nexauren.docpilot.ui.components

import androidx.activity.compose.BackHandler
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.localization.LText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class DocumentPickerType {
    PDF,
    IMAGE,
}

/**
 * In-app document picker. It presents indexed device documents and recently used files inside
 * DocPilot; selection never launches a third-party file-browser activity.
 */
@Composable
fun DocumentPickerDialog(
    title: String,
    type: DocumentPickerType,
    multiple: Boolean,
    knownDocuments: List<DocumentItem>,
    hasStorageAccess: Boolean,
    onRequestStorageAccess: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (List<DocumentItem>) -> Unit,
) {
    val rootDirectory = remember(type) { Environment.getExternalStorageDirectory() }
    var currentDirectory by remember(type) { mutableStateOf(rootDirectory) }
    var query by remember(type) { mutableStateOf("") }
    var files by remember(type) { mutableStateOf(knownDocuments.filter { accepts(type, it) }) }
    var directories by remember(type) { mutableStateOf<List<File>>(emptyList()) }
    var selectedUris by remember(type, multiple) { mutableStateOf<Set<String>>(emptySet()) }
    var scanning by remember(type) { mutableStateOf(false) }
    var refreshToken by remember(type) { mutableStateOf(0) }
    val atRoot = currentDirectory.absolutePath == rootDirectory.absolutePath

    BackHandler(enabled = hasStorageAccess && !atRoot) {
        currentDirectory = currentDirectory.parentFile ?: rootDirectory
        query = ""
        selectedUris = emptySet()
    }

    LaunchedEffect(type, knownDocuments, hasStorageAccess, refreshToken, currentDirectory) {
        files = if (hasStorageAccess) emptyList() else knownDocuments.filter { accepts(type, it) }
        directories = emptyList()
        scanning = true
        val contents = withContext(Dispatchers.IO) {
            if (!hasStorageAccess) {
                emptyList<File>() to emptyList<DocumentItem>()
            } else {
                val children = runCatching {
                    currentDirectory.listFiles()
                        ?.filter { it.canRead() && !it.isHidden }
                        .orEmpty()
                }.getOrDefault(emptyList())
                val folders = children
                    .filter { it.isDirectory && it.canRead() }
                    .sortedBy { it.name.lowercase() }
                val documents = children
                    .filter { it.isFile && acceptsFileName(type, it.name) }
                    .sortedWith(compareByDescending<File> { it.lastModified() }.thenBy { it.name.lowercase() })
                    .map { file -> DocumentItem(file.name, Uri.fromFile(file).toString()) }
                folders to documents
            }
        }
        directories = contents.first
        val visibleDocuments = if (hasStorageAccess) {
            contents.second
        } else {
            knownDocuments.filter { accepts(type, it) }
        }
        files = visibleDocuments.distinctBy { it.uri }
        scanning = false
        selectedUris = selectedUris.intersect(files.map { it.uri }.toSet())
    }

    val filteredDirectories = remember(directories, query) {
        if (query.isBlank()) directories
        else directories.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    val filteredFiles = remember(files, query) {
        if (query.isBlank()) files
        else files.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    fun toggle(document: DocumentItem) {
        selectedUris = if (document.uri in selectedUris) {
            selectedUris - document.uri
        } else if (multiple) {
            selectedUris + document.uri
        } else {
            setOf(document.uri)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 560.dp)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (type == DocumentPickerType.PDF) Icons.Outlined.PictureAsPdf else Icons.Outlined.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        LText(title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                        LText(
                            if (multiple) "Seleciona um ou mais ficheiros" else "Seleciona um ficheiro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Fechar")
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Limpar pesquisa")
                            }
                        }
                    },
                    placeholder = { LText("Pesquisar ficheiros") },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    IconButton(
                        enabled = hasStorageAccess && !atRoot,
                        onClick = {
                            currentDirectory = currentDirectory.parentFile ?: rootDirectory
                            query = ""
                            selectedUris = emptySet()
                        },
                    ) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Pasta anterior")
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        LText(
                            if (!hasStorageAccess) "Ficheiros recentes" else if (atRoot) "Armazenamento interno" else currentDirectory.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                        )
                        LText(
                            if (!hasStorageAccess) "Documentos já usados nesta aplicação"
                            else "Navega pelas pastas sem sair do DocPilot",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }

                if (!hasStorageAccess) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Outlined.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                LText("Ver todos os ficheiros", style = MaterialTheme.typography.titleSmall)
                                LText(
                                    "Os ficheiros já usados continuam disponíveis. Ativa o acesso para indexar PDFs e imagens do dispositivo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                            TextButton(onClick = onRequestStorageAccess) {
                                LText("Ativar")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LText(
                        when {
                            scanning -> "A procurar ficheiros…"
                            else -> "${filteredDirectories.size} pastas · ${filteredFiles.size} ficheiros"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        enabled = hasStorageAccess && !scanning,
                        onClick = {
                            // Re-trigger the indexed device query without leaving the in-app picker.
                            refreshToken += 1
                        },
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Atualizar")
                    }
                }

                if (scanning && files.isEmpty() && directories.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredFiles.isEmpty() && filteredDirectories.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().weight(1f).padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(42.dp),
                            )
                            LText(
                                if (query.isNotBlank()) "Não encontrámos ficheiros com esse nome"
                                else if (!hasStorageAccess) "A tua lista de ficheiros está vazia"
                                else "Esta pasta não contém ficheiros deste tipo",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            LText(
                                if (query.isBlank()) "Importa um PDF para a biblioteca ou ativa o acesso ao dispositivo para mostrar os ficheiros existentes."
                                else "Experimenta outro termo de pesquisa.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (query.isNotBlank()) {
                                TextButton(onClick = { query = "" }) { LText("Limpar pesquisa") }
                            } else if (!hasStorageAccess) {
                                OutlinedButton(onClick = onRequestStorageAccess) { LText("Ativar acesso ao dispositivo") }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredDirectories, key = { "folder:${it.absolutePath}" }) { folder ->
                            Card(
                                onClick = {
                                    currentDirectory = folder
                                    query = ""
                                    selectedUris = emptySet()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        modifier = Modifier.size(42.dp),
                                        shape = RoundedCornerShape(13.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Outlined.FolderOpen,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        LText(folder.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                        LText("Pasta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Outlined.ArrowForward, contentDescription = "Abrir pasta", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        items(filteredFiles, key = { it.uri }) { document ->
                            val isSelected = document.uri in selectedUris
                            Card(
                                onClick = { toggle(document) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
                                    else MaterialTheme.colorScheme.surface,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Surface(
                                        modifier = Modifier.size(42.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                if (type == DocumentPickerType.PDF) Icons.Outlined.PictureAsPdf else Icons.Outlined.Image,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        LText(
                                            document.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 2,
                                        )
                                        LText(
                                            if (type == DocumentPickerType.PDF) "Documento PDF" else "Imagem",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                    if (multiple) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { toggle(document) },
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = "Selecionado",
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        LText("Cancelar")
                    }
                    Button(
                        enabled = selectedUris.isNotEmpty() && (!scanning || files.isNotEmpty()),
                        onClick = {
                            val selected = files.filter { it.uri in selectedUris }
                            if (selected.isNotEmpty()) onConfirm(selected)
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        LText(if (multiple) "Usar seleção" else "Abrir PDF")
                    }
                }
            }
        }
    }

}

private fun acceptsFileName(type: DocumentPickerType, fileName: String): Boolean =
    when (type) {
        DocumentPickerType.PDF -> fileName.endsWith(".pdf", ignoreCase = true)
        DocumentPickerType.IMAGE -> listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif", ".heic", ".heif", ".tif", ".tiff")
            .any { extension -> fileName.endsWith(extension, ignoreCase = true) }
    }

private fun accepts(type: DocumentPickerType, document: DocumentItem): Boolean =
    when (type) {
        DocumentPickerType.PDF -> document.name.endsWith(".pdf", ignoreCase = true) ||
            document.uri.substringBefore('?').endsWith(".pdf", ignoreCase = true)
        DocumentPickerType.IMAGE -> listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif", ".heic", ".heif", ".tif", ".tiff")
            .any { extension -> document.name.endsWith(extension, ignoreCase = true) }
    }
