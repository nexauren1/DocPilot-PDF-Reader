package com.nexauren.docpilot.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CallSplit
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.pdf.PdfProcessor
import com.nexauren.docpilot.pdf.formatBytes
import com.nexauren.docpilot.pdf.parsePageOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class PdfTool(val title: String, val subtitle: String, val icon: ImageVector) {
    MERGE("Juntar PDFs", "Combinar documentos", Icons.Outlined.Description),
    SPLIT("Dividir PDF", "Extrair páginas", Icons.Outlined.CallSplit),
    COMPRESS("Comprimir", "Reduzir tamanho", Icons.Outlined.Archive),
    REORDER("Organizar", "Reordenar páginas", Icons.Outlined.Edit),
}

private data class ToolCard(val title: String, val subtitle: String, val icon: ImageVector, val tool: PdfTool?)

private val toolCards = listOf(
    ToolCard("Juntar PDFs", "Combinar documentos", Icons.Outlined.Description, PdfTool.MERGE),
    ToolCard("Dividir PDF", "Extrair páginas", Icons.Outlined.CallSplit, PdfTool.SPLIT),
    ToolCard("Comprimir", "Reduzir tamanho", Icons.Outlined.Archive, PdfTool.COMPRESS),
    ToolCard("Organizar", "Reordenar páginas", Icons.Outlined.Edit, PdfTool.REORDER),
    ToolCard("Scanner", "Digitalizar documentos", Icons.Outlined.CameraAlt, null),
    ToolCard("Imagem → PDF", "Converter imagens", Icons.Outlined.PhotoLibrary, null),
    ToolCard("Assinar", "Assinatura digital", Icons.Outlined.Create, null),
    ToolCard("Proteger", "Senha e segurança", Icons.Outlined.Lock, null),
    ToolCard("Extrair texto", "OCR e conteúdo", Icons.Outlined.Description, null),
    ToolCard("Duplicar páginas", "Copiar conteúdo", Icons.Outlined.ContentCopy, null),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen() {
    var selectedTool by remember { mutableStateOf<PdfTool?>(null) }

    if (selectedTool != null) {
        ToolWorkspace(tool = selectedTool!!, onBack = { selectedTool = null })
        return
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Ferramentas") }) }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 4.dp, 16.dp, padding.calculateBottomPadding() + 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(toolCards) { card ->
                Card(
                    onClick = { card.tool?.let { selectedTool = it } },
                    enabled = card.tool != null,
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(card.icon, contentDescription = null, tint = if (card.tool != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(card.title, style = MaterialTheme.typography.titleSmall)
                        Text(card.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (card.tool == null) Text("Em breve", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolWorkspace(tool: PdfTool, onBack: () -> Unit) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var startPage by remember { mutableStateOf("1") }
    var endPage by remember { mutableStateOf("1") }
    var orderText by remember { mutableStateOf("1,2,3") }
    var processing by remember { mutableStateOf(false) }
    var pendingSave by remember { mutableStateOf<((Uri) -> Long)?>(null) }

    val multiPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris -> selectedUris = uris }
    val singlePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) selectedUris = listOf(uri) }
    val outputPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val action = pendingSave
        pendingSave = null
        if (uri != null && action != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching { action(uri) } }
                processing = false
                snackbar.showSnackbar(result.fold({ "PDF criado • " + formatBytes(it) }, { "Erro: " + (it.message ?: "processamento falhou") }))
            }
        }
    }

    fun createOutput(name: String, action: (Uri) -> Long) {
        pendingSave = action
        outputPicker.launch(name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool.title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = when (tool) {
                    PdfTool.MERGE -> "Selecione dois ou mais PDFs para criar um único documento.",
                    PdfTool.SPLIT -> "Selecione um PDF e indique o intervalo de páginas.",
                    PdfTool.COMPRESS -> "Imagens grandes serão reduzidas e recomprimidas localmente.",
                    PdfTool.REORDER -> "Indique a nova ordem, por exemplo: 3,1,2,4.",
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            if (tool == PdfTool.MERGE) {
                OutlinedButton(onClick = { multiPicker.launch(arrayOf("application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Folder, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Selecionar PDFs")
                }
            } else {
                OutlinedButton(onClick = { singlePicker.launch(arrayOf("application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Folder, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Selecionar PDF")
                }
            }

            if (selectedUris.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Ficheiros selecionados", style = MaterialTheme.typography.titleSmall)
                        selectedUris.forEachIndexed { index, uri -> Text((index + 1).toString() + ". " + (uri.lastPathSegment ?: "PDF"), maxLines = 1) }
                    }
                }
            }

            when (tool) {
                PdfTool.SPLIT -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(startPage, { startPage = it.filter(Char::isDigit) }, label = { Text("De") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(endPage, { endPage = it.filter(Char::isDigit) }, label = { Text("Até") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                PdfTool.REORDER -> OutlinedTextField(orderText, { orderText = it }, label = { Text("Nova ordem") }, supportingText = { Text("Ex.: 3,1,2,4") }, modifier = Modifier.fillMaxWidth())
                else -> Unit
            }

            Button(
                onClick = {
                    when (tool) {
                        PdfTool.MERGE -> if (selectedUris.size < 2) scope.launch { snackbar.showSnackbar("Selecione pelo menos dois PDFs.") } else createOutput("docpilot-merged.pdf") { uri -> PdfProcessor.merge(resolver, selectedUris, uri) }
                        PdfTool.SPLIT -> {
                            val start = startPage.toIntOrNull()
                            val end = endPage.toIntOrNull()
                            if (selectedUris.size != 1 || start == null || end == null) scope.launch { snackbar.showSnackbar("Selecione um PDF e um intervalo válido.") }
                            else createOutput("docpilot-split.pdf") { uri -> PdfProcessor.extractPages(resolver, selectedUris.single(), uri, start, end) }
                        }
                        PdfTool.COMPRESS -> if (selectedUris.size != 1) scope.launch { snackbar.showSnackbar("Selecione um PDF.") } else createOutput("docpilot-compressed.pdf") { uri -> PdfProcessor.compress(resolver, selectedUris.single(), uri) }
                        PdfTool.REORDER -> {
                            val orderResult = runCatching { parsePageOrder(orderText) }
                            if (orderResult.isFailure) scope.launch { snackbar.showSnackbar(orderResult.exceptionOrNull()?.message ?: "Ordem inválida.") }
                            else if (selectedUris.size != 1) scope.launch { snackbar.showSnackbar("Selecione um PDF.") }
                            else createOutput("docpilot-reordered.pdf") { uri -> PdfProcessor.reorder(resolver, selectedUris.single(), uri, orderResult.getOrThrow()) }
                        }
                    }
                },
                enabled = !processing,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (processing) { CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp).width(20.dp)); Spacer(Modifier.width(10.dp)); Text("A processar…") }
                else Text("Criar PDF")
            }

            Text("O processamento é local no dispositivo. Os PDFs não são enviados para um servidor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
        }
    }
}