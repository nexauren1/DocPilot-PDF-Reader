package com.nexauren.docpilot.ui.screens

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.nexauren.docpilot.ui.localization.LText
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nexauren.docpilot.pdf.formatBytes
import java.io.File
import java.io.FileInputStream

internal data class CachedPdfResult(
    val file: File,
    val outputName: String,
    val sizeBytes: Long,
    val previewPage: Int = 1,
)

internal data class CachedTextResult(
    val text: String,
    val outputName: String = "pdf-tools-text.txt",
)

internal data class CachedImageResult(
    val file: File,
    val outputName: String,
    val sizeBytes: Long,
)

internal fun createCachedPdfResult(
    context: Context,
    outputName: String,
    action: (Uri) -> Long,
    previewPage: Int = 1,
): CachedPdfResult {
    val directory = File(context.cacheDir, "tool-results")
    check(directory.exists() || directory.mkdirs()) { "Não foi possível preparar o espaço temporário." }
    val file = File.createTempFile("pdf-tools-result-", ".pdf", directory)
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file,
        )
        val reportedSize = action(uri)
        check(file.exists() && file.length() > 0L) { "A ferramenta não produziu um PDF válido." }
        return CachedPdfResult(
            file = file,
            outputName = outputName,
            sizeBytes = reportedSize.takeIf { it > 0L } ?: file.length(),
            previewPage = previewPage.coerceAtLeast(1),
        )
    } catch (error: Throwable) {
        file.delete()
        throw error
    }
}

internal fun createCachedImageResult(
    context: Context,
    outputName: String,
    action: (Uri) -> Long,
): CachedImageResult {
    val directory = File(context.cacheDir, "tool-results")
    check(directory.exists() || directory.mkdirs()) {
        "Não foi possível preparar o espaço temporário."
    }
    val file = File.createTempFile("pdf-tools-image-", ".png", directory)
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file,
        )
        val reportedSize = action(uri)
        check(file.exists() && file.length() > 8L) {
            "A ferramenta não produziu uma imagem PNG válida."
        }
        return CachedImageResult(
            file = file,
            outputName = outputName,
            sizeBytes = reportedSize.takeIf { it > 0L } ?: file.length(),
        )
    } catch (error: Throwable) {
        file.delete()
        throw error
    }
}

private fun copyFileToUri(context: Context, source: File, destination: Uri): Long {
    context.contentResolver.openOutputStream(destination, "wt").use { output ->
        requireNotNull(output) { "Não foi possível abrir o destino escolhido." }
        FileInputStream(source).use { input -> input.copyTo(output) }
    }
    return source.length()
}

private data class PdfPreviewData(
    val bitmap: Bitmap,
    val pageCount: Int,
    val displayedPage: Int,
)

