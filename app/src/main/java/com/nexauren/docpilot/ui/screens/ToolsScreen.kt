package com.nexauren.docpilot.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
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
    ROTATE("Rodar páginas", "Rodar páginas 90°", Icons.Outlined.RotateRight),
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

private enum class Action { NONE, SCAN, IMAGE_TO_PDF, OCR, PDF_TEXT }

private val toolCards = listOf(
    ToolCard("Juntar PDFs", "Combinar documentos", Icons.Outlined.Description, pdfTool = PdfTool.MERGE),
    ToolCard("Dividir PDF", "Extrair páginas", Icons.Outlined.CallSplit, pdfTool = PdfTool.SPLIT),
    ToolCard("Comprimir", "Reduzir tamanho", Icons.Outlined.Archive, pdfTool = PdfTool.COMPRESS),
    ToolCard("Organizar", "Reordenar páginas", Icons.Outlined.Edit, pdfTool = PdfTool.REORDER),
    ToolCard("Rodar páginas", "Rodar páginas 90°", Icons.Outlined.RotateRight, pdfTool = PdfTool.ROTATE),
    ToolCard("Scanner", "Digitalizar documentos", Icons.Outlined.CameraAlt, action = Action.SCAN),
    ToolCard("Imagem → PDF", "Converter imagens", Icons.Outlined.PhotoLibrary, action = Action.IMAGE_TO_PDF),
    ToolCard("Assinatura visual", "Adicionar assinatura", Icons.Outlined.Create, securityTool = SecurityTool.SIGN),
    ToolCard("Proteger", "Senha e permissões", Icons.Outlined.Lock, securityTool = SecurityTool.PROTECT),
    ToolCard("OCR de imagem", "Ler texto em fotos", Icons.Outlined.Description, action = Action.OCR),
    ToolCard("Extrair texto PDF", "Exportar texto para TXT", Icons.Outlined.ContentCopy, action = Action.PDF_TEXT),
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
    var processing by remember { mutableStateOf(false) }
    var toolQuery by remember { mutableStateOf("") }
    var pendingPdfTextUri by remember { mutableStateOf<Uri?>(null) }

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

    val textOutputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { outputUri ->
        val sourceUri = pendingPdfTextUri
        pendingPdfTextUri = null
        if (sourceUri != null && outputUri != null) {
            processing = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        DocumentTools.extractPdfTextToTextFile(context.contentResolver, sourceUri, outputUri)
                    }
                }
                processing = false
                snackbar.showSnackbar(
                    result.fold(
                        { "Texto exportado • " + formatBytes(it) },
                        { "Não foi possível extrair texto: " + (it.message ?: "erro desconhecido") },
                    ),
                )
            }
        }
    }

    val pdfTextPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingPdfTextUri = uri
            textOutputPicker.launch("docpilot-text.txt")
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
        val filteredTools = remember(toolQuery) {
            toolCards.filter {
                it.title.contains(toolQuery, ignoreCase = true) ||
                    it.subtitle.contains(toolQuery, ignoreCase = true)
            }
        }
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = toolQuery,
                    onValueChange = { toolQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (toolQuery.isNotEmpty()) {
                            IconButton(onClick = { toolQuery = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Limpar pesquisa")
                            }
                        }
                    },
                    placeholder = { Text("Pesquisar ferramentas") },
                )
                Text(
                    if (toolQuery.isBlank()) "Ferramentas para o teu fluxo de trabalho"
                    else "${filteredTools.size} resultado(s)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    itemsIndexed(filteredTools, key = { _, card -> card.title }) { index, card ->
                        val accent = toolAccent(index)
                        val enabled = card.pdfTool != null || card.securityTool != null || card.action != Action.NONE
                        Card(
                            onClick = {
                                when {
                                    card.pdfTool != null -> selectedPdfTool = card.pdfTool
                                    card.securityTool != null -> selectedSecurityTool = card.securityTool
                                    card.action == Action.SCAN -> launchScanner()
                                    card.action == Action.IMAGE_TO_PDF -> imagePicker.launch(arrayOf("image/*"))
                                    card.action == Action.OCR -> ocrPicker.launch(arrayOf("image/*"))
                                    card.action == Action.PDF_TEXT -> pdfTextPicker.launch(arrayOf("application/pdf"))
                                }
                            },
                            enabled = enabled,
                            modifier = Modifier.animateContentSize(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.16f)))),
                                )
                                Column(
                                    Modifier.padding(15.dp),
                                    verticalArrangement = Arrangement.spacedBy(9.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(accent.copy(alpha = 0.13f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            card.icon,
                                            contentDescription = null,
                                            tint = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(25.dp),
                                        )
                                    }
                                    Text(card.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        card.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (!enabled) {
                                        Text(
                                            "Em breve",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = processing,
                modifier = Modifier.align(Alignment.Center).padding(20.dp),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                ProcessingPanel(
                    title = "A processar documento",
                    detail = "A trabalhar localmente no dispositivo. Mantém esta janela aberta.",
                    accent = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun toolAccent(index: Int): Color {
    val colors = listOf(
        Color(0xFF356AE6),
        Color(0xFF7C4DFF),
        Color(0xFF008C95),
        Color(0xFFB45309),
        Color(0xFFDB4B73),
        Color(0xFF387D4A),
        Color(0xFF9356CF),
        Color(0xFF2376A8),
        Color(0xFFB76D28),
        Color(0xFF515BC4),
    )
    return colors[index % colors.size]
}

private fun pdfToolAccent(tool: PdfTool): Color = when (tool) {
    PdfTool.MERGE -> Color(0xFF356AE6)
    PdfTool.SPLIT -> Color(0xFF7C4DFF)
    PdfTool.COMPRESS -> Color(0xFF008C95)
    PdfTool.REORDER -> Color(0xFFB45309)
    PdfTool.ROTATE -> Color(0xFFDB4B73)
}

@Composable
private fun WorkspaceHero(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.22f)))),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(29.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProcessingPanel(
    title: String,
    detail: String,
    accent: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = accent, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(color = accent, modifier = Modifier.fillMaxWidth())
            }
        }
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
            WorkspaceHero(
                title = tool.title,
                subtitle = if (tool == SecurityTool.PROTECT)
                    "Protege uma cópia do teu documento."
                else
                    "Adiciona a assinatura visual e guarda uma cópia.",
                icon = tool.icon,
                accent = if (tool == SecurityTool.PROTECT) Color(0xFFB45309) else Color(0xFF7C4DFF),
            )
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

            AnimatedVisibility(
                visible = processing,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                ProcessingPanel(
                    title = if (tool == SecurityTool.PROTECT) "A proteger o PDF" else "A aplicar assinatura",
                    detail = "A preparar uma nova cópia sem alterar o ficheiro original.",
                    accent = if (tool == SecurityTool.PROTECT) Color(0xFFB45309) else Color(0xFF7C4DFF),
                )
            }

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
            WorkspaceHero(
                title = tool.title,
                subtitle = when (tool) {
                    PdfTool.MERGE -> "Combina vários ficheiros num só."
                    PdfTool.SPLIT -> "Escolhe o intervalo que queres guardar."
                    PdfTool.COMPRESS -> "Otimiza imagens incorporadas no PDF."
                    PdfTool.REORDER -> "Define a sequência final das páginas."
                    PdfTool.ROTATE -> "Roda cada página 90° e guarda uma nova cópia."
                },
                icon = tool.icon,
                accent = pdfToolAccent(tool),
            )

            Text(
                text = when (tool) {
                    PdfTool.MERGE -> "Selecione dois ou mais PDFs para criar um único documento."
                    PdfTool.SPLIT -> "Selecione um PDF e indique o intervalo de páginas."
                    PdfTool.COMPRESS -> "Imagens grandes serão reduzidas e recomprimidas localmente."
                    PdfTool.REORDER -> "Indique a nova ordem, por exemplo: 3,1,2,4."
                    PdfTool.ROTATE -> "Roda todas as páginas 90° no sentido horário."
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

            AnimatedVisibility(
                visible = processing,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                ProcessingPanel(
                    title = when (tool) {
                        PdfTool.MERGE -> "A juntar documentos"
                        PdfTool.SPLIT -> "A extrair páginas"
                        PdfTool.COMPRESS -> "A otimizar imagens"
                        PdfTool.REORDER -> "A reorganizar páginas"
                        PdfTool.ROTATE -> "A rodar páginas"
                    },
                    detail = "O DocPilot está a criar uma nova cópia do teu PDF.",
                    accent = pdfToolAccent(tool),
                )
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

                        PdfTool.ROTATE -> {
                            if (selectedUris.size != 1) {
                                scope.launch { snackbar.showSnackbar("Selecione um PDF.") }
                            } else {
                                createOutput("docpilot-rotated.pdf") { uri ->
                                    PdfProcessor.rotateAllPages(resolver, selectedUris.single(), uri)
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
}
