package com.nexauren.docpilot.pdf

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import java.io.IOException
import kotlin.math.min
import kotlin.math.max

object DocumentTools {

    fun imagesToPdf(
        resolver: ContentResolver,
        imageUris: List<Uri>,
        outputUri: Uri,
        pageWidth: Int = 595,
        pageHeight: Int = 842,
    ): Long {
        require(imageUris.isNotEmpty()) { "Selecione pelo menos uma imagem." }
        require(pageWidth > 0 && pageHeight > 0) { "O tamanho da página é inválido." }

        val pdf = PdfDocument()
        try {
            imageUris.forEachIndexed { index, uri ->
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Não foi possível abrir a imagem." }
                    BitmapFactory.decodeStream(input, null, bounds)
                }
                require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Imagem inválida." }

                // Downsample large camera images before decoding to reduce peak memory use.
                val maxDimensionPx = max(pageWidth, pageHeight) * 2
                var sampleSize = 1
                while (
                    bounds.outWidth / sampleSize > maxDimensionPx ||
                    bounds.outHeight / sampleSize > maxDimensionPx
                ) {
                    sampleSize *= 2
                }
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                }
                val bitmap = resolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Não foi possível reabrir a imagem." }
                    BitmapFactory.decodeStream(input, null, decodeOptions)
                } ?: throw IOException("Imagem inválida.")

