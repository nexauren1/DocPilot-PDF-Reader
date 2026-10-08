package com.nexauren.docpilot.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import com.nexauren.docpilot.model.DocumentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    document: DocumentItem,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var pageBitmap by remember(document.uri) { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember(document.uri) { mutableStateOf<Int?>(null) }
    var error by remember(document.uri) { mutableStateOf<String?>(null) }

    LaunchedEffect(document.uri) {
        pageBitmap = null
        pageCount = null
        error = null

        runCatching {
            val uri = android.net.Uri.parse(document.uri)
            val descriptor: ParcelFileDescriptor =
                requireNotNull(context.contentResolver.openFileDescriptor(uri, "r"))
            descriptor.use { parcelFileDescriptor ->
                PdfRenderer(parcelFileDescriptor).use { renderer ->
                    pageCount = renderer.pageCount
                    if (renderer.pageCount > 0) {
                        renderer.openPage(0).use { page ->
                            val targetWidth = 1200
                            val targetHeight =
                                (targetWidth.toFloat() * page.height / page.width.toFloat()).toInt()
                            val bitmap = Bitmap.createBitmap(
                                targetWidth,
                                targetHeight.coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888,
                            )
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                            )
                            pageBitmap = bitmap
                        }
                    }
                }
            }
        }.onFailure {
            error = "Não foi possível abrir este PDF."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
                actions = {
                    pageCount?.let { Text("1 / $it", modifier = Modifier.padding(end = 14.dp)) }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFEDEDF3)),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                error != null -> {
                    Text(error!!, modifier = Modifier.padding(32.dp))
                }
                pageBitmap == null -> {
                    CircularProgressIndicator(modifier = Modifier.padding(40.dp))
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Image(
                            bitmap = pageBitmap!!.asImageBitmap(),
                            contentDescription = "Página 1",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
