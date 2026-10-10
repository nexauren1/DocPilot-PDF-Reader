package com.nexauren.docpilot.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(document: DocumentItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val pageListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var pageCount by remember(document.uri) { mutableStateOf(0) }
    var error by remember(document.uri) { mutableStateOf<String?>(null) }
    var resetZoomToken by remember(document.uri) { mutableStateOf(0) }
    var searchVisible by remember(document.uri) { mutableStateOf(false) }
    var searchQuery by remember(document.uri) { mutableStateOf("") }
    var searchResults by remember(document.uri) { mutableStateOf<List<Int>>(emptyList()) }
    var searchResultIndex by remember(document.uri) { mutableStateOf(0) }
    var searching by remember(document.uri) { mutableStateOf(false) }
    var searchMessage by remember(document.uri) { mutableStateOf<String?>(null) }
    var jumpDialog by remember(document.uri) { mutableStateOf(false) }
    var jumpPageText by remember(document.uri) { mutableStateOf("") }
    var isBookmarked by remember(document.uri) { mutableStateOf(false) }
    var readingMode by remember(document.uri) { mutableStateOf(false) }
    var readingText by remember(document.uri) { mutableStateOf("") }
    var readingModeLoading by remember(document.uri) { mutableStateOf(false) }
    var moreActionsExpanded by remember(document.uri) { mutableStateOf(false) }
    val bookmarkKey = remember(document.uri) { "bookmark_page_${document.uri.hashCode()}" }
    val positionKey = remember(document.uri) { "reader_page_${document.uri.hashCode()}" }

    val currentPage by remember {
        derivedStateOf {
            (pageListState.firstVisibleItemIndex + 1).coerceIn(1, maxOf(1, pageCount))
        }
    }

    fun runPdfSearch() {
        val query = searchQuery.trim()
        if (query.isBlank() || searching) return
        searching = true
        searchMessage = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { searchPdfPages(context, document.uri, query) }
            }
            searching = false
            result.onSuccess { pages ->
                searchResults = pages
                searchResultIndex = 0
                searchMessage = if (pages.isEmpty()) "Sem resultados. PDFs digitalizados podem precisar de OCR." else "${pages.size} página(s) com correspondências."
                if (pages.isNotEmpty()) pageListState.animateScrollToItem(pages.first() - 1)
            }.onFailure {
                searchResults = emptyList()
                searchMessage = "Não foi possível pesquisar este PDF. Confirma se o ficheiro continua acessível."
            }
        }
    }

    fun moveSearchResult(delta: Int) {
        if (searchResults.isEmpty()) return
        val next = (searchResultIndex + delta + searchResults.size) % searchResults.size
        searchResultIndex = next
        scope.launch { pageListState.animateScrollToItem(searchResults[next] - 1) }
    }

    LaunchedEffect(document.uri) {
        pageCount = 0
        error = null
        isBookmarked = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
            .getInt(bookmarkKey, -1) > 0
        runCatching {
            pageCount = withContext(Dispatchers.IO) { openPdfPageCount(context, document.uri) }
        }.onFailure {
            error = "Não foi possível abrir este PDF."
        }
    }

    LaunchedEffect(pageCount, document.uri) {
        if (pageCount > 0) {
            val preferences = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
            val initialPage = preferences.getInt(positionKey, 1).coerceIn(1, pageCount)
            pageListState.scrollToItem(initialPage - 1)
        }
    }

    LaunchedEffect(currentPage, pageCount, document.uri) {
        if (pageCount > 0) {
            val preferences = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
            preferences.edit().putInt(positionKey, currentPage).apply()
            isBookmarked = preferences.getInt(bookmarkKey, -1) == currentPage
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(document.name, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                        if (pageCount > 0) {
                            Text(
                                text = "${currentPage} / ${pageCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (readingMode) {
                            readingMode = false
                        } else if (readingText.isNotBlank()) {
                            readingMode = true
                        } else {
                            readingModeLoading = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    runCatching { extractPdfText(context, document.uri) }
                                }
                                readingModeLoading = false
                                result.onSuccess { extracted ->
                                    readingText = extracted
                                    readingMode = extracted.isNotBlank()
                                    if (extracted.isBlank()) {
                                        searchMessage = "Este PDF não contém texto extraível. Para documentos digitalizados, usa OCR."
                                        searchVisible = true
                                    }
                                }.onFailure {
                                    searchMessage = "Não foi possível extrair o texto deste PDF."
                                    searchVisible = true
                                }
                            }
                        }
                    }) {
                        if (readingModeLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Outlined.MenuBook,
                                contentDescription = if (readingMode) "Voltar à página PDF" else "Modo de leitura",
                                tint = if (readingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = {
                        searchVisible = !searchVisible
                        readingMode = false
                        if (searchVisible) {
                            searchMessage = null
                        } else {
                            searchResults = emptyList()
                            searchResultIndex = 0
                            searchMessage = null
                        }
                    }) {
                        Icon(Icons.Outlined.Search, contentDescription = if (searchVisible) "Fechar pesquisa" else "Pesquisar no PDF", tint = if (searchVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        val prefs = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
                        if (isBookmarked) {
                            prefs.edit().remove(bookmarkKey).apply()
                            isBookmarked = false
                        } else {
                            prefs.edit().putInt(bookmarkKey, currentPage).apply()
                            isBookmarked = true
                        }
                    }) {
                        Icon(
                            if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remover marcador" else "Guardar marcador",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box {
                        IconButton(onClick = { moreActionsExpanded = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "Mais ações do PDF")
                        }
                        DropdownMenu(
                            expanded = moreActionsExpanded,
                            onDismissRequest = { moreActionsExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ir para página") },
                                leadingIcon = { Icon(Icons.Outlined.FormatListNumbered, contentDescription = null) },
                                onClick = {
                                    jumpPageText = currentPage.toString()
                                    jumpDialog = true
                                    moreActionsExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Repor zoom") },
                                leadingIcon = { Icon(Icons.Outlined.ZoomOutMap, contentDescription = null) },
                                onClick = {
                                    resetZoomToken++
                                    moreActionsExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Partilhar PDF") },
                                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                                onClick = {
                                    moreActionsExpanded = false
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(document.uri))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    runCatching {
                                        context.startActivity(Intent.createChooser(shareIntent, "Partilhar PDF"))
                                    }
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            if (pageCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        enabled = currentPage > 1,
                        onClick = {
                            scope.launch {
                                pageListState.animateScrollToItem(currentPage - 2)
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Página anterior")
                    }

                    TextButton(onClick = {
                        jumpPageText = currentPage.toString()
                        jumpDialog = true
                    }) {
                        Text("Página ${currentPage} de ${pageCount}")
                    }

                    IconButton(
                        enabled = currentPage < pageCount,
                        onClick = {
                            scope.launch {
                                pageListState.animateScrollToItem(currentPage)
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Página seguinte")
                    }
                }
            }
        },
    ) { padding ->
        when {
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(error!!, modifier = Modifier.padding(28.dp))
                }
            }

            pageCount == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    LinearProgressIndicator(
                        progress = (currentPage.toFloat() / pageCount.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AnimatedVisibility(
                        visible = searchVisible,
                        enter = fadeIn(tween(160)) + expandVertically(tween(160)),
                        exit = fadeOut(tween(120)) + shrinkVertically(tween(120)),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it; searchMessage = null },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Pesquisar neste PDF") },
                                    placeholder = { Text("Palavra ou frase") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                IconButton(onClick = { runPdfSearch() }, enabled = searchQuery.isNotBlank() && !searching) {
                                    if (searching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    else Icon(Icons.Outlined.Search, contentDescription = "Executar pesquisa")
                                }
                                IconButton(onClick = { searchVisible = false; searchQuery = ""; searchResults = emptyList(); searchMessage = null }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Fechar pesquisa")
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    searchMessage ?: if (searchResults.isNotEmpty()) "Resultado ${searchResultIndex + 1} de ${searchResults.size} páginas" else "A pesquisa salta para as páginas que contêm o texto.",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (searchResults.isNotEmpty()) {
                                    IconButton(onClick = { moveSearchResult(-1) }) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "Resultado anterior") }
                                    IconButton(onClick = { moveSearchResult(1) }) { Icon(Icons.Outlined.ChevronRight, contentDescription = "Resultado seguinte") }
                                }
                            }
                        }
                    }
                    if (readingMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "MODO DE LEITURA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                document.name,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            SelectionContainer {
                                Text(
                                    readingText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.25f,
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    } else {
                        ThumbnailStrip(
                            context = context,
                            uri = document.uri,
                            pageCount = pageCount,
                            currentPage = currentPage,
                            onSelectPage = { page ->
                                scope.launch { pageListState.animateScrollToItem(page - 1) }
                            },
                        )

                        LazyColumn(
                            state = pageListState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(
                                items = (0 until pageCount).toList(),
                                key = { it },
                            ) { pageIndex ->
                                PdfPage(
                                    uri = document.uri,
                                    pageIndex = pageIndex,
                                    resetZoomToken = resetZoomToken,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (jumpDialog) {
        AlertDialog(
            onDismissRequest = { jumpDialog = false },
            title = { Text("Ir para página") },
            text = {
                OutlinedTextField(
                    value = jumpPageText,
                    onValueChange = { jumpPageText = it.filter(Char::isDigit) },
                    label = { Text("Número da página") },
                    supportingText = { Text("Este PDF tem ${pageCount} páginas.") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = jumpPageText.toIntOrNull()
                    if (target != null && target in 1..pageCount) {
                        scope.launch { pageListState.animateScrollToItem(target - 1) }
                        jumpDialog = false
                    }
                }) { Text("Ir") }
            },
            dismissButton = {
                TextButton(onClick = { jumpDialog = false }) { Text("Cancelar") }
            },
        )
    }


}

@Composable
private fun ThumbnailStrip(
    context: android.content.Context,
    uri: String,
    pageCount: Int,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = (0 until pageCount).toList(),
            key = { it },
        ) { pageIndex ->
            Card(
                onClick = { onSelectPage(pageIndex + 1) },
                shape = RoundedCornerShape(10.dp),
                border = if (pageIndex + 1 == currentPage) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    null
                },
            ) {
                PdfThumbnail(
                    context = context,
                    uri = uri,
                    pageIndex = pageIndex,
                )
            }
        }
    }
}

@Composable
private fun PdfThumbnail(
    context: android.content.Context,
    uri: String,
    pageIndex: Int,
) {
    var bitmap by remember(uri, pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri, pageIndex) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                renderPdfPage(context, uri, pageIndex, 180)
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(78.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Miniatura da página ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } ?: CircularProgressIndicator(
            strokeWidth = 1.5.dp,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun PdfPage(
    uri: String,
    pageIndex: Int,
    resetZoomToken: Int,
) {
    val context = LocalContext.current
    var bitmap by remember(uri, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var scale by remember(uri, pageIndex, resetZoomToken) { mutableStateOf(1f) }
    var offsetX by remember(uri, pageIndex, resetZoomToken) { mutableStateOf(0f) }
    var offsetY by remember(uri, pageIndex, resetZoomToken) { mutableStateOf(0f) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    LaunchedEffect(uri, pageIndex) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                renderPdfPage(context, uri, pageIndex, 1400)
            }.getOrNull()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            bitmap?.let { image ->
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "Página ${pageIndex + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .transformable(transformState),
                    contentScale = ContentScale.FillWidth,
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun openPdfPageCount(
    context: android.content.Context,
    uriString: String,
): Int {
    val uri = android.net.Uri.parse(uriString)
    context.contentResolver.openFileDescriptor(uri, "r").use { descriptor ->
        requireNotNull(descriptor) { "Não foi possível abrir o PDF." }
        PdfRenderer(descriptor).use { renderer ->
            return renderer.pageCount
        }
    }
}

private fun renderPdfPage(
    context: android.content.Context,
    uriString: String,
    pageIndex: Int,
    targetWidth: Int,
): Bitmap {
    val uri = android.net.Uri.parse(uriString)
    context.contentResolver.openFileDescriptor(uri, "r").use { descriptor ->
        requireNotNull(descriptor) { "Não foi possível abrir o PDF." }

        PdfRenderer(descriptor).use { renderer ->
            require(pageIndex in 0 until renderer.pageCount) {
                "Página fora do intervalo."
            }

            renderer.openPage(pageIndex).use { page ->
                val width = targetWidth.coerceAtLeast(240)
                val height = (width.toFloat() * page.height / page.width.toFloat())
                    .toInt()
                    .coerceAtLeast(1)

                val bitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888,
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                )
                return bitmap
            }
        }
    }
}
private fun searchPdfPages(
    context: android.content.Context,
    uriString: String,
    query: String,
): List<Int> {
    require(query.isNotBlank()) { "Escreve o texto que queres procurar." }
    val matches = mutableListOf<Int>()
    context.contentResolver.openInputStream(android.net.Uri.parse(uriString)).use { input ->
        requireNotNull(input) { "Não foi possível ler este PDF." }
        PDDocument.load(input).use { document ->
            val stripper = PDFTextStripper()
            for (page in 1..document.numberOfPages) {
                stripper.startPage = page
                stripper.endPage = page
                if (stripper.getText(document).contains(query, ignoreCase = true)) {
                    matches += page
                }
            }
        }
    }
    return matches
}

private fun extractPdfText(
    context: android.content.Context,
    uriString: String,
): String {
    context.contentResolver.openInputStream(android.net.Uri.parse(uriString)).use { input ->
        requireNotNull(input) { "Não foi possível ler este PDF." }
        PDDocument.load(input).use { document ->
            return PDFTextStripper().getText(document).trim()
        }
    }
}
