package com.nexauren.docpilot.pdf

import android.content.ContentResolver
import android.net.Uri
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDXObject
import kotlin.math.max
import kotlin.math.roundToInt

object PdfProcessor {
    fun merge(resolver: ContentResolver, inputUris: List<Uri>, outputUri: Uri): Long {
        require(inputUris.size >= 2) { "Selecione pelo menos dois PDFs." }
        PDDocument().use { merged ->
            inputUris.forEach { uri ->
                resolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Não foi possível abrir um dos PDFs." }
                    PDDocument.load(input).use { source ->
                        source.pages.forEach { page -> merged.importPage(page) }
                    }
                }
            }
            resolver.openOutputStream(outputUri).use { output ->
                requireNotNull(output) { "Não foi possível criar o PDF de saída." }
                merged.save(output)
            }
        }
        return fileSize(resolver, outputUri)
    }

    fun extractPages(resolver: ContentResolver, inputUri: Uri, outputUri: Uri, startPage: Int, endPage: Int): Long {
        require(startPage >= 1) { "A primeira página deve ser 1 ou maior." }
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { source ->
                require(endPage <= source.numberOfPages) { "O PDF tem " + source.numberOfPages + " páginas." }
                require(startPage <= endPage) { "A página inicial deve ser menor ou igual à final." }
                PDDocument().use { outputDocument ->
                    for (index in startPage - 1 until endPage) outputDocument.importPage(source.getPage(index))
                    resolver.openOutputStream(outputUri).use { output ->
                        requireNotNull(output) { "Não foi possível criar o PDF de saída." }
                        outputDocument.save(output)
                    }
                }
            }
        }
        return fileSize(resolver, outputUri)
    }

    fun reorder(resolver: ContentResolver, inputUri: Uri, outputUri: Uri, order: List<Int>): Long {
        require(order.isNotEmpty()) { "Indique pelo menos uma página." }
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { source ->
                validateOrder(order, source.numberOfPages)
                PDDocument().use { outputDocument ->
                    order.forEach { pageNumber -> outputDocument.importPage(source.getPage(pageNumber - 1)) }
                    resolver.openOutputStream(outputUri).use { output ->
                        requireNotNull(output) { "Não foi possível criar o PDF de saída." }
                        outputDocument.save(output)
                    }
                }
            }
        }
        return fileSize(resolver, outputUri)
    }

    fun compress(resolver: ContentResolver, inputUri: Uri, outputUri: Uri, quality: Float = 0.72f, maxDimension: Int = 1800): Long {
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                document.pages.forEach { page -> optimizeImages(document, page.resources, quality, maxDimension) }
                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF de saída." }
                    document.save(output)
                }
            }
        }
        return fileSize(resolver, outputUri)
    }

    private fun optimizeImages(document: PDDocument, resources: PDResources?, quality: Float, maxDimension: Int) {
        if (resources == null) return
        for (name in resources.xObjectNames) {
            val xObject: PDXObject = runCatching { resources.getXObject(name) }.getOrNull() ?: continue
            if (xObject !is PDImageXObject) continue
            val image = runCatching { xObject.image }.getOrNull() ?: continue
            val largestSide = max(image.width, image.height)
            if (largestSide <= maxDimension) continue
            val scale = maxDimension.toFloat() / largestSide.toFloat()
            val resizedWidth = (image.width * scale).roundToInt().coerceAtLeast(1)
            val resizedHeight = (image.height * scale).roundToInt().coerceAtLeast(1)
            val resized = android.graphics.Bitmap.createScaledBitmap(image, resizedWidth, resizedHeight, true)
            val replacement = runCatching { JPEGFactory.createFromImage(document, resized, quality) }.getOrNull()
            if (replacement != null) resources.put(name, replacement)
            if (resized !== image) resized.recycle()
        }
    }

    private fun validateOrder(order: List<Int>, pageCount: Int) {
        require(order.all { it in 1..pageCount }) { "A ordem contém uma página fora do intervalo 1.." + pageCount + "." }
    }

    private fun fileSize(resolver: ContentResolver, uri: Uri): Long = runCatching {
        resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
    }.getOrDefault(-1L)
}

fun parsePageOrder(text: String): List<Int> = text
    .split(",", " ", ";", "\n", "\t")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { it.toIntOrNull() ?: throw IllegalArgumentException("Use apenas números de página.") }

fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "tamanho desconhecido"
    if (bytes < 1024) return bytes.toString() + " B"
    if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0)
    return String.format("%.1f MB", bytes / (1024.0 * 1024.0))
}