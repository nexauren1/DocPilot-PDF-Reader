package com.nexauren.docpilot.ui.screens

import android.app.Activity
import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.provider.OpenableColumns
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import com.google.mlkit.vision.common.InputImage
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nexauren.docpilot.pdf.DocumentTools
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

private enum class SecurityTool(val title: String, val subtitle: String, val icon: ImageVector) {
    PROTECT("Proteger", "Senha e permissões", Icons.Outlined.Lock),
    SIGN("Assinatura visual", "Adicionar assinatura ao PDF", Icons.Outlined.Create),
}

private data class ToolCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val pdfTool: PdfTool? = null,
    val securityTool: SecurityTool? = null,
    val action: Action = Action.NONE,
)

private enum class Action { NONE, SCAN, IMAGE_TO_PDF, OCR, PDF_INFO, EXTRACT_TEXT }

private val toolCards = listOf(
    ToolCard("Juntar PDFs", "Combinar documentos", Icons.Outlined.Description, pdfTool = PdfTool.MERGE),
    ToolCard("Dividir PDF", "Extrair páginas", Icons.Outlined.CallSplit, pdfTool = PdfTool.SPLIT),
    ToolCard("Comprimir", "Reduzir tamanho", Icons.Outlined.Archive, pdfTool = PdfTool.COMPRESS),
    ToolCard("Organizar", "Reordenar páginas", Icons.Outlined.Edit, pdfTool = PdfTool.REORDER),
    ToolCard("Scanner", "Digitalizar documentos", Icons.Outlined.CameraAlt, action = Action.SCAN),
    ToolCard("Imagem → PDF", "Converter imagens", Icons.Outlined.PhotoLibrary, action = Action.IMAGE_TO_PDF),
    ToolCard("Assinatura visual", "Adicionar assinatura", Icons.Outlined.Create, securityTool = SecurityTool.SIGN),
    ToolCard("Proteger", "Senha e permissões", Icons.Outlined.Lock, securityTool = SecurityTool.PROTECT),
    ToolCard("OCR de imagem", "Reconhecer texto numa imagem", Icons.Outlined.Description, action = Action.OCR),
    ToolCard("Extrair texto PDF", "Guardar texto num ficheiro .txt", Icons.Outlined.Description, action = Action.EXTRACT_TEXT),
    ToolCard("Informações do PDF", "Páginas, nome e tamanho", Icons.Outlined.Info, action = Action.PDF_INFO),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onOutputSaved: (String) -> Unit, onOpenMenu: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var selectedPdfTool by remember { mutableStateOf<PdfTool?>(null) }
    var selectedSecurityTool by remember { mutableStateOf<SecurityTool?>(null) }
    var ocrResult by remember { mutableStateOf<String?>(null) }
    var pdfInfoResult by remember { mutableStateOf<String?>(null) }
    var pendingPdfText by remember { mutableStateOf<String?>(null) }
    var processing by remember { mutableStateOf(false) }

    var toolQuery by remember { mutableStateOf("") }
    var toolCategory by remember { mutableStateOf("Todas") }
    val visibleTools = remember(toolQuery, toolCategory) {
        toolCards.filter { card ->
            val queryMatches = card.title.contains(toolQuery, ignoreCase = true) ||
                card.subtitle.contains(toolQuery, ignoreCase = true)
            val categoryMatches = when (toolCategory) {
                "PDF" -> card.pdfTool != null || card.action == Action.PDF_INFO || card.action == Action.EXTRACT_TEXT
                "Segurança" -> card.securityTool != null
                "Digitalização" -> card.action == Action.SCAN || card.action == Action.OCR
                "Conversão" -> card.action == Action.IMAGE_TO_PDF
                else -> true
            }
            queryMatches && categoryMatches
        }
    }

    var pendingScannerUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingOutputAction by remember { mutableStateOf<((Uri) -> Long)?>(null) }

    val outputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        val action = pendingOutputAction
        pendingOutputAction = null
        if (uri != null && action != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching { action(uri) } }
                processing = false
                // Add the finished PDF to the app library only after processing succeeds.
                result.onSuccess { onOutputSaved(uri.toString()) }
                snackbar.showSnackbar(
                    result.fold(
                        { "PDF criado • " + formatBytes(it) },
                        { "Erro: " + (it.message ?: "processamento falhou") },
                    ),
                )
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        pendingImageUris = uris
        if (uris.isNotEmpty()) {
            pendingOutputAction = { output -> DocumentTools.imagesToPdf(context.contentResolver, uris, output) }
            outputPicker.launch("docpilot-images.pdf")
        }
    }

    val ocrPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            processing = true
            runCatching {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        ocrResult = result.text.ifBlank { "Nenhum texto foi identificado nesta imagem." }
                    }
                    .addOnFailureListener { error ->
                        scope.launch { snackbar.showSnackbar("OCR falhou: " + (error.message ?: "erro desconhecido")) }
                    }
                    .addOnCompleteListener {
                        recognizer.close()
                        processing = false
                    }
            }.onFailure {
                processing = false
                scope.launch { snackbar.showSnackbar("Não foi possível abrir a imagem.") }
            }
        }
    }

    val textOutputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { outputUri ->
        val text = pendingPdfText
        pendingPdfText = null
        if (outputUri != null && text != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(outputUri)?.use { output ->
                            output.write(text.toByteArray(Charsets.UTF_8))
                        } ?: error("Não foi possível guardar o ficheiro de texto.")
                        text.length
                    }
                }
                processing = false
                snackbar.showSnackbar(
                    result.fold(
                        { "Texto exportado • $it caracteres." },
                        { "Não foi possível guardar o texto: " + (it.message ?: "erro desconhecido") },
                    ),
                )
            }
        }
    }

    val textPdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { sourceUri ->
        if (sourceUri != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching { extractPdfTextFromDocument(context, sourceUri) }
                }
                processing = false
                result.onSuccess { extracted ->
                    if (extracted.isBlank()) {
                        snackbar.showSnackbar("Este PDF não tem texto extraível. Se for digitalizado, usa OCR.")
                    } else {
                        pendingPdfText = extracted
                        textOutputPicker.launch("docpilot-extracted-text.txt")
                    }
                }.onFailure {
                    snackbar.showSnackbar("Não foi possível extrair o texto deste PDF.")
                }
            }
        }
    }

    val infoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching { inspectPdf(context, uri) } }
                processing = false
                result.onSuccess { pdfInfoResult = it }
                    .onFailure { snackbar.showSnackbar("Não foi possível ler as informações deste PDF.") }
            }
        }
    }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(30)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF,
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }
    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pdfUri = scanResult?.getPdf()?.getUri()
            if (pdfUri != null) {
                pendingScannerUri = pdfUri
                pendingOutputAction = { output -> DocumentTools.scanResultPdf(context.contentResolver, pdfUri, output) }
                outputPicker.launch("docpilot-scan.pdf")
            }
            scanResult?.getPages()?.firstOrNull()?.let { page ->
                val imageUri = page.getImageUri()
                if (imageUri != null) {
                    processing = true
                    runCatching {
                        val image = InputImage.fromFilePath(context, imageUri)
                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        recognizer.process(image)
                            .addOnSuccessListener { text ->
                                if (text.text.isNotBlank()) {
                                    ocrResult = text.text
                                }
                            }
                            .addOnCompleteListener {
                                recognizer.close()
                                processing = false
                            }
                    }.onFailure {
                        processing = false
                    }
                }
            }
        }
    }

    fun launchScanner() {
        val currentActivity = activity ?: run {
            scope.launch { snackbar.showSnackbar("Scanner indisponível neste contexto.") }
            return
        }
        scanner.getStartScanIntent(currentActivity)
            .addOnSuccessListener { sender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(sender).build())
            }
            .addOnFailureListener { error ->
                scope.launch { snackbar.showSnackbar("Não foi possível iniciar o scanner: " + (error.message ?: "erro")) }
            }
    }

    if (selectedPdfTool != null) {
        ToolWorkspace(tool = selectedPdfTool!!, onBack = { selectedPdfTool = null }, onOutputSaved = onOutputSaved)
        return
    }

    if (selectedSecurityTool != null) {
        SecurityWorkspace(
            tool = selectedSecurityTool!!,
            onBack = { selectedSecurityTool = null },
            onOutputSaved = onOutputSaved,
        )
        return
    }

    if (pdfInfoResult != null) {
        AlertDialog(
            onDismissRequest = { pdfInfoResult = null },
            title = { Text("Informações do PDF") },
            text = { Text(pdfInfoResult.orEmpty(), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(onClick = { pdfInfoResult = null }) { Text("Concluído") }
            },
        )
    }

    if (ocrResult != null) {
        AlertDialog(
            onDismissRequest = { ocrResult = null },
            title = { Text("Texto reconhecido") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(ocrResult.orEmpty())
                }
            },
            confirmButton = {
                Button(onClick = { ocrResult = null }) {
                    Text("Fechar")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ferramentas") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Abrir menu")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    "Ferramentas para cada tarefa",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    "Escolhe uma ação e acompanha cada etapa até guardar o resultado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                OutlinedTextField(
                    value = toolQuery,
                    onValueChange = { toolQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar ferramentas") },
                    placeholder = { Text("Pesquisar ferramentas") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Todas", "PDF", "Segurança", "Digitalização", "Conversão").forEach { category ->
                        FilterChip(
                            selected = toolCategory == category,
                            onClick = { toolCategory = category },
                            label = { Text(category) },
                        )
                    }
                }
                Text(
                    "${visibleTools.size} ferramentas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                if (visibleTools.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Nenhuma ferramenta encontrada", style = MaterialTheme.typography.titleMedium)
                                Text("Altera a pesquisa ou escolhe outra categoria.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    itemsIndexed(visibleTools, key = { _, card -> card.title }) { index, card ->
                        val enabled = card.pdfTool != null || card.securityTool != null || card.action != Action.NONE
                        val accent = accentFor(card)
                        val delay = (index * 28).coerceAtMost(196)
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(durationMillis = 220, delayMillis = delay)) +
                                scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = 220, delayMillis = delay)),
                            exit = fadeOut(tween(120)),
                        ) {
                            Card(
                                onClick = {
                                    when {
                                        card.pdfTool != null -> selectedPdfTool = card.pdfTool
                                        card.securityTool != null -> selectedSecurityTool = card.securityTool
                                        card.action == Action.SCAN -> launchScanner()
                                        card.action == Action.IMAGE_TO_PDF -> imagePicker.launch(arrayOf("image/*"))
                                        card.action == Action.OCR -> ocrPicker.launch(arrayOf("image/*"))
                                        card.action == Action.PDF_INFO -> infoPicker.launch(arrayOf("application/pdf"))
                                        card.action == Action.EXTRACT_TEXT -> textPdfPicker.launch(arrayOf("application/pdf"))
                                    }
                                },
                                enabled = enabled,
                                shape = RoundedCornerShape(22.dp),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
                                colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.075f)),
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(15.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Box(
                                            modifier = Modifier.size(48.dp).background(
                                                Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.72f))),
                                                RoundedCornerShape(16.dp),
                                            ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(card.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                        Text("↗", color = accent, style = MaterialTheme.typography.titleLarge)
                                    }
                                    Text(card.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        card.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        minLines = 2,
                                    )
                                    Text(
                                        categoryFor(card).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (processing) {
        ProcessingOverlay("A trabalhar no ficheiro", "O DocPilot está a processar a tarefa no teu dispositivo.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecurityWorkspace(
    tool: SecurityTool,
    onBack: () -> Unit,
    onOutputSaved: (String) -> Unit,
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var inputUri by remember { mutableStateOf<Uri?>(null) }
    var value by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<((Uri) -> Long)?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        inputUri = uri
    }
    val output = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val action = pendingAction
        pendingAction = null
        if (uri != null && action != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching { action(uri) } }
                processing = false
                result.onSuccess { onOutputSaved(uri.toString()) }
                snackbar.showSnackbar(
                    result.fold(
                        { "PDF criado • " + formatBytes(it) },
                        { "Erro: " + (it.message ?: "processamento falhou") },
                    ),
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, accentFor(tool).copy(alpha = 0.24f)),
                colors = CardDefaults.cardColors(containerColor = accentFor(tool).copy(alpha = 0.08f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier.size(58.dp).background(
                            Brush.linearGradient(listOf(accentFor(tool), accentFor(tool).copy(alpha = 0.72f))),
                            RoundedCornerShape(18.dp),
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (tool == SecurityTool.PROTECT) Icons.Outlined.Lock else Icons.Outlined.Create,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tool.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (tool == SecurityTool.PROTECT) "Proteção aplicada a uma cópia do documento." else "Texto visível no documento, sem certificado digital.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = if (tool == SecurityTool.PROTECT) {
                    "Crie uma cópia do PDF protegida por palavra-passe."
                } else {
                    "Adicione uma assinatura visual ao rodapé de cada página. Isto não é uma assinatura digital com certificado."
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedButton(
                onClick = { picker.launch(arrayOf("application/pdf")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Folder, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Selecionar PDF")
            }

            inputUri?.let {
                Text(
                    "PDF selecionado: " + (it.lastPathSegment ?: "documento.pdf"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(if (tool == SecurityTool.PROTECT) "Palavra-passe" else "Texto da assinatura")
                },
            )

            Button(
                enabled = inputUri != null && value.isNotBlank() && !processing,
                onClick = {
                    val source = inputUri ?: return@Button
                    if (tool == SecurityTool.PROTECT) {
                        pendingAction = { outputUri ->
                            DocumentTools.protectPdf(resolver, source, outputUri, value)
                        }
                        output.launch("docpilot-protected.pdf")
                    } else {
                        pendingAction = { outputUri ->
                            DocumentTools.addVisualSignature(resolver, source, outputUri, value)
                        }
                        output.launch("docpilot-signed.pdf")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (processing) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("A processar…")
                } else {
                    Text(if (tool == SecurityTool.PROTECT) "Proteger PDF" else "Aplicar assinatura")
                }
            }

            Text(
                "O processamento acontece localmente no dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (processing) {
        ProcessingOverlay(
            if (tool == SecurityTool.PROTECT) "A proteger o documento" else "A aplicar assinatura",
            "A criar uma nova cópia do PDF no teu dispositivo.",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolWorkspace(
    tool: PdfTool,
    onBack: () -> Unit,
    onOutputSaved: (String) -> Unit,
) {
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

    val multiPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedUris = uris
    }

    val singlePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) selectedUris = listOf(uri)
    }

    val outputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val action = pendingSave
        pendingSave = null
        if (uri != null && action != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) { runCatching { action(uri) } }
                processing = false
                result.onSuccess { onOutputSaved(uri.toString()) }
                snackbar.showSnackbar(
                    result.fold(
                        { "PDF criado • " + formatBytes(it) },
                        { "Erro: " + (it.message ?: "processamento falhou") },
                    )
                )
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, accentFor(tool).copy(alpha = 0.24f)),
                colors = CardDefaults.cardColors(containerColor = accentFor(tool).copy(alpha = 0.08f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier.size(58.dp).background(
                            Brush.linearGradient(listOf(accentFor(tool), accentFor(tool).copy(alpha = 0.72f))),
                            RoundedCornerShape(18.dp),
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(tool.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tool.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            when (tool) {
                                PdfTool.MERGE -> "Combina vários documentos num só ficheiro."
                                PdfTool.SPLIT -> "Extrai apenas o intervalo de páginas necessário."
                                PdfTool.COMPRESS -> "Reduz o peso do ficheiro quando existem imagens grandes."
                                PdfTool.REORDER -> "Define a sequência final das páginas."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = when (tool) {
                    PdfTool.MERGE -> "Selecione dois ou mais PDFs para criar um único documento."
                    PdfTool.SPLIT -> "Selecione um PDF e indique o intervalo de páginas."
                    PdfTool.COMPRESS -> "Imagens grandes serão reduzidas e recomprimidas localmente."
                    PdfTool.REORDER -> "Indique a nova ordem, por exemplo: 3,1,2,4."
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            if (tool == PdfTool.MERGE) {
                OutlinedButton(
                    onClick = { multiPicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Folder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Selecionar PDFs")
                }
            } else {
                OutlinedButton(
                    onClick = { singlePicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Folder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Selecionar PDF")
                }
            }

            if (selectedUris.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Ficheiros selecionados", style = MaterialTheme.typography.titleSmall)
                        selectedUris.forEachIndexed { index, uri ->
                            Text(
                                (index + 1).toString() + ". " +
                                    (uri.lastPathSegment ?: "PDF"),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            when (tool) {
                PdfTool.SPLIT -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = startPage,
                            onValueChange = { startPage = it.filter(Char::isDigit) },
                            label = { Text("De") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = endPage,
                            onValueChange = { endPage = it.filter(Char::isDigit) },
                            label = { Text("Até") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                PdfTool.REORDER -> {
                    OutlinedTextField(
                        value = orderText,
                        onValueChange = { orderText = it },
                        label = { Text("Nova ordem") },
                        supportingText = { Text("Ex.: 3,1,2,4") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                else -> Unit
            }

            Button(
                onClick = {
                    when (tool) {
                        PdfTool.MERGE -> {
                            if (selectedUris.size < 2) {
                                scope.launch {
                                    snackbar.showSnackbar("Selecione pelo menos dois PDFs.")
                                }
                            } else {
                                createOutput("docpilot-merged.pdf") { uri ->
                                    PdfProcessor.merge(resolver, selectedUris, uri)
                                }
                            }
                        }

                        PdfTool.SPLIT -> {
                            val start = startPage.toIntOrNull()
                            val end = endPage.toIntOrNull()

                            if (selectedUris.size != 1 || start == null || end == null) {
                                scope.launch {
                                    snackbar.showSnackbar(
                                        "Selecione um PDF e um intervalo válido."
                                    )
                                }
                            } else {
                                createOutput("docpilot-split.pdf") { uri ->
                                    PdfProcessor.extractPages(
                                        resolver,
                                        selectedUris.single(),
                                        uri,
                                        start,
                                        end,
                                    )
                                }
                            }
                        }

                        PdfTool.COMPRESS -> {
                            if (selectedUris.size != 1) {
                                scope.launch {
                                    snackbar.showSnackbar("Selecione um PDF.")
                                }
                            } else {
                                createOutput("docpilot-compressed.pdf") { uri ->
                                    PdfProcessor.compress(
                                        resolver,
                                        selectedUris.single(),
                                        uri,
                                    )
                                }
                            }
                        }

                        PdfTool.REORDER -> {
                            val orderResult = runCatching {
                                parsePageOrder(orderText)
                            }

                            if (orderResult.isFailure) {
                                scope.launch {
                                    snackbar.showSnackbar(
                                        orderResult.exceptionOrNull()?.message
                                            ?: "Ordem inválida."
                                    )
                                }
                            } else if (selectedUris.size != 1) {
                                scope.launch {
                                    snackbar.showSnackbar("Selecione um PDF.")
                                }
                            } else {
                                createOutput("docpilot-reordered.pdf") { uri ->
                                    PdfProcessor.reorder(
                                        resolver,
                                        selectedUris.single(),
                                        uri,
                                        orderResult.getOrThrow(),
                                    )
                                }
                            }
                        }
                    }
                },
                enabled = !processing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                if (processing) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("A processar…")
                } else {
                    Text("Criar PDF")
                }
            }

            Text(
                "O processamento é local no dispositivo. Os PDFs não são enviados para um servidor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(18.dp))
        }
    }
    if (processing) {
        ProcessingOverlay(
            when (tool) {
                PdfTool.MERGE -> "A juntar os PDFs"
                PdfTool.SPLIT -> "A dividir o documento"
                PdfTool.COMPRESS -> "A comprimir o PDF"
                PdfTool.REORDER -> "A reorganizar as páginas"
            },
            "A preparar o novo ficheiro. Mantém o DocPilot aberto até terminar.",
        )
    }
}


private fun categoryFor(card: ToolCard): String = when {
    card.pdfTool != null || card.action == Action.PDF_INFO || card.action == Action.EXTRACT_TEXT -> "PDF"
    card.securityTool != null -> "Segurança"
    card.action == Action.SCAN || card.action == Action.OCR -> "Digitalização"
    card.action == Action.IMAGE_TO_PDF -> "Conversão"
    else -> "PDF"
}

private fun accentFor(card: ToolCard): Color = when {
    card.pdfTool != null -> accentFor(card.pdfTool)
    card.securityTool != null -> accentFor(card.securityTool)
    card.action == Action.SCAN -> Color(0xFF0C8A78)
    card.action == Action.IMAGE_TO_PDF -> Color(0xFFE58A20)
    card.action == Action.OCR -> Color(0xFF8A4DE8)
    card.action == Action.EXTRACT_TEXT -> Color(0xFF2F63D8)
    else -> Color(0xFF4566C8)
}

private fun accentFor(tool: PdfTool): Color = when (tool) {
    PdfTool.MERGE -> Color(0xFF3267D5)
    PdfTool.SPLIT -> Color(0xFF7C4DFF)
    PdfTool.COMPRESS -> Color(0xFF008F8C)
    PdfTool.REORDER -> Color(0xFFE07A22)
}

private fun accentFor(tool: SecurityTool): Color = when (tool) {
    SecurityTool.PROTECT -> Color(0xFFD34D67)
    SecurityTool.SIGN -> Color(0xFF7250E8)
}

private fun inspectPdf(context: Context, uri: Uri): String {
    var name = "Documento.pdf"
    var sizeBytes = -1L
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
        }
    }
    val pageCount = context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
        PdfRenderer(descriptor).use { renderer -> renderer.pageCount }
    } ?: error("O PDF não pôde ser aberto.")
    val sizeText = if (sizeBytes > 0L) formatBytes(sizeBytes) else "Não disponível"
    return "Nome: $name\nPáginas: $pageCount\nTamanho: $sizeText\n\nO ficheiro foi analisado localmente no dispositivo."
}

@Composable
private fun ProcessingOverlay(title: String, detail: String) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f)).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.94f, animationSpec = tween(180)),
                exit = fadeOut(tween(120)),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp).background(
                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                                RoundedCornerShape(24.dp),
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                        }
                        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Text(detail, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text("Processamento local", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

 
private fun extractPdfTextFromDocument(context: Context, uri: Uri): String {
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Não foi possível ler este PDF." }
        PDDocument.load(input).use { document ->
            return PDFTextStripper().getText(document).trim()
        }
    }
}