                try {
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                    val page = pdf.startPage(pageInfo)
                    val scale = min(pageWidth.toFloat() / bitmap.width, pageHeight.toFloat() / bitmap.height)
                    val drawWidth = bitmap.width * scale
                    val drawHeight = bitmap.height * scale
                    val left = (pageWidth - drawWidth) / 2f
                    val top = (pageHeight - drawHeight) / 2f
                    page.canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), null)
                    pdf.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            resolver.openOutputStream(outputUri).use { output ->
                requireNotNull(output) { "Não foi possível criar o PDF de saída." }
                pdf.writeTo(output)
            }
        } finally {
            pdf.close()
        }

        return resolver.openAssetFileDescriptor(outputUri, "r")?.use { it.length } ?: -1L
    }

    fun exportPdfPageAsPng(
        resolver: ContentResolver,
        inputUri: Uri,
        pageNumber: Int,
        outputUri: Uri,
        maxDimension: Int = 2200,
    ): Long {
        require(pageNumber >= 1) { "O número da página deve ser 1 ou maior." }
        require(maxDimension in 720..3200) { "A resolução de exportação está fora do intervalo permitido." }

        resolver.openFileDescriptor(inputUri, "r").use { descriptor ->
            requireNotNull(descriptor) { "Não foi possível abrir o PDF." }
            PdfRenderer(descriptor).use { renderer ->
                require(pageNumber in 1..renderer.pageCount) {
                    "Este PDF tem ${renderer.pageCount} página(s)."
                }
                renderer.openPage(pageNumber - 1).use { page ->
                    val scale = min(
                        maxDimension.toFloat() / page.width.coerceAtLeast(1),
                        maxDimension.toFloat() / page.height.coerceAtLeast(1),
                    )
                    val width = (page.width * scale).toInt().coerceAtLeast(1)
                    val height = (page.height * scale).toInt().coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    try {
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        val transform = Matrix().apply {
                            setScale(width.toFloat() / page.width, height.toFloat() / page.height)
                        }
                        page.render(
                            bitmap,
                            null,
                            transform,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                        )
                        resolver.openOutputStream(outputUri, "wt").use { output ->
                            requireNotNull(output) { "Não foi possível criar a imagem PNG." }
                            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                                "Não foi possível codificar a página como PNG."
                            }
                        }
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
        }

        val size = runCatching {
            resolver.openAssetFileDescriptor(outputUri, "r")?.use { it.length } ?: -1L
        }.getOrDefault(-1L)
        require(size > 0L) { "A imagem exportada está vazia." }
        return size
    }

    fun protectPdf(
        resolver: ContentResolver,
        inputUri: Uri,
        outputUri: Uri,
        password: String,
    ): Long {
        require(password.length >= 4) { "Use uma palavra-passe com pelo menos 4 caracteres." }

        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                val permissions = AccessPermission()
                permissions.setCanModify(false)
                permissions.setCanExtractContent(false)
                permissions.setCanPrint(true)

                // Keep the open password separate from the owner password so the intended
                // permission restrictions are not bypassed with the same user-supplied secret.
                val ownerPassword = java.util.UUID.randomUUID().toString().replace("-", "")
                val policy = StandardProtectionPolicy(ownerPassword, password, permissions)
                policy.encryptionKeyLength = 128
                policy.permissions = permissions
                document.protect(policy)

                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF protegido." }
                    document.save(output)
                }
            }
        }

        return resolver.openAssetFileDescriptor(outputUri, "r")?.use { it.length } ?: -1L
    }

    fun addVisualSignature(
        resolver: ContentResolver,
        inputUri: Uri,
        outputUri: Uri,
        signatureText: String,
    ): Long {
        require(signatureText.isNotBlank()) { "Indique o nome da assinatura." }

        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                document.pages.forEach { page ->
                    PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                    ).use { stream ->
                        stream.beginText()
                        stream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
                        stream.setNonStrokingColor(80f / 255f, 80f / 255f, 150f / 255f)
                        stream.newLineAtOffset(36f, 28f)
                        val safeSignature = buildString {
                            signatureText.take(120).forEach { character ->
                                val value = character.toString()
                                append(
                                    if (runCatching { PDType1Font.HELVETICA_BOLD.encode(value) }.isSuccess) {
                                        value
                                    } else {
                                        "?"
                                    },
                                )
                            }
                        }
                        stream.showText(safeSignature)
                        stream.endText()
                    }
                }

                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o PDF assinado." }
                    document.save(output)
                }
            }
        }

        return resolver.openAssetFileDescriptor(outputUri, "r")?.use { it.length } ?: -1L
    }

    fun inspectPdf(
        resolver: ContentResolver,
        inputUri: Uri,
    ): String {
        val fileSize = runCatching {
            resolver.openAssetFileDescriptor(inputUri, "r")?.use { it.length } ?: -1L
        }.getOrDefault(-1L)

        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                val info = document.documentInformation
                val firstPageSize = if (document.numberOfPages > 0) document.getPage(0).mediaBox else null
                return buildString {
                    appendLine("Informações do PDF")
                    appendLine()
                    appendLine("Páginas: ${document.numberOfPages}")
                    appendLine("Tamanho do ficheiro: ${formatBytes(fileSize)}")
                    firstPageSize?.let {
                        appendLine("Primeira página: ${it.width.toInt()} × ${it.height.toInt()} pt")
                    }
                    appendLine("Encriptação: ${if (document.isEncrypted) "detetada" else "não detetada"}")
                    appendLine()
                    appendLine("Metadados")
                    appendLine("Título: ${info.title?.takeIf { it.isNotBlank() } ?: "Não indicado"}")
                    appendLine("Autor: ${info.author?.takeIf { it.isNotBlank() } ?: "Não indicado"}")
                    appendLine("Assunto: ${info.subject?.takeIf { it.isNotBlank() } ?: "Não indicado"}")
                    appendLine("Criador: ${info.creator?.takeIf { it.isNotBlank() } ?: "Não indicado"}")
                    appendLine("Aplicação de origem: ${info.producer?.takeIf { it.isNotBlank() } ?: "Não indicado"}")
                }
            }
        }
    }

    fun extractPdfText(
        resolver: ContentResolver,
        inputUri: Uri,
    ): String {
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                val extractedText = PDFTextStripper().getText(document).trim()
                require(extractedText.isNotBlank()) {
                    "Este PDF não contém texto extraível. Se for um documento digitalizado, usa OCR."
                }
                return extractedText
            }
        }
    }

    fun extractPdfTextToTextFile(
        resolver: ContentResolver,
        inputUri: Uri,
        outputUri: Uri,
    ): Long {
        resolver.openInputStream(inputUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF." }
            PDDocument.load(input).use { document ->
                val extractedText = PDFTextStripper().getText(document).trim()
                require(extractedText.isNotBlank()) {
                    "Este PDF não contém texto extraível. Se for um documento digitalizado, usa OCR."
                }
                resolver.openOutputStream(outputUri).use { output ->
                    requireNotNull(output) { "Não foi possível criar o ficheiro TXT." }
                    output.write(extractedText.toByteArray(Charsets.UTF_8))
                }
            }
        }
        return resolver.openAssetFileDescriptor(outputUri, "r")?.use { it.length } ?: -1L
    }

    fun scanResultPdf(
        resolver: ContentResolver,
        sourcePdfUri: Uri,
        outputUri: Uri,
    ): Long {
        resolver.openInputStream(sourcePdfUri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o PDF digitalizado." }
            resolver.openOutputStream(outputUri).use { output ->
                requireNotNull(output) { "Não foi possível criar o PDF de saída." }
                input.copyTo(output)
            }
        }
        return resolver.openAssetFileDescriptor(outputUri, "r")?.use { it.length } ?: -1L
    }
}
