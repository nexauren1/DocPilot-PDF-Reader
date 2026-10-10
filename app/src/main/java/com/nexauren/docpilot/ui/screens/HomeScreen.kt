package com.nexauren.docpilot.ui.screens

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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

    if (showAccessDialog) {
        AlertDialog(
            onDismissRequest = { showAccessDialog = false },
            title = { Text("Aceder aos teus documentos") },
            text = {
                Text("O DocPilot pode procurar PDFs no armazenamento partilhado do dispositivo para criar uma biblioteca automática. Os ficheiros são lidos no teu dispositivo e não são enviados para um servidor. Podes continuar a abrir um PDF individualmente se preferires não conceder este acesso.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showAccessDialog = false
                    onRequestDocumentAccess()
                }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { showAccessDialog = false }) { Text("Agora não") }
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
                                    Brush.linearGradient(listOf(Color(0xFF2F63D8), Color(0xFF7250E8))),
                                    RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text("DocPilot", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Espaço de documentos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onViewLibrary) {
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
            item {
                Column {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Os teus documentos, organizados.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Lê, gere e transforma PDFs num espaço simples e privado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2F63D8), Color(0xFF7250E8))),
                            RoundedCornerShape(26.dp),
                        )
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text("O TEU ESPAÇO DE TRABALHO", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.88f))
                        }
                        Text("Tudo começa com um PDF.", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text(
                            "Abre um documento para ler ou usa as ferramentas para o preparar como precisas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.90f),
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (hasDocumentAccess) onRefreshDeviceDocuments()
                                    else showAccessDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2F55BE)),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (hasDocumentAccess) "Atualizar ficheiros" else "Ativar biblioteca")
                            }
                            OutlinedButton(
                                onClick = onViewLibrary,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f)),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Biblioteca")
                            }
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
                                        Brush.linearGradient(listOf(Color(0xFFE5EDFF), Color(0xFFEDE7FF))),
                                        RoundedCornerShape(14.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFF4B55D5),
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    "ABERTO RECENTEMENTE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    latestDocument.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                )
                                Text(
                                    "Toca para abrir o documento",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
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
                    Text("Acesso rápido", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text(
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
                            Text("Privacidade por defeito", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Os teus PDFs são trabalhados localmente. Não precisas de criar uma conta DocPilot.",
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
                        Text("Documentos recentes", style = MaterialTheme.typography.titleLarge)
                        Text("Continua de onde ficaste", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
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
                            Text("A tua biblioteca começa aqui", style = MaterialTheme.typography.titleMedium)
                            Text(
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
                                Text(if (hasDocumentAccess) "Procurar PDFs" else "Dar acesso aos PDFs")
                            }
                            OutlinedButton(onClick = onImportPdf) {
                                Text("Abrir um PDF individual")
                            }
                        }
                    }
                }
            } else {
                items(documents.take(4), key = { it.uri }) { document ->
                    DocumentCard(document = document) { onOpenDocument(document) }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
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
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
