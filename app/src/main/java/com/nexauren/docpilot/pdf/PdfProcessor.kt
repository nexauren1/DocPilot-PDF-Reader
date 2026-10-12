package com.nexauren.docpilot.pdf

import android.content.ContentResolver
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject
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
                validatePageOrder(order, source.numberOfPages)
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

    fun rotateAllPages(resolver: ContentResolver, inputUri: Uri, outputUri: Uri): Long {
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                document.pages.forEach { page ->
                    page.rotation = (page.rotation + 90) % 360
                }
                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF rodado." }
                    document.save(output)
                }
            }
        }
        return fileSize(resolver, outputUri)
    }

    fun rotatePage(
        resolver: ContentResolver,
        inputUri: Uri,
        outputUri: Uri,
        pageNumber: Int,
        degrees: Int,
    ): Long {
        requireValidRotationDegrees(degrees)
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                requireValidPageNumber(pageNumber, document.numberOfPages)
                val page = document.getPage(pageNumber - 1)
                page.rotation = (page.rotation + degrees) % 360
                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF rodado." }
                    document.save(output)
                }
            }
        }
        return fileSize(resolver, outputUri)
    }

    fun cropPage(
        resolver: ContentResolver,
        inputUri: Uri,
        outputUri: Uri,
        pageNumber: Int,
        marginRatio: Float,
    ): Long {
        require(marginRatio in 0f..0.2f) { "Escolhe um recorte entre 0% e 20% por margem." }
        require(marginRatio > 0f) { "Escolhe uma margem de recorte maior que 0%." }
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                requireValidPageNumber(pageNumber, document.numberOfPages)
                val page = document.getPage(pageNumber - 1)
                val original = page.cropBox
                val marginX = original.width * marginRatio
                val marginY = original.height * marginRatio
                val croppedWidth = original.width - 2f * marginX
                val croppedHeight = original.height - 2f * marginY
                require(croppedWidth > 0f && croppedHeight > 0f) {
                    "O recorte é demasiado grande para esta página."
                }
                page.cropBox = PDRectangle(
                    original.lowerLeftX + marginX,
                    original.lowerLeftY + marginY,
                    croppedWidth,
                    croppedHeight,
                )
                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF recortado." }
                    document.save(output)
                }
            }
        }
        return fileSize(resolver, outputUri)
    }

    fun addTextWatermark(
        resolver: ContentResolver,
        inputUri: Uri,
        outputUri: Uri,
        watermarkText: String,
    ): Long {
        val text = normalizeWatermarkText(watermarkText)
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                require(document.numberOfPages > 0) { "Este PDF não contém páginas." }
                val font = PDType1Font.HELVETICA_BOLD
                document.pages.forEach { page ->
                    val box = page.mediaBox
                    val maximumWidth = box.width * 0.84f
                    val defaultSize = 34f
                    val defaultTextWidth = font.getStringWidth(text) / 1000f * defaultSize
                    val fontSize = if (defaultTextWidth > maximumWidth) {
                        (defaultSize * maximumWidth / defaultTextWidth).coerceAtLeast(8f)
                    } else {
                        defaultSize
                    }
                    val textWidth = font.getStringWidth(text) / 1000f * fontSize
                    val x = box.lowerLeftX + (box.width - textWidth) / 2f
                    val y = box.lowerLeftY + box.height / 2f
                    PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true,
                    ).use { stream ->
                        stream.setNonStrokingColor(185, 185, 185)
                        stream.beginText()
                        stream.setFont(font, fontSize)
                        stream.newLineAtOffset(x, y)
                        stream.showText(text)
                        stream.endText()
                    }
                }
                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF com marca de água." }
                    document.save(output)
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
            // JPEG cannot preserve alpha. Leave transparent artwork untouched rather than
            // flattening logos and icons onto an unintended background.
            if (image.hasAlpha() || largestSide <= maxDimension) continue
            val scale = maxDimension.toFloat() / largestSide.toFloat()
            val resizedWidth = (image.width * scale).roundToInt().coerceAtLeast(1)
            val resizedHeight = (image.height * scale).roundToInt().coerceAtLeast(1)
            val resized = android.graphics.Bitmap.createScaledBitmap(image, resizedWidth, resizedHeight, true)
            val replacement = runCatching { JPEGFactory.createFromImage(document, resized, quality) }.getOrNull()
            if (replacement != null) resources.put(name, replacement)
            if (resized !== image) resized.recycle()
        }
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
internal fun validatePageOrder(order: List<Int>, pageCount: Int) {
    require(pageCount > 0) { "Este PDF não contém páginas." }
    require(order.size == pageCount) {
        "A nova ordem deve incluir todas as $pageCount páginas. Para extrair páginas, usa Dividir PDF."
    }
    require(order.all { it in 1..pageCount }) {
        "A ordem contém uma página fora do intervalo 1..$pageCount."
    }
    require(order.distinct().size == pageCount) {
        "Cada página deve aparecer uma única vez na nova ordem."
    }
}

internal fun normalizeWatermarkText(text: String): String {
    val normalized = text.trim().replace(Regex("\\s+"), " ")
    require(normalized.isNotEmpty()) { "Escreve o texto da marca de água." }
    require(normalized.length <= 80) { "A marca de água pode ter até 80 caracteres." }
    return normalized
}

internal fun requireValidPageNumber(pageNumber: Int, pageCount: Int) {
    require(pageCount > 0) { "Este PDF não contém páginas." }
    require(pageNumber in 1..pageCount) { "A página deve estar entre 1 e $pageCount." }
}

internal fun requireValidRotationDegrees(degrees: Int) {
    require(degrees in setOf(90, 180, 270)) {
        "Escolhe uma rotação de 90°, 180° ou 270°."
    }
}

