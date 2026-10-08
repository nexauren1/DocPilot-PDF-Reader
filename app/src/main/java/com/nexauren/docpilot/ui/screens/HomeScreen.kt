package com.nexauren.docpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.components.DocumentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    documents: List<DocumentItem>,
    onImportPdf: () -> Unit,
    onOpenDocument: (DocumentItem) -> Unit,
    onViewLibrary: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DocPilot", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "PDF Reader & Document Tools",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Tudo para os seus documentos.",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Leia, organize e transforme PDFs num único lugar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(onClick = onImportPdf) {
                                Icon(Icons.Outlined.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Importar PDF")
                            }
                            OutlinedButton(onClick = onViewLibrary) {
                                Icon(Icons.Outlined.Folder, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Biblioteca")
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Acesso rápido",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "3 áreas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuickAction("Leitor", Icons.Outlined.PictureAsPdf, Modifier.weight(1f), onImportPdf)
                    QuickAction("Arquivos", Icons.Outlined.Folder, Modifier.weight(1f), onViewLibrary)
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text("Recentes", style = MaterialTheme.typography.titleMedium)
            }

            if (documents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Os seus PDFs recentes aparecem aqui depois da primeira importação.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            } else {
                items(documents.take(5), key = { it.uri }) { document ->
                    DocumentCard(document = document) {
                        onOpenDocument(document)
                    }
                }
            }

            item {
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
