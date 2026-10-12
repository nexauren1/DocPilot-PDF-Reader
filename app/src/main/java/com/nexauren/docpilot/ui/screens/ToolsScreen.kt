package com.nexauren.docpilot.ui.screens

import com.nexauren.docpilot.ui.localization.localized

import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import com.google.android.gms.tasks.Tasks
import com.nexauren.docpilot.pdf.DocumentTools
import com.tom_roush.pdfbox.pdmodel.PDDocument
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
    ROTATE("Rodar páginas", "Rodar todas as páginas 90°", Icons.Outlined.RotateRight),
    ROTATE_ONE("Rodar uma página", "Escolher a página e a rotação", Icons.Outlined.RotateRight),
    CROP_ONE("Recortar uma página", "Ajustar a área visível de uma página", Icons.Outlined.Crop),
    EXPORT_PAGE_IMAGE("Página para PNG", "Exportar uma página como imagem", Icons.Outlined.PhotoLibrary),
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

private enum class Action { NONE, SCAN, IMAGE_TO_PDF, OCR, PDF_TEXT, PDF_INFO }

private fun displayName(context: android.content.Context, uri: Uri, fallback: String): String {
    val fromProvider = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else null
            }
    }.getOrNull()
    return fromProvider?.takeIf { it.isNotBlank() }
        ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        ?: fallback
}

