package com.nexauren.docpilot.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class SearchHighlight(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(document: DocumentItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val pageListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var pageCount by remember(document.uri) { mutableStateOf(0) }
    var pageAspectRatios by remember(document.uri) { mutableStateOf<List<Float>>(emptyList()) }
    var positionRestored by remember(document.uri) { mutableStateOf(false) }
    var error by remember(document.uri) { mutableStateOf<String?>(null) }
    var resetZoomToken by remember(document.uri) { mutableStateOf(0) }
    var searchOpen by remember(document.uri) { mutableStateOf(false) }
    var searchQuery by remember(document.uri) { mutableStateOf("") }
    var searchResults by remember(document.uri) { mutableStateOf<List<Int>>(emptyList()) }
    var searchHighlights by remember(document.uri) { mutableStateOf<Map<Int, List<SearchHighlight>>>(emptyMap()) }
    var searchResultIndex by remember(document.uri) { mutableStateOf(0) }
    var searching by remember(document.uri) { mutableStateOf(false) }
    var searchMessage by remember(document.uri) { mutableStateOf<String?>(null) }
    var jumpDialog by remember(document.uri) { mutableStateOf(false) }
    var jumpPageText by remember(document.uri) { mutableStateOf("") }
    var isBookmarked by remember(document.uri) { mutableStateOf(false) }
    var readingMode by remember(document.uri) { mutableStateOf(false) }
    var readingText by remember(document.uri) { mutableStateOf("") }
    var readingModeLoading by remember(document.uri) { mutableStateOf(false) }
    val bookmarkKey = remember(document.uri) { "bookmark_page_${document.uri.hashCode()}" }
    val positionKey = remember(document.uri) { "reader_page_${document.uri.hashCode()}" }

    val currentPage by remember {
        derivedStateOf {
            (pageListState.firstVisibleItemIndex + 1).coerceIn(1, maxOf(1, pageCount))
        }
    }

    LaunchedEffect(document.uri) {
        pageCount = 0
        pageAspectRatios = emptyList()
        positionRestored = false
        error = null
        pageListState.scrollToItem(0)
        isBookmarked = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
            .getInt(bookmarkKey, -1) > 0
        runCatching {
            val ratios = withContext(Dispatchers.IO) {
                openPdfPageAspectRatios(context, document.uri)
            }
            pageAspectRatios = ratios
            pageCount = ratios.size
        }.onFailure {
            error = "Não foi possível abrir este PDF."
        }
    }

    LaunchedEffect(pageCount, document.uri) {
        if (pageCount > 0) {
            positionRestored = false
            val preferences = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
            val initialPage = preferences.getInt(positionKey, 1).coerceIn(1, pageCount)
            pageListState.scrollToItem(initialPage - 1)
            positionRestored = true
        }
    }

    LaunchedEffect(currentPage, pageCount, document.uri, positionRestored) {
        if (pageCount > 0 && positionRestored) {
            val preferences = context.getSharedPreferences("docpilot_reader", android.content.Context.MODE_PRIVATE)
            preferences.edit().putInt(positionKey, currentPage).apply()
            isBookmarked = preferences.getInt(bookmarkKey, -1) == currentPage
        }
    }

    LaunchedEffect(searchQuery, document.uri, pageCount, searchOpen) {
        val query = searchQuery.trim()
        searching = false
        if (query.isBlank() || pageCount == 0 || !searchOpen) {
            searchResults = emptyList()
            searchHighlights = emptyMap()
            searchResultIndex = 0
            return@LaunchedEffect
        }

        searchResults = emptyList()
        searchHighlights = emptyMap()
        delay(350)
        searching = true
        val result = withContext(Dispatchers.IO) {
            runCatching { searchPdfMatches(context, document.uri, query) }
        }
        searching = false
        result.onSuccess { matches ->
            searchHighlights = matches
            val pages = matches.keys.sorted()
            searchResults = pages
            searchResultIndex = 0
            val totalMatches = matches.values.sumOf { it.size }
            searchMessage = if (pages.isEmpty()) {
                "Não foram encontradas correspondências. PDFs digitalizados podem precisar de OCR."
            } else {
                "$totalMatches correspondência(s) destacada(s) em ${pages.size} página(s)."
            }
            if (pages.isNotEmpty()) pageListState.animateScrollToItem(pages.first() - 1)
        }.onFailure {
            searchMessage = "Não foi possível pesquisar este PDF."
            searchResults = emptyList()
            searchHighlights = emptyMap()
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
                                        searchOpen = true
                                    }
                                }.onFailure {
                                    searchMessage = "Não foi possível extrair o texto deste PDF."
                                    searchOpen = true
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
                        searchOpen = !searchOpen
                        if (searchOpen) {
                            readingMode = false
                        } else {
                            searchQuery = ""
                            searchResults = emptyList()
                            searchMessage = null
                        }
                    }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Pesquisar no PDF")
                    }
                    IconButton(onClick = { jumpPageText = currentPage.toString(); jumpDialog = true }) {
                        Icon(Icons.Outlined.FormatListNumbered, contentDescription = "Ir para página")
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
                    IconButton(onClick = { resetZoomToken++ }) {
                        Icon(Icons.Outlined.ZoomOutMap, contentDescription = "Repor zoom")
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
                    AnimatedVisibility(visible = searchOpen, enter = fadeIn(), exit = fadeOut()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it; searchMessage = null },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        label = { Text("Pesquisar neste PDF") },
                                        placeholder = { Text("Escreve uma palavra ou frase") },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = ""; searchMessage = null }) {
                                                    Icon(Icons.Outlined.Close, contentDescription = "Limpar pesquisa")
                                                }
                                            }
                                        },
                                    )
                                    IconButton(onClick = {
                                        searchOpen = false
                                        searchQuery = ""
                                        searchResults = emptyList()
                                        searchMessage = null
                                    }) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Fechar pesquisa")
                                    }
                                }
                                if (searchQuery.isNotBlank()) {
                                    Text(
                                        "Termo pesquisado: “${searchQuery.trim()}”",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (searching) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Text("A procurar enquanto escreves…", style = MaterialTheme.typography.bodySmall)
                                    } else if (searchResults.isNotEmpty()) {
                                        Text(
                                            "Página ${searchResults[searchResultIndex]} • ${searchResultIndex + 1}/${searchResults.size}",
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        IconButton(onClick = {
                                            searchResultIndex = (searchResultIndex - 1 + searchResults.size) % searchResults.size
                                            scope.launch { pageListState.animateScrollToItem(searchResults[searchResultIndex] - 1) }
                                        }) {
                                            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Resultado anterior")
                                        }
                                        IconButton(onClick = {
                                            searchResultIndex = (searchResultIndex + 1) % searchResults.size
                                            scope.launch { pageListState.animateScrollToItem(searchResults[searchResultIndex] - 1) }
                                        }) {
                                            Icon(Icons.Outlined.ChevronRight, contentDescription = "Resultado seguinte")
                                        }
                                    } else {
                                        Text(
                                            "Os resultados aparecem automaticamente.",
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                searchMessage?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    pageAspectRatio = pageAspectRatios.getOrNull(pageIndex) ?: (595f / 842f),
                                    highlights = searchHighlights[pageIndex + 1].orEmpty(),
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
    pageAspectRatio: Float,
    highlights: List<SearchHighlight>,
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
        modifier = Modifier.fillMaxWidth().aspectRatio(pageAspectRatio.coerceIn(0.25f, 3f)),
        shape = RoundedCornerShape(10.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            bitmap?.let { image ->
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "Página ${pageIndex + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .transformable(transformState),
                    contentScale = ContentScale.FillBounds,
                )
                if (highlights.isNotEmpty()) {
                    Canvas(
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    ) {
                        highlights.forEach { match ->
                            val left = match.left.coerceIn(0f, 1f) * size.width
                            val top = match.top.coerceIn(0f, 1f) * size.height
                            val right = match.right.coerceIn(0f, 1f) * size.width
                            val bottom = match.bottom.coerceIn(0f, 1f) * size.height
                            if (right > left && bottom > top) {
                                drawRect(
                                    color = Color(0xFFFFD54F).copy(alpha = 0.42f),
                                    topLeft = Offset(left, top),
                                    size = Size(right - left, bottom - top),
                                )
                                drawLine(
                                    color = Color(0xFF9B6500),
                                    start = Offset(left, bottom),
                                    end = Offset(right, bottom),
                                    strokeWidth = 2.dp.toPx(),
                                )
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun openPdfPageAspectRatios(
    context: android.content.Context,
    uriString: String,
): List<Float> {
    val uri = android.net.Uri.parse(uriString)
    context.contentResolver.openFileDescriptor(uri, "r").use { descriptor ->
        requireNotNull(descriptor) { "Não foi possível abrir o PDF." }
        PdfRenderer(descriptor).use { renderer ->
            return (0 until renderer.pageCount).map { pageIndex ->
                renderer.openPage(pageIndex).use { page ->
                    page.width.toFloat() / page.height.coerceAtLeast(1).toFloat()
                }
            }
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
private fun searchPdfMatches(
    context: android.content.Context,
    uriString: String,
    query: String,
): Map<Int, List<SearchHighlight>> {
    require(query.isNotBlank()) { "Escreve o texto que queres procurar." }
    val terms = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val pagesWithMatches = linkedMapOf<Int, List<SearchHighlight>>()
    context.contentResolver.openInputStream(android.net.Uri.parse(uriString)).use { input ->
        requireNotNull(input) { "Não foi possível ler este PDF." }
        PDDocument.load(input).use { document ->
            for (pageNumber in 1..document.numberOfPages) {
                val page = document.getPage(pageNumber - 1)
                val pageWidth = page.cropBox.width.coerceAtLeast(1f)
                val pageHeight = page.cropBox.height.coerceAtLeast(1f)
                val stripper = object : PDFTextStripper() {
                    val highlights = mutableListOf<SearchHighlight>()

                    override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
                        super.writeString(text, textPositions)
                        if (textPositions.isEmpty()) return

                        val glyphText = StringBuilder()
                        val glyphPositions = mutableListOf<TextPosition>()
                        textPositions.forEach { position ->
                            position.unicode.orEmpty().forEach { character ->
                                glyphText.append(character)
                                glyphPositions += position
                            }
                        }
                        val line = glyphText.toString()
                        terms.forEach { term ->
                            var start = 0
                            while (start < line.length) {
                                val foundAt = line.indexOf(term, start, ignoreCase = true)
                                if (foundAt < 0) break
                                val end = (foundAt + term.length).coerceAtMost(glyphPositions.size)
                                if (foundAt < end) {
                                    val foundPositions = glyphPositions.subList(foundAt, end)
                                    val left = foundPositions.minOf { it.xDirAdj }
                                    val right = foundPositions.maxOf { it.xDirAdj + it.widthDirAdj }
                                    val top = foundPositions.minOf { it.yDirAdj - it.heightDir }
                                    val bottom = foundPositions.maxOf { it.yDirAdj }
                                    if (right > left && bottom > top) {
                                        highlights += SearchHighlight(
                                            left = (left / pageWidth).coerceIn(0f, 1f),
                                            top = (top / pageHeight).coerceIn(0f, 1f),
                                            right = (right / pageWidth).coerceIn(0f, 1f),
                                            bottom = (bottom / pageHeight).coerceIn(0f, 1f),
                                        )
                                    }
                                }
                                start = foundAt + term.length.coerceAtLeast(1)
                            }
                        }
                    }
                }
                stripper.startPage = pageNumber
                stripper.endPage = pageNumber
                val pageText = stripper.getText(document)
                if (pageText.contains(query, ignoreCase = true)) {
                    pagesWithMatches[pageNumber] = stripper.highlights.distinct()
                }
            }
        }
    }
    return pagesWithMatches
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
