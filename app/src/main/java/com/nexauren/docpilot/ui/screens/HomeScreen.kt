package com.nexauren.docpilot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.nexauren.docpilot.ui.localization.LText
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.components.DocumentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    documents: List<DocumentItem>,
    onImportPdf: () -> Unit,
    hasDocumentAccess: Boolean,
    onRequestDocumentAccess: () -> Unit,
    onRefreshDeviceDocuments: () -> Unit,
    onOpenDocument: (DocumentItem) -> Unit,
    onViewLibrary: () -> Unit,
    onViewTools: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    var showAccessDialog by remember { mutableStateOf(false) }
    var homeSearchVisible by remember { mutableStateOf(false) }
    var homeSearchQuery by remember { mutableStateOf("") }
    val homeSearchResults = remember(documents, homeSearchQuery) {
        documents.filter { it.name.contains(homeSearchQuery.trim(), ignoreCase = true) }
    }

    if (showAccessDialog) {
        AlertDialog(
            onDismissRequest = { showAccessDialog = false },
            title = { LText("Aceder aos teus documentos") },
            text = {
                LText("O DocPilot pode procurar PDFs no armazenamento partilhado do dispositivo para criar uma biblioteca automática. Os ficheiros são lidos no teu dispositivo e não são enviados para um servidor. Podes continuar a abrir um PDF individualmente se preferires não conceder este acesso.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showAccessDialog = false
                    onRequestDocumentAccess()
                }) { LText("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { showAccessDialog = false }) { LText("Agora não") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Abrir menu")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(Modifier.width(11.dp))
                        Column {
                            LText("DocPilot", style = MaterialTheme.typography.titleLarge)
                            LText(
                                "Leitor de PDFs e ferramentas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        homeSearchVisible = !homeSearchVisible
                        if (!homeSearchVisible) homeSearchQuery = ""
                    }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Pesquisar documentos")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (homeSearchVisible) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        OutlinedTextField(
                            value = homeSearchQuery,
                            onValueChange = { homeSearchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                            trailingIcon = {
                                if (homeSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { homeSearchQuery = "" }) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Limpar pesquisa")
                                    }
                                }
                            },
                            placeholder = { LText("Pesquisar os teus documentos") },
                        )
                    }
                }
            }

            if (homeSearchVisible && homeSearchQuery.isNotBlank()) {
                if (homeSearchResults.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                LText("Nenhum documento encontrado", style = MaterialTheme.typography.titleMedium)
                                LText("Tenta outro nome ou parte do nome do ficheiro.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    item {
                        LText(
                            "${homeSearchResults.size} resultado(s)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(homeSearchResults, key = { it.uri }) { document ->
                        DocumentCard(document = document) { onOpenDocument(document) }
                    }
                }
            } else {
                item {
                Column {
                    Spacer(Modifier.height(4.dp))
                    LText(
                        "Os teus documentos, organizados.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(5.dp))
                    LText(
                        "Lê, gere e transforma PDFs num espaço simples e privado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            LText(
                                "PDF WORKSPACE",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        LText(
                            "Os teus documentos. O teu controlo.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        LText(
                            "Abre, lê e transforma PDFs num único espaço. Os documentos são processados no dispositivo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = onImportPdf,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Spacer(Modifier.width(7.dp))
                                LText("Abrir PDF")
                            }
                            OutlinedButton(
                                onClick = onViewTools,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(7.dp))
                                LText("Ferramentas")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            LText("Biblioteca do dispositivo", style = MaterialTheme.typography.titleSmall)
                            LText(
                                if (hasDocumentAccess) "${documents.size} documentos"
                                else "Acede aos PDFs para os listar e pesquisar no DocPilot.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(
                            onClick = {
                                if (hasDocumentAccess) onRefreshDeviceDocuments()
                                else showAccessDialog = true
                            },
                        ) {
                            LText(if (hasDocumentAccess) "Atualizar" else "Configurar")
                        }
                    }
                }
            }

            if (documents.isNotEmpty()) {
                item {
                    val latestDocument = documents.first()
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenDocument(latestDocument) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 62.dp, height = 76.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(14.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                LText(
                                    "ABERTO RECENTEMENTE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                LText(
                                    latestDocument.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                )
                                LText(
                                    "Toca para abrir o documento",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                LText(
                                    "Abrir documento  →",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    LText("Acesso rápido", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    LText(
                        "${documents.size} documentos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard("Leitor PDF", "Abre um documento", Icons.Outlined.PictureAsPdf, Color(0xFFE5EDFF), Color(0xFF2F63D8), Modifier.weight(1f), onImportPdf)
                    QuickActionCard("Biblioteca", "Encontra os ficheiros", Icons.Outlined.Folder, Color(0xFFEDE7FF), Color(0xFF7250E8), Modifier.weight(1f), onViewLibrary)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard("Scanner", "Digitaliza documentos", Icons.Outlined.CameraAlt, Color(0xFFE1F5EF), Color(0xFF16856B), Modifier.weight(1f), onViewTools)
                    QuickActionCard("Ferramentas", "PDF e conversão", Icons.Outlined.AutoAwesome, Color(0xFFFFF0DA), Color(0xFFB66B08), Modifier.weight(1f), onViewTools)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            LText("Privacidade por defeito", style = MaterialTheme.typography.titleSmall)
                            LText(
                                "Os teus PDFs são trabalhados localmente. Não precisas de criar uma conta.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        LText("Documentos recentes", style = MaterialTheme.typography.titleLarge)
                        LText("Continua de onde ficaste", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LText(
                        "Ver todos",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onViewLibrary).padding(8.dp),
                    )
                }
            }

            if (documents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(54.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp))
                            }
                            LText("A tua biblioteca começa aqui", style = MaterialTheme.typography.titleMedium)
                            LText(
                                if (hasDocumentAccess) "Ainda não encontrámos PDFs. Podes voltar a procurar ou abrir um ficheiro individualmente." else "Permite ao DocPilot procurar PDFs no dispositivo ou abre um ficheiro individualmente.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(onClick = {
                                if (hasDocumentAccess) onRefreshDeviceDocuments()
                                else showAccessDialog = true
                            }) {
                                Icon(Icons.Outlined.Folder, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                LText(if (hasDocumentAccess) "Procurar PDFs" else "Dar acesso aos PDFs")
                            }
                            OutlinedButton(onClick = onImportPdf) {
                                LText("Abrir um PDF individual")
                            }
                        }
                    }
                }
            } else {
                items(documents.take(8), key = { it.uri }) { document ->
                    DocumentCard(document = document) { onOpenDocument(document) }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(iconBackground, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(23.dp))
            }
            LText(title, style = MaterialTheme.typography.titleSmall)
            LText(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