@Composable
private fun PdfFirstPagePreview(file: File, requestedPage: Int) {
    var preview by remember(file, requestedPage) { mutableStateOf<PdfPreviewData?>(null) }
    var previewError by remember(file, requestedPage) { mutableStateOf<String?>(null) }

    LaunchedEffect(file, requestedPage) {
        preview = null
        previewError = null
        val outcome = withContext(Dispatchers.IO) {
            runCatching {
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        require(renderer.pageCount > 0) { "O documento não contém páginas." }
                        val pageNumber = requestedPage.coerceIn(1, renderer.pageCount)
                        renderer.openPage(pageNumber - 1).use { page ->
                            // Preserve the selected page's natural ratio and cap memory use.
                            val scale = minOf(
                                720f / page.width.toFloat(),
                                1800f / page.height.toFloat(),
                            )
                            val width = (page.width * scale).toInt().coerceAtLeast(1)
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            val transform = android.graphics.Matrix().apply {
                                setScale(width.toFloat() / page.width, height.toFloat() / page.height)
                            }
                            page.render(bitmap, null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            PdfPreviewData(bitmap, renderer.pageCount, pageNumber)
                        }
                    }
                }
            }
        }
        outcome.fold(
            onSuccess = { preview = it },
            onFailure = { previewError = it.message ?: "Não foi possível pré-visualizar este PDF." },
        )
    }

    DisposableEffect(preview?.bitmap) {
        val bitmap = preview?.bitmap
        onDispose { bitmap?.recycle() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when {
                preview != null -> {
                    Image(
                        bitmap = preview!!.bitmap.asImageBitmap(),
                        contentDescription = "Pré-visualização da página ${preview!!.displayedPage} do PDF",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                        contentScale = ContentScale.Fit,
                    )
                    LText(
                        "Página ${preview!!.displayedPage} de ${preview!!.pageCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                previewError != null -> LText(
                    previewError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                else -> {
                    CircularProgressIndicator()
                    LText("A preparar pré-visualização…", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PdfResultWorkspace(
    result: CachedPdfResult,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }

    // Delete the private temporary file when the result workspace is closed.
    DisposableEffect(result.file) {
        onDispose { result.file.delete() }
    }

    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { destination ->
        if (destination != null) {
            saving = true
            scope.launch {
                val outcome = withContext(Dispatchers.IO) {
                    runCatching { copyFileToUri(context, result.file, destination) }
                }
                saving = false
                outcome.fold(
                    onSuccess = {
                        onSaved(destination.toString())
                        snackbar.showSnackbar("PDF guardado com sucesso.")
                    },
                    onFailure = {
                        snackbar.showSnackbar("Não foi possível guardar: ${it.message ?: "erro desconhecido"}")
                    },
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LText("Pré-visualização") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            LText("O resultado está pronto", style = MaterialTheme.typography.headlineSmall)
            LText(
                "Revê a página ${result.previewPage}. O ficheiro original não foi alterado. Guarda uma cópia quando estiveres satisfeito.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PdfFirstPagePreview(result.file, result.previewPage)
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    LText(result.outputName, style = MaterialTheme.typography.titleMedium)
                    LText(
                        "PDF • ${formatBytes(result.sizeBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(
                onClick = { savePicker.launch(result.outputName) },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(10.dp))
                    LText("A guardar…")
                } else {
                    Icon(Icons.Outlined.SaveAlt, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    LText("Guardar PDF")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                LText("Voltar às opções")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

private data class ImagePreviewData(val bitmap: Bitmap)

@Composable
private fun PngResultPreview(file: File) {
    var preview by remember(file) { mutableStateOf<ImagePreviewData?>(null) }
    var previewError by remember(file) { mutableStateOf<String?>(null) }

    LaunchedEffect(file) {
        preview = null
        previewError = null
        val outcome = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, bounds)
                require(bounds.outWidth > 0 && bounds.outHeight > 0) {
                    "A imagem exportada não é válida."
                }
                var sampleSize = 1
                while (
                    bounds.outWidth / sampleSize > 1200 ||
                    bounds.outHeight / sampleSize > 1200
                ) {
                    sampleSize *= 2
                }
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                requireNotNull(bitmap) { "Não foi possível pré-visualizar a imagem." }
                ImagePreviewData(bitmap)
            }
        }
        outcome.fold(
            onSuccess = { preview = it },
            onFailure = { previewError = it.message ?: "Não foi possível carregar a imagem." },
        )
    }

    DisposableEffect(preview?.bitmap) {
        val bitmap = preview?.bitmap
        onDispose { bitmap?.recycle() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                preview != null -> Image(
                    bitmap = preview!!.bitmap.asImageBitmap(),
                    contentDescription = "Pré-visualização da página exportada",
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                    contentScale = ContentScale.Fit,
                )
                previewError != null -> LText(
                    previewError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                )
                else -> CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImageResultWorkspace(
    result: CachedImageResult,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }

    DisposableEffect(result.file) {
        onDispose { result.file.delete() }
    }

    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png"),
    ) { destination ->
        if (destination != null) {
            saving = true
            scope.launch {
                val outcome = withContext(Dispatchers.IO) {
                    runCatching { copyFileToUri(context, result.file, destination) }
                }
                saving = false
                outcome.fold(
                    onSuccess = { snackbar.showSnackbar("Imagem PNG guardada com sucesso.") },
                    onFailure = {
                        snackbar.showSnackbar(
                            "Não foi possível guardar a imagem: ${it.message ?: "erro desconhecido"}",
                        )
                    },
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LText("Pré-visualização da imagem") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            LText("A página foi exportada", style = MaterialTheme.typography.headlineSmall)
            LText(
                "Revê a imagem antes de a guardar. O PDF original não foi alterado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PngResultPreview(result.file)
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(
                    Modifier.fillMaxWidth().padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    LText(result.outputName, style = MaterialTheme.typography.titleMedium)
                    LText(
                        "PNG • ${formatBytes(result.sizeBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(
                onClick = { savePicker.launch(result.outputName) },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(10.dp))
                    LText("A guardar…")
                } else {
                    Icon(Icons.Outlined.SaveAlt, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    LText("Guardar imagem PNG")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                LText("Voltar às ferramentas")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TextResultWorkspace(
    result: CachedTextResult,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }

    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { destination ->
        if (destination != null) {
            saving = true
            scope.launch {
                val outcome = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(destination, "wt").use { output ->
                            requireNotNull(output) { "Não foi possível abrir o destino escolhido." }
                            output.write(result.text.toByteArray(Charsets.UTF_8))
                        }
                    }
                }
                saving = false
                outcome.fold(
                    onSuccess = { snackbar.showSnackbar("Texto guardado com sucesso.") },
                    onFailure = { snackbar.showSnackbar("Não foi possível guardar: ${it.message ?: "erro desconhecido"}") },
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LText("Resultado do texto") },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            LText("Texto pronto para rever", style = MaterialTheme.typography.headlineSmall)
            LText(
                "Confere o conteúdo antes de o copiar ou guardar num ficheiro TXT.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                LText(
                    result.text,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(result.text))
                        scope.launch { snackbar.showSnackbar("Texto copiado.") }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    LText("Copiar")
                }
                Button(
                    onClick = { savePicker.launch(result.outputName) },
                    enabled = !saving,
                    modifier = Modifier.weight(1f),
                ) {
                    if (saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Outlined.SaveAlt, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    LText(if (saving) "A guardar…" else "Guardar TXT")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                LText("Voltar às ferramentas")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}