private val toolCards = listOf(
    ToolCard("Juntar PDFs", "Combinar documentos", Icons.Outlined.Description, pdfTool = PdfTool.MERGE),
    ToolCard("Dividir PDF", "Extrair páginas", Icons.Outlined.CallSplit, pdfTool = PdfTool.SPLIT),
    ToolCard("Comprimir", "Reduzir tamanho", Icons.Outlined.Archive, pdfTool = PdfTool.COMPRESS),
    ToolCard("Organizar", "Reordenar páginas", Icons.Outlined.Edit, pdfTool = PdfTool.REORDER),
    ToolCard("Rodar páginas", "Rodar todas as páginas", Icons.Outlined.RotateRight, pdfTool = PdfTool.ROTATE),
    ToolCard("Rodar uma página", "Rodar só a página escolhida", Icons.Outlined.RotateRight, pdfTool = PdfTool.ROTATE_ONE),
    ToolCard("Recortar página", "Ajustar a área de uma página", Icons.Outlined.Crop, pdfTool = PdfTool.CROP_ONE),
    ToolCard("Página para PNG", "Exportar uma página como imagem", Icons.Outlined.PhotoLibrary, pdfTool = PdfTool.EXPORT_PAGE_IMAGE),
    ToolCard("Scanner", "Digitalizar documentos", Icons.Outlined.CameraAlt, action = Action.SCAN),
    ToolCard("Imagem → PDF", "Converter imagens", Icons.Outlined.PhotoLibrary, action = Action.IMAGE_TO_PDF),
    ToolCard("Assinatura visual", "Adicionar assinatura", Icons.Outlined.Create, securityTool = SecurityTool.SIGN),
    ToolCard("Proteger", "Senha e permissões", Icons.Outlined.Lock, securityTool = SecurityTool.PROTECT),
    ToolCard("OCR de imagem", "Ler texto em fotos", Icons.Outlined.Description, action = Action.OCR),
    ToolCard("Extrair texto PDF", "Exportar texto para TXT", Icons.Outlined.ContentCopy, action = Action.PDF_TEXT),
    ToolCard("Informações do PDF", "Páginas, tamanho e metadados", Icons.Outlined.Info, action = Action.PDF_INFO),
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
    var resultPdf by remember { mutableStateOf<CachedPdfResult?>(null) }
    var resultImage by remember { mutableStateOf<CachedImageResult?>(null) }
    var resultText by remember { mutableStateOf<CachedTextResult?>(null) }
    var activeUtility by remember { mutableStateOf<Action?>(null) }
    var processing by remember { mutableStateOf(false) }
    var toolQuery by remember { mutableStateOf("") }
    var pendingPdfTextUri by remember { mutableStateOf<Uri?>(null) }
    var pendingScannerUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var workflowError by remember { mutableStateOf<String?>(null) }

    fun processPdfResult(
        outputName: String,
        action: (Uri) -> Long,
        previewPage: Int = 1,
    ) {
        if (processing) return
        processing = true
        workflowError = null
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching { createCachedPdfResult(context, outputName, action, previewPage) }
            }
            processing = false
            outcome.fold(
                onSuccess = {
                    resultPdf = it
                },
                onFailure = {
                    workflowError = it.message ?: "O processamento não foi concluído."
                },
            )
        }
    }

    fun processImageResult(outputName: String, action: (Uri) -> Long) {
        if (processing) return
        processing = true
        workflowError = null
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching { createCachedImageResult(context, outputName, action) }
            }
            processing = false
            outcome.fold(
                onSuccess = { resultImage = it },
                onFailure = { workflowError = it.message ?: "A exportação da imagem falhou." },
            )
        }
    }

    fun processTextResult(outputName: String, action: () -> String) {
        if (processing) return
        processing = true
        workflowError = null
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching {
                    val extracted = action()
                    require(extracted.isNotBlank()) { "Não foi encontrado texto para apresentar." }
                    CachedTextResult(extracted, outputName)
                }
            }
            processing = false
            outcome.fold(
                onSuccess = {
                    resultText = it
                },
                onFailure = {
                    workflowError = it.message ?: "O processamento do texto falhou."
                },
            )
        }
    }

    val pdfTextPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingPdfTextUri = uri
            activeUtility = Action.PDF_TEXT
        }
    }
    val pdfInfoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingPdfTextUri = uri
            activeUtility = Action.PDF_INFO
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        pendingImageUris = uris
        if (uris.isNotEmpty()) activeUtility = Action.IMAGE_TO_PDF
    }

    val ocrPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            pendingImageUris = listOf(uri)
            activeUtility = Action.OCR
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
                activeUtility = Action.SCAN
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

    if (workflowError != null) {
        AlertDialog(
            onDismissRequest = { workflowError = null },
            title = { Text(localized("Não foi possível concluir")) },
            text = { Text(workflowError.orEmpty()) },
            confirmButton = {
                Button(onClick = { workflowError = null }) { Text(localized("OK")) }
            },
        )
    }

    resultPdf?.let { result ->
        PdfResultWorkspace(
            result = result,
            onBack = { resultPdf = null },
            onSaved = onOutputSaved,
        )
        return
    }

    resultImage?.let { result ->
        ImageResultWorkspace(result = result, onBack = { resultImage = null })
        return
    }

    resultText?.let { result ->
        TextResultWorkspace(result = result, onBack = { resultText = null })
        return
    }

    if (selectedPdfTool != null) {
        ToolWorkspace(
            tool = selectedPdfTool!!,
            onBack = { selectedPdfTool = null },
            processing = processing,
            onProcessPdf = { name, action -> processPdfResult(name, action) },
            onProcessPdfPage = { name, page, action -> processPdfResult(name, action, page) },
            onProcessImage = { name, action -> processImageResult(name, action) },
        )
        return
    }

    if (selectedSecurityTool != null) {
        SecurityWorkspace(
            tool = selectedSecurityTool!!,
            onBack = { selectedSecurityTool = null },
            processing = processing,
            onProcessPdf = { name, action -> processPdfResult(name, action) },
        )
        return
    }

    if (activeUtility != null) {
        UtilityWorkspace(
            action = activeUtility!!,
            imageUris = pendingImageUris,
            pdfUri = pendingPdfTextUri,
            scanUri = pendingScannerUri,
            processing = processing,
            onBack = { activeUtility = null },
            onProcessPdf = { name, action -> processPdfResult(name, action) },
            onProcessText = { name, action -> processTextResult(name, action) },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localized("Ferramentas")) },
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
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF245BE2), Color(0xFF7250E8))), RoundedCornerShape(24.dp))
                            .padding(18.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(
                                modifier = Modifier.size(52.dp).background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(17.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(localized("Os teus PDFs, mais simples."), style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Text(
                                    toolCards.size.toString() + " ferramentas gratuitas para organizar, proteger e transformar documentos no dispositivo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f),
                                )
                            }
                        }
                    }
                }
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
                    placeholder = { Text(localized("Pesquisar ferramentas")) },
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
                                    card.action == Action.PDF_INFO -> pdfInfoPicker.launch(arrayOf("application/pdf"))
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
                                    Text(localized(card.title), style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        localized(card.subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (!enabled) {
                                        Text(localized("Em breve"),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UtilityWorkspace(
    action: Action,
    imageUris: List<Uri>,
    pdfUri: Uri?,
    scanUri: Uri?,
    processing: Boolean,
    onBack: () -> Unit,
    onProcessPdf: (String, (Uri) -> Long) -> Unit,
    onProcessText: (String, () -> String) -> Unit,
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var paperSize by remember(action) { mutableStateOf("A4") }
    var landscape by remember(action) { mutableStateOf(false) }
    var removeBlankLines by remember(action) { mutableStateOf(false) }

    val title = when (action) {
        Action.SCAN -> "Scanner"
        Action.IMAGE_TO_PDF -> "Imagem para PDF"
        Action.OCR -> "Reconhecer texto (OCR)"
        Action.PDF_TEXT -> "Extrair texto do PDF"
        Action.PDF_INFO -> "Informações do PDF"
        Action.NONE -> "Ferramenta"
    }
    val subtitle = when (action) {
        Action.SCAN -> "Revê a digitalização antes de guardar."
        Action.IMAGE_TO_PDF -> "Configura a página antes de converter as imagens."
        Action.OCR -> "Reconhece texto localmente e mostra-o antes de guardar."
        Action.PDF_TEXT -> "Extrai o texto, revê o conteúdo e só depois guarda."
        Action.PDF_INFO -> "Consulta o tamanho, as páginas e os metadados disponíveis."
        Action.NONE -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localized(title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            WorkspaceHero(
                title = title,
                subtitle = subtitle,
                icon = when (action) {
                    Action.SCAN -> Icons.Outlined.CameraAlt
                    Action.IMAGE_TO_PDF -> Icons.Outlined.PhotoLibrary
                    Action.OCR -> Icons.Outlined.Description
                    Action.PDF_TEXT -> Icons.Outlined.ContentCopy
                    Action.PDF_INFO -> Icons.Outlined.Info
                    Action.NONE -> Icons.Outlined.Description
                },
                accent = when (action) {
                    Action.SCAN -> Color(0xFF387D4A)
                    Action.IMAGE_TO_PDF -> Color(0xFF7C4DFF)
                    Action.OCR -> Color(0xFF356AE6)
                    Action.PDF_TEXT -> Color(0xFF008C95)
                    Action.PDF_INFO -> Color(0xFF7250E8)
                    Action.NONE -> MaterialTheme.colorScheme.primary
                },
            )

            when (action) {
                Action.IMAGE_TO_PDF -> {
                    Text(localized("Imagens selecionadas: ${imageUris.size}"), style = MaterialTheme.typography.titleSmall)
                    Card(shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            imageUris.take(8).forEachIndexed { index, uri ->
                                Text("${index + 1}. ${displayName(context, uri, "Imagem")}",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            if (imageUris.size > 8) Text(localized("+ ${imageUris.size - 8} imagem(ns)"))
                        }
                    }
                    Text(localized("Tamanho da página"), style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A4", "Carta", "A3").forEach { option ->
                            FilterChip(
                                selected = paperSize == option,
                                onClick = { paperSize = option },
                                label = { Text(option) },
                            )
                        }
                    }
                    Text(localized("Orientação"), style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !landscape, onClick = { landscape = false }, label = { Text(localized("Retrato")) })
                        FilterChip(selected = landscape, onClick = { landscape = true }, label = { Text(localized("Paisagem")) })
                    }
                    Text(localized("Cada imagem será ajustada à página escolhida sem alterar o ficheiro de origem."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Action.OCR -> {
                    Text(localized("Imagem selecionada"), style = MaterialTheme.typography.titleSmall)
                    Text(imageUris.firstOrNull()?.let { displayName(context, it, "Imagem") } ?: "Imagem", style = MaterialTheme.typography.bodyMedium)
                    Text(localized("Depois do reconhecimento, poderás rever o texto, copiá-lo ou guardá-lo como TXT."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Action.PDF_TEXT -> {
                    Text(localized("PDF selecionado"), style = MaterialTheme.typography.titleSmall)
                    Text(pdfUri?.let { displayName(context, it, "Documento PDF") } ?: "Documento PDF", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = removeBlankLines, onCheckedChange = { removeBlankLines = it })
                        Column(Modifier.weight(1f)) {
                            Text(localized("Remover linhas vazias"))
                            Text(localized("Útil para limpar texto exportado de alguns documentos."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Action.SCAN -> {
                    Text(localized("Digitalização pronta para processar"), style = MaterialTheme.typography.titleSmall)
                    Text(scanUri?.let { displayName(context, it, "Documento digitalizado") } ?: "Documento digitalizado", style = MaterialTheme.typography.bodyMedium)
                    Text(localized("O DocPilot vai preparar uma pré-visualização. O PDF só será guardado no destino escolhido depois de reveres o resultado."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Action.PDF_INFO -> {
                    Text(localized("PDF selecionado"), style = MaterialTheme.typography.titleSmall)
                    Text(pdfUri?.let { displayName(context, it, "Documento PDF") } ?: "Documento PDF", style = MaterialTheme.typography.bodyMedium)
                    Text(localized("Consulta o número de páginas, o tamanho do ficheiro, as dimensões da primeira página e os metadados. O original não será alterado."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Action.NONE -> Unit
            }

            if (processing) {
                ProcessingPanel(
                    title = when (action) {
                        Action.SCAN -> "A preparar digitalização"
                        Action.IMAGE_TO_PDF -> "A converter imagens"
                        Action.OCR -> "A reconhecer texto"
                        Action.PDF_TEXT -> "A extrair texto"
                        Action.PDF_INFO -> "A analisar informações"
                        Action.NONE -> "A processar"
                    },
                    detail = "O trabalho é feito no dispositivo. Mantém o DocPilot aberto.",
                    accent = MaterialTheme.colorScheme.primary,
                )
            }

            Button(
                enabled = !processing && when (action) {
                    Action.SCAN -> scanUri != null
                    Action.IMAGE_TO_PDF -> imageUris.isNotEmpty()
                    Action.OCR -> imageUris.isNotEmpty()
                    Action.PDF_TEXT -> pdfUri != null
                    Action.PDF_INFO -> pdfUri != null
                    Action.NONE -> false
                },
                onClick = {
                    when (action) {
                        Action.SCAN -> {
                            val source = scanUri ?: return@Button
                            onProcessPdf("docpilot-scan.pdf") { outputUri ->
                                DocumentTools.scanResultPdf(resolver, source, outputUri)
                            }
                        }

                        Action.IMAGE_TO_PDF -> {
                            val dimensions = when (paperSize) {
                                "Carta" -> 612 to 792
                                "A3" -> 842 to 1191
                                else -> 595 to 842
                            }
                            val width = if (landscape) dimensions.second else dimensions.first
                            val height = if (landscape) dimensions.first else dimensions.second
                            onProcessPdf("docpilot-images.pdf") { outputUri ->
                                DocumentTools.imagesToPdf(resolver, imageUris, outputUri, width, height)
                            }
                        }

                        Action.OCR -> {
                            val source = imageUris.firstOrNull() ?: return@Button
                            onProcessText("docpilot-ocr.txt") {
                                val image = InputImage.fromFilePath(context, source)
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                try {
                                    val recognized = Tasks.await(recognizer.process(image)).text.trim()
                                    recognized.ifBlank { "Nenhum texto foi identificado nesta imagem." }
                                } finally {
                                    recognizer.close()
                                }
                            }
                        }

                        Action.PDF_TEXT -> {
                            val source = pdfUri ?: return@Button
                            onProcessText("docpilot-text.txt") {
                                val extracted = DocumentTools.extractPdfText(resolver, source)
                                if (removeBlankLines) {
                                    extracted.lineSequence().filter { it.isNotBlank() }.joinToString("\n")
                                } else {
                                    extracted
                                }
                            }
                        }

                        Action.PDF_INFO -> {
                            val source = pdfUri ?: return@Button
                            onProcessText("docpilot-info.txt") {
                                DocumentTools.inspectPdf(resolver, source)
                            }
                        }

                        Action.NONE -> Unit
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (processing) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(localized("A processar…"))
                } else {
                    Text(
                        when (action) {
                            Action.SCAN -> "Pré-visualizar PDF"
                            Action.IMAGE_TO_PDF -> "Converter e pré-visualizar"
                            Action.OCR -> "Reconhecer texto"
                            Action.PDF_TEXT -> "Extrair e pré-visualizar"
                            Action.PDF_INFO -> "Ver informações"
                            Action.NONE -> "Continuar"
                        },
                    )
                }
            }

            OutlinedButton(onClick = onBack, enabled = !processing, modifier = Modifier.fillMaxWidth()) {
                Text(localized("Cancelar"))
            }
            Spacer(Modifier.height(18.dp))
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
    PdfTool.ROTATE_ONE -> Color(0xFFDB4B73)
    PdfTool.CROP_ONE -> Color(0xFF7C4DFF)
    PdfTool.EXPORT_PAGE_IMAGE -> Color(0xFF16856B)
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
                    Text(localized(title), style = MaterialTheme.typography.titleLarge)
                    Text(localized(subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(localized(title), style = MaterialTheme.typography.titleMedium)
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
    processing: Boolean,
    onProcessPdf: (String, (Uri) -> Long) -> Unit,
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var inputUri by remember { mutableStateOf<Uri?>(null) }
    var value by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        inputUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localized(tool.title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
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
                Text(localized("Selecionar PDF"))
            }

            inputUri?.let {
                Text(localized("PDF selecionado: ") + (displayName(context, it, "documento.pdf")),
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
                        onProcessPdf("docpilot-protected.pdf") { outputUri ->
                            DocumentTools.protectPdf(resolver, source, outputUri, value)
                        }
                    } else {
                        onProcessPdf("docpilot-signed.pdf") { outputUri ->
                            DocumentTools.addVisualSignature(resolver, source, outputUri, value)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (processing) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(localized("A processar…"))
                } else {
                    Text(if (tool == SecurityTool.PROTECT) "Proteger PDF" else "Aplicar assinatura")
                }
            }

            Text(localized("O processamento acontece localmente no dispositivo."),
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
    processing: Boolean,
    onProcessPdf: (String, (Uri) -> Long) -> Unit,
    onProcessPdfPage: (String, Int, (Uri) -> Long) -> Unit,
    onProcessImage: (String, (Uri) -> Long) -> Unit,
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var startPage by remember { mutableStateOf("1") }
    var endPage by remember { mutableStateOf("1") }
    var orderText by remember { mutableStateOf("1") }
    var pageNumber by remember { mutableStateOf("1") }
    var rotationDegrees by remember { mutableStateOf(90) }
    var cropMarginPercent by remember { mutableStateOf(5) }
    var detectedPageCount by remember { mutableStateOf<Int?>(null) }

    val multiPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedUris = uris
    }

    val singlePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUris = listOf(uri)
            detectedPageCount = null
            pageNumber = "1"
            scope.launch {
                val pageCountResult = withContext(Dispatchers.IO) {
                    runCatching {
                        resolver.openInputStream(uri).use { input ->
                            requireNotNull(input) { "Não foi possível abrir este PDF." }
                            PDDocument.load(input).use { it.numberOfPages }
                        }
                    }
                }
                pageCountResult.onSuccess { count ->
                    detectedPageCount = count
                    if (count > 0) {
                        endPage = count.toString()
                        orderText = (1..count).joinToString(",")
                    }
                }.onFailure {
                    snackbar.showSnackbar("Não foi possível ler as páginas: " + (it.message ?: "erro desconhecido"))
                }
            }
        }
    }

    fun createOutput(name: String, action: (Uri) -> Long) {
        onProcessPdf(name, action)
    }

    fun createPageOutput(name: String, page: Int, action: (Uri) -> Long) {
        onProcessPdfPage(name, page, action)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localized(tool.title)) },
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
                    PdfTool.ROTATE -> "Roda todas as páginas 90° e guarda uma nova cópia."
                    PdfTool.ROTATE_ONE -> "Roda apenas a página que escolheres."
                    PdfTool.CROP_ONE -> "Recorta apenas a área visível da página escolhida."
                    PdfTool.EXPORT_PAGE_IMAGE -> "Cria uma imagem PNG de uma página do PDF."
                },
                icon = tool.icon,
                accent = pdfToolAccent(tool),
            )

            Text(
                text = when (tool) {
                    PdfTool.MERGE -> "Selecione dois ou mais PDFs para criar um único documento."
                    PdfTool.SPLIT -> "Selecione um PDF e indique o intervalo de páginas."
                    PdfTool.COMPRESS -> "Otimiza imagens incorporadas, quando adequado. PDFs de texto ou vetoriais podem reduzir pouco."
                    PdfTool.REORDER -> "Indique a nova ordem, por exemplo: 3,1,2,4."
                    PdfTool.ROTATE -> "Roda todas as páginas 90° no sentido horário."
                    PdfTool.ROTATE_ONE -> "Escolhe o número da página e roda-a 90°, 180° ou 270°."
                    PdfTool.CROP_ONE -> "Escolhe uma página e aplica um recorte proporcional. O PDF original não será alterado."
                    PdfTool.EXPORT_PAGE_IMAGE -> "Exporta uma página para PNG, mantendo a proporção original."
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
                    Text(localized("Selecionar PDFs"))
                }
            } else {
                OutlinedButton(
                    onClick = { singlePicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Folder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(localized("Selecionar PDF"))
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
                        Text(localized("Ficheiros selecionados"), style = MaterialTheme.typography.titleSmall)
                        selectedUris.forEachIndexed { index, uri ->
                            Text(
                                (index + 1).toString() + ". " +
                                    (displayName(context, uri, "PDF")),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            when (tool) {
                PdfTool.SPLIT -> {
                    detectedPageCount?.let { count ->
                        Text(localized("Documento com $count página(s)."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = startPage,
                            onValueChange = { startPage = it.filter(Char::isDigit) },
                            label = { Text(localized("De")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = endPage,
                            onValueChange = { endPage = it.filter(Char::isDigit) },
                            label = { Text(localized("Até")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                PdfTool.REORDER -> {
                    detectedPageCount?.let { count ->
                        Text(localized("Páginas disponíveis: 1 a $count. A ordem é preenchida automaticamente ao selecionar o PDF."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedTextField(
                        value = orderText,
                        onValueChange = { orderText = it },
                        label = { Text(localized("Nova ordem")) },
                        supportingText = { Text(localized("Ex.: 3,1,2,4")) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                PdfTool.ROTATE_ONE, PdfTool.CROP_ONE, PdfTool.EXPORT_PAGE_IMAGE -> {
                    detectedPageCount?.let { count ->
                        Text(localized("O documento tem $count página(s)."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedTextField(
                        value = pageNumber,
                        onValueChange = { pageNumber = it.filter(Char::isDigit) },
                        label = { Text(localized("Número da página")) },
                        supportingText = {
                            Text("Escolhe um valor entre 1 e ${detectedPageCount ?: "…"}")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (tool == PdfTool.ROTATE_ONE) {
                        Text(localized("Rotação"), style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(90, 180, 270).forEach { angle ->
                                FilterChip(
                                    selected = rotationDegrees == angle,
                                    onClick = { rotationDegrees = angle },
                                    label = { Text(localized("$angle°")) },
                                )
                            }
                        }
                    }
                    if (tool == PdfTool.CROP_ONE) {
                        Text(localized("Margem do recorte por lado"), style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15).forEach { margin ->
                                FilterChip(
                                    selected = cropMarginPercent == margin,
                                    onClick = { cropMarginPercent = margin },
                                    label = { Text(localized("$margin%")) },
                                )
                            }
                        }
                        Text(localized("O recorte altera a área visível. O conteúdo fora da área recortada não é apagado permanentemente."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                        PdfTool.ROTATE -> "A rodar todas as páginas"
                        PdfTool.ROTATE_ONE -> "A rodar uma página"
                        PdfTool.CROP_ONE -> "A recortar uma página"
                        PdfTool.EXPORT_PAGE_IMAGE -> "A exportar a página como PNG"
                    },
                    detail = "O DocPilot - PDF Reader está a criar uma nova cópia do teu PDF.",
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

                        PdfTool.ROTATE_ONE -> {
                            val number = pageNumber.toIntOrNull()
                            val count = detectedPageCount
                            if (selectedUris.size != 1 || number == null || count == null || number !in 1..count) {
                                scope.launch { snackbar.showSnackbar("Seleciona um PDF e uma página válida.") }
                            } else {
                                createPageOutput("docpilot-page-rotated.pdf", number) { uri ->
                                    PdfProcessor.rotatePage(
                                        resolver, selectedUris.single(), uri, number, rotationDegrees,
                                    )
                                }
                            }
                        }

                        PdfTool.CROP_ONE -> {
                            val number = pageNumber.toIntOrNull()
                            val count = detectedPageCount
                            if (selectedUris.size != 1 || number == null || count == null || number !in 1..count) {
                                scope.launch { snackbar.showSnackbar("Seleciona um PDF e uma página válida.") }
                            } else {
                                createPageOutput("docpilot-page-cropped.pdf", number) { uri ->
                                    PdfProcessor.cropPage(
                                        resolver, selectedUris.single(), uri, number, cropMarginPercent / 100f,
                                    )
                                }
                            }
                        }

                        PdfTool.EXPORT_PAGE_IMAGE -> {
                            val number = pageNumber.toIntOrNull()
                            val count = detectedPageCount
                            if (selectedUris.size != 1 || number == null || count == null || number !in 1..count) {
                                scope.launch { snackbar.showSnackbar("Seleciona um PDF e uma página válida.") }
                            } else {
                                onProcessImage("docpilot-page-$number.png") { uri ->
                                    DocumentTools.exportPdfPageAsPng(resolver, selectedUris.single(), number, uri)
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
                    Text(localized("A processar…"))
                } else {
                    Text(if (tool == PdfTool.EXPORT_PAGE_IMAGE) "Exportar PNG" else "Criar PDF")
                }
            }

            Text(
                if (tool == PdfTool.COMPRESS) {
                    "A compressão tenta reduzir imagens raster grandes. O resultado pode não ficar menor em PDFs já otimizados; compara o tamanho antes de substituir o original."
                } else {
                    "O processamento é local no dispositivo. Os PDFs não são enviados para um servidor."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(18.dp))
        }
    }
}
