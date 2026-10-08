package com.nexauren.docpilot.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

    val currentPage by remember {
        derivedStateOf {
            (pageListState.firstVisibleItemIndex + 1).coerceIn(1, maxOf(1, pageCount))
        }
    }

    LaunchedEffect(document.uri) {
        pageCount = 0
        error = null
        runCatching {
            pageCount = withContext(Dispatchers.IO) { openPdfPageCount(context, document.uri) }
        }.onFailure {
            error = "Não foi possível abrir este PDF."
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

                    Text(
                        text = "Página ${currentPage} de ${pageCount}",
                        style = MaterialTheme.typography.labelLarge,
                    )

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
                        .background(Color(0xFFEDEDF3)),
                ) {
                    ThumbnailStrip(
                        context = context,
                        uri = document.uri,
                        pageCount = pageCount,
                        currentPage = currentPage,
                        onSelectPage = { page ->
                            scope.launch {
                                pageListState.animateScrollToItem(page - 1)
                            }
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