package com.nexauren.docpilot.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.components.DocumentCard

private val BrandBlue = Color(0xFF315DEB)
private val BrandViolet = Color(0xFF7652E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    documents: List<DocumentItem>,
    folderConnected: Boolean,
    folderName: String?,
    scanning: Boolean,
    onChooseFolder: () -> Unit,
    onRefreshFolder: () -> Unit,
    onOpenSinglePdf: () -> Unit,
    onOpenDocument: (DocumentItem) -> Unit,
    onViewLibrary: () -> Unit,
    onViewTools: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Brush.linearGradient(listOf(BrandBlue, BrandViolet)), RoundedCornerShape(15.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.PictureAsPdf, null, tint = Color.White, modifier = Modifier.size(25.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text("DocPilot", style = MaterialTheme.typography.titleLarge)
                            Text("PDFs. Sem complicações.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("O teu espaço de documentos.", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Lê, organiza e prepara PDFs com ferramentas feitas para o dia a dia.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().background(
                        Brush.linearGradient(listOf(Color(0xFF2858E6), Color(0xFF594DE1), Color(0xFF8050DD))),
                        RoundedCornerShape(28.dp),
                    ).padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(30.dp).background(Color.White.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                            Text("A TUA BIBLIOTECA, NUM SÓ LUGAR", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.88f))
                        }
                        Text("Encontra os teus PDFs automaticamente.", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text(
                            "Escolhe uma pasta e o DocPilot procura documentos nela e nas subpastas. Os ficheiros ficam no teu dispositivo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.91f),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onChooseFolder,
                                enabled = !scanning,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2E4FC6)),
                                shape = RoundedCornerShape(15.dp),
                            ) {
                                Icon(Icons.Outlined.FolderOpen, null)
                                Spacer(Modifier.width(7.dp))
                                Text(if (folderConnected) "Gerir pasta" else "Escolher pasta")
                            }
                            OutlinedButton(
                                onClick = onOpenSinglePdf,
                                enabled = !scanning,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.52f)),
                                shape = RoundedCornerShape(15.dp),
                            ) {
                                Icon(Icons.Outlined.Add, null)
                                Spacer(Modifier.width(5.dp))
                                Text("Abrir PDF")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(if (folderConnected) "Pasta ligada" else "Acesso à tua biblioteca", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (folderConnected) folderName ?: "Pasta selecionada" else "Escolhe uma pasta para encontrar os documentos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = onRefreshFolder, enabled = !scanning) {
                                if (scanning) CircularProgressIndicator(modifier = Modifier.size(19.dp), strokeWidth = 2.dp)
                                else Icon(Icons.Outlined.Refresh, contentDescription = "Atualizar biblioteca")
                            }
                        }
                        if (scanning) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("A procurar PDFs nas pastas…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(
                                if (folderConnected) "Atualiza para encontrar documentos novos."
                                else "O DocPilot só lê a pasta que autorizares. Podes alterar esta escolha nas definições.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${documents.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                Text("PDFs na biblioteca", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Local e privado", style = MaterialTheme.typography.titleSmall)
                                Text("Sem conta obrigatória", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Acesso rápido", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("Ver ferramentas", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onViewTools).padding(7.dp))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    QuickActionCard("Ler PDF", "Abrir um documento", Icons.Outlined.PictureAsPdf, BrandBlue, Modifier.weight(1f), onOpenSinglePdf)
                    QuickActionCard("Scanner", "Digitalizar papel", Icons.Outlined.CameraAlt, Color(0xFF14866D), Modifier.weight(1f), onViewTools)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    QuickActionCard("Ferramentas", "PDF e conversão", Icons.Outlined.Tune, BrandViolet, Modifier.weight(1f), onViewTools)
                    QuickActionCard("Biblioteca", "Pesquisar ficheiros", Icons.Outlined.Folder, Color(0xFFB56B12), Modifier.weight(1f), onViewLibrary)
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Recentes", style = MaterialTheme.typography.titleLarge)
                        Text("Continua de onde ficaste", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Ver biblioteca", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onViewLibrary).padding(7.dp))
                }
            }

            if (documents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(58.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(29.dp))
                            }
                            Text("A tua biblioteca começa aqui", style = MaterialTheme.typography.titleMedium)
                            Text("Liga uma pasta ou abre um PDF para começar a ler.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = onChooseFolder, enabled = !scanning) { Text("Escolher uma pasta") }
                        }
                    }
                }
            } else {
                items(documents.take(5), key = { it.uri }) { document ->
                    DocumentCard(document = document) { onOpenDocument(document) }
                }
            }
            item { Spacer(Modifier.height(3.dp)) }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(Modifier.size(42.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(23.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
