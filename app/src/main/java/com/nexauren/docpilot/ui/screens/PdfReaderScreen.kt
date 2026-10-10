package com.nexauren.docpilot.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.LruCache
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val READER_PREFS = "docpilot_reader"
private val pageBitmapCache = object : LruCache<String, Bitmap>(24 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
}

private data class PdfSearchMatch(val page: Int, val excerpt: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(document: DocumentItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val pageListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var pageCount by remember(document.uri) { mutableStateOf(0) }
    var error by remember(document.uri) { mutableStateOf<String?>(null) }
    var resetZoomToken by remember(document.uri) { mutableStateOf(0) }
    var showPageDialog by remember(document.uri) { mutableStateOf(false) }
    var pageInput by remember(document.uri) { mutableStateOf("1") }
    var pageError by remember(document.uri) { mutableStateOf<String?>(null) }
    var showSearchDialog by remember(document.uri) { mutableStateOf(false) }
    var searchQuery by remember(document.uri) { mutableStateOf("") }
    var searching by remember(document.uri) { mutableStateOf(false) }
    var searchResults by remember(document.uri) { mutableStateOf<List<PdfSearchMatch>>(emptyList()) }
    var searchMessage by remember(document.uri) { mutableStateOf<String?>(null) }
    var showBookmarksDialog by remember(document.uri) { mutableStateOf(false) }
    var bookmarkedPages by remember(document.uri) { mutableStateOf(loadBookmarkPages(context, document.uri)) }

    val currentPage by remember(pageListState, pageCount) {
        derivedStateOf { (pageListState.firstVisibleItemIndex + 1).coerceIn(1, maxOf(1, pageCount)) }
    }

    fun goToPage(page: Int) {
        if (pageCount > 0) {
            scope.launch { pageListState.animateScrollToItem((page - 1).coerceIn(0, pageCount - 1)) }
        }
    }

    fun toggleCurrentBookmark() {
        val next = if (currentPage in bookmarkedPages) bookmarkedPages - currentPage else bookmarkedPages + currentPage
        bookmarkedPages = next
        context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(bookmarkKey(document.uri), next.map { it.toString() }.toSet())
            .apply()
    }

    fun searchCurrentPdf() {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            searchMessage = "Escreve uma palavra ou frase para pesquisar."
            searchResults = emptyList()
            return
        }
        searching = true
        searchMessage = null
        searchResults = emptyList()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { findPdfTextMatches(context, document.uri, query) }
            }
            searching = false
            result.onSuccess { matches ->
                searchResults = matches
                searchMessage = when {
                    matches.isNotEmpty() -> "${matches.size} páginas com resultados"
                    else -> "Não encontrámos esse texto. PDFs digitalizados podem precisar de OCR."
                }
            }.onFailure {
                searchMessage = "Não foi possível pesquisar este PDF. Se estiver digitalizado, experimenta o OCR."
            }
        }
    }

    BackHandler { onBack() }

    LaunchedEffect(document.uri) {
        pageCount = 0
        error = null
        runCatching {
            pageCount = withContext(Dispatchers.IO) { openPdfPageCount(context, document.uri) }
        }.onFailure {
            error = "Não foi possível abrir este PDF. O ficheiro pode ter sido movido, removido ou estar protegido por palavra-passe."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(document.name, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (pageCount > 0) "PDF • Página $currentPage de $pageCount" else "A preparar o documento…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Pesquisar texto no PDF")
                    }
                    IconButton(onClick = { toggleCurrentBookmark() }, enabled = pageCount > 0) {
                        Icon(
                            if (currentPage in bookmarkedPages) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (currentPage in bookmarkedPages) "Remover marcador" else "Marcar esta página",
                            tint = if (currentPage in bookmarkedPages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = { showBookmarksDialog = true }) {
                        Icon(Icons.Outlined.Bookmarks, contentDescription = "Ver marcadores")
                    }
                    IconButton(
                        onClick = {
                            runCatching {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, Uri.parse(document.uri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(send, "Partilhar PDF"))
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Partilhar PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            if (pageCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(enabled = currentPage > 1, onClick = { goToPage(currentPage - 1) }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Página anterior")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            pageInput = currentPage.toString()
                            pageError = null
                            showPageDialog = true
                        }.padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text("Página $currentPage / $pageCount", style = MaterialTheme.typography.labelLarge)
                        Text("Toca para ir a uma página", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(enabled = currentPage < pageCount, onClick = { goToPage(currentPage + 1) }) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Página seguinte")
                    }
                    IconButton(onClick = { resetZoomToken++ }) {
                        Icon(Icons.Outlined.ZoomOutMap, contentDescription = "Repor zoom")
                    }
                }
            }
        },
    ) { padding ->
        when {
            error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = onBack) { Text("Voltar à biblioteca") }
                    }
                }
            }
            pageCount == 0 -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator()
                        Text("A preparar o teu PDF…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    ThumbnailStrip(
                        context = context,
                        uri = document.uri,
                        pageCount = pageCount,
                        currentPage = currentPage,
                        onSelectPage = ::goToPage,
                    )
                    LazyColumn(
                        state = pageListState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(items = (0 until pageCount).toList(), key = { it }) { pageIndex ->
                            PdfPage(uri = document.uri, pageIndex = pageIndex, resetZoomToken = resetZoomToken)
                        }
                    }
                }
            }
        }
    }

    if (showPageDialog) {
        AlertDialog(
            onDismissRequest = { showPageDialog = false },
            title = { Text("Ir para a página") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Este documento tem $pageCount páginas.")
                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = { pageInput = it.filter(Char::isDigit).take(6); pageError = null },
                        label = { Text("Número da página") },
                        singleLine = true,
                    )
                    pageError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = pageInput.toIntOrNull()
                    if (target == null || target !in 1..pageCount) pageError = "Indica um número entre 1 e $pageCount."
                    else {
                        showPageDialog = false
                        goToPage(target)
                    }
                }) { Text("Ir") }
            },
            dismissButton = { TextButton(onClick = { showPageDialog = false }) { Text("Cancelar") } },
        )
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Pesquisar no PDF") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; searchMessage = null },
                        label = { Text("Palavra ou frase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    )
                    if (searching) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("A pesquisar no documento…", style = MaterialTheme.typography.bodySmall)
                    }
                    searchMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (searchResults.isNotEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            searchResults.forEach { match ->
                                Column(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        showSearchDialog = false
                                        goToPage(match.page)
                                    }.padding(vertical = 5.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text("Página ${match.page}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    Text(match.excerpt, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { searchCurrentPdf() }, enabled = !searching) { Text("Pesquisar") }
            },
            dismissButton = { TextButton(onClick = { showSearchDialog = false }) { Text("Fechar") } },
        )
    }

    if (showBookmarksDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarksDialog = false },
            title = { Text("Páginas marcadas") },
            text = {
                if (bookmarkedPages.isEmpty()) {
                    Text("Ainda não guardaste marcadores neste documento.")
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        bookmarkedPages.sorted().forEach { page ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    showBookmarksDialog = false
                                    goToPage(page)
                                }.padding(vertical = 9.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.Bookmark, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Text("Página $page", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                Text("Abrir", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showBookmarksDialog = false }) { Text("Fechar") } },
        )
    }
}

@Composable
private fun ThumbnailStrip(
    context: Context,
    uri: String,
    pageCount: Int,
    currentPage: Int,
    onSelectPage: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        items(items = (0 until pageCount).toList(), key = { it }) { pageIndex ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Card(
                    onClick = { onSelectPage(pageIndex + 1) },
                    shape = RoundedCornerShape(10.dp),
                    border = if (pageIndex + 1 == currentPage) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    PdfThumbnail(context = context, uri = uri, pageIndex = pageIndex)
                }
                Text(
                    text = (pageIndex + 1).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (pageIndex + 1 == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PdfThumbnail(context: Context, uri: String, pageIndex: Int) {
    var bitmap by remember(uri, pageIndex) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uri, pageIndex) {
        bitmap = withContext(Dispatchers.IO) { runCatching { renderPdfPage(context, uri, pageIndex, 180) }.getOrNull() }
    }
    Box(
        modifier = Modifier.width(58.dp).height(78.dp).background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { image ->
            Image(image.asImageBitmap(), contentDescription = "Miniatura da página ${pageIndex + 1}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } ?: CircularProgressIndicator(strokeWidth = 1.5.dp, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PdfPage(uri: String, pageIndex: Int, resetZoomToken: Int) {
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
            runCatching { renderPdfPage(context, uri, pageIndex, 1400) }.getOrNull()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            bitmap?.let { image ->
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "Página ${pageIndex + 1}",
                    modifier = Modifier.fillMaxWidth().graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }.transformable(transformState),
                    contentScale = ContentScale.FillWidth,
                )
            } ?: Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun bookmarkKey(uri: String): String = "bookmarks_${uri.hashCode()}"

private fun loadBookmarkPages(context: Context, uri: String): Set<Int> =
    context.getSharedPreferences(READER_PREFS, Context.MODE_PRIVATE)
        .getStringSet(bookmarkKey(uri), emptySet())
        .orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .toSet()

private fun findPdfTextMatches(context: Context, uriString: String, query: String): List<PdfSearchMatch> {
    val matches = mutableListOf<PdfSearchMatch>()
    context.contentResolver.openInputStream(Uri.parse(uriString)).use { input ->
        requireNotNull(input) { "Não foi possível abrir o PDF." }
        PDDocument.load(input).use { document ->
            val stripper = PDFTextStripper()
            for (pageNumber in 1..document.numberOfPages) {
                stripper.startPage = pageNumber
                stripper.endPage = pageNumber
                val pageText = stripper.getText(document)
                val index = pageText.indexOf(query, ignoreCase = true)
                if (index >= 0) {
                    val start = (index - 62).coerceAtLeast(0)
                    val end = (index + query.length + 90).coerceAtMost(pageText.length)
                    val excerpt = pageText.substring(start, end).replace('\n', ' ').replace('\r', ' ').trim()
                    matches.add(PdfSearchMatch(pageNumber, if (start > 0) "…$excerpt" else excerpt))
                    if (matches.size >= 80) break
                }
            }
        }
    }
    return matches
}

private fun openPdfPageCount(context: Context, uriString: String): Int {
    val uri = Uri.parse(uriString)
    context.contentResolver.openFileDescriptor(uri, "r").use { descriptor ->
        requireNotNull(descriptor) { "Não foi possível abrir o PDF." }
        PdfRenderer(descriptor).use { renderer -> return renderer.pageCount }
    }
}

private fun renderPdfPage(context: Context, uriString: String, pageIndex: Int, targetWidth: Int): Bitmap {
    val cacheKey = "$uriString|$pageIndex|$targetWidth"
    pageBitmapCache.get(cacheKey)?.let { return it }
    val uri = Uri.parse(uriString)
    context.contentResolver.openFileDescriptor(uri, "r").use { descriptor ->
        requireNotNull(descriptor) { "Não foi possível abrir o PDF." }
        PdfRenderer(descriptor).use { renderer ->
            require(pageIndex in 0 until renderer.pageCount) { "Página fora do intervalo." }
            renderer.openPage(pageIndex).use { page ->
                val width = targetWidth.coerceAtLeast(240)
                val height = (width.toFloat() * page.height / page.width.toFloat()).toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pageBitmapCache.put(cacheKey, bitmap)
                return bitmap
            }
        }
    }
}
