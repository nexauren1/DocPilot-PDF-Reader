package com.nexauren.docpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.components.DocumentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    documents: List<DocumentItem>,
    folderConnected: Boolean,
    folderName: String?,
    scanning: Boolean,
    onChooseFolder: () -> Unit,
    onRefreshFolder: () -> Unit,
    onOpenSinglePdf: () -> Unit,
    onOpenDocument: (DocumentItem) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(documents, query) {
        documents.filter { it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("Biblioteca")
                        Text(
                            "${documents.size} PDFs guardados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshFolder, enabled = !scanning) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Atualizar pasta")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        Card(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            Icon(
                                Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(if (folderConnected) "Pasta conectada" else "Liga uma pasta", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (folderConnected) folderName ?: "Pasta selecionada" else "Descoberta local de PDFs e subpastas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (scanning) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("A ler a pasta…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Button(
                            onClick = onChooseFolder,
                            enabled = !scanning,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(13.dp),
                        ) {
                            Icon(Icons.Outlined.FolderOpen, null)
                            Spacer(Modifier.size(6.dp))
                            Text(if (folderConnected) "Alterar pasta" else "Escolher pasta")
                        }
                        OutlinedButton(
                            onClick = onOpenSinglePdf,
                            enabled = !scanning,
                            shape = RoundedCornerShape(13.dp),
                        ) {
                            Icon(Icons.Outlined.Add, null)
                            Spacer(Modifier.size(5.dp))
                            Text("Abrir PDF")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 13.dp, bottom = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Pesquisar por nome") },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (query.isBlank()) "Todos os documentos" else "Resultados da pesquisa",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${filtered.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(25.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(9.dp),
                            ) {
                                Icon(
                                    if (documents.isEmpty()) Icons.Outlined.Folder else Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(35.dp),
                                )
                                Text(
                                    when {
                                        documents.isEmpty() && folderConnected -> "Não foram encontrados PDFs"
                                        documents.isEmpty() -> "A tua biblioteca está vazia"
                                        else -> "Nenhum resultado"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    when {
                                        documents.isEmpty() && folderConnected -> "Atualiza a pasta ou abre um PDF individual."
                                        documents.isEmpty() -> "Escolhe uma pasta para encontrar PDFs automaticamente, ou abre um ficheiro."
                                        else -> "Experimenta procurar com outro nome."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (documents.isEmpty()) {
                                    Button(onClick = if (folderConnected) onRefreshFolder else onChooseFolder, enabled = !scanning) {
                                        Text(if (folderConnected) "Atualizar pasta" else "Escolher pasta")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.uri }) { document ->
                        DocumentCard(document) { onOpenDocument(document) }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
