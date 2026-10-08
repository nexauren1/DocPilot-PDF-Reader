package com.nexauren.docpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.EditDocument
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

private data class Tool(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

private val tools = listOf(
    Tool("Juntar PDFs", "Combinar documentos", Icons.Outlined.Description),
    Tool("Dividir PDF", "Separar páginas", Icons.Outlined.CallSplit),
    Tool("Comprimir", "Reduzir tamanho", Icons.Outlined.Archive),
    Tool("Proteger", "Senha e segurança", Icons.Outlined.Lock),
    Tool("Organizar", "Reordenar páginas", Icons.Outlined.Edit),
    Tool("Scanner", "Digitalizar documentos", Icons.Outlined.CameraAlt),
    Tool("Imagem → PDF", "Converter imagens", Icons.Outlined.PhotoLibrary),
    Tool("Assinar", "Assinatura digital", Icons.Outlined.Create),
    Tool("Extrair texto", "OCR e conteúdo", Icons.Outlined.Description),
    Tool("Duplicar páginas", "Copiar conteúdo", Icons.Outlined.ContentCopy),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onToolClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ferramentas") },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(tools) { tool ->
                Card(
                    modifier = Modifier.clickable { onToolClick(tool.title) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        Icon(
                            tool.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(tool.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            tool.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
