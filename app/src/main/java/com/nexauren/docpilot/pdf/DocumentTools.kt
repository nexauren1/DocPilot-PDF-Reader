package com.nexauren.docpilot.pdf

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
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

object DocumentTools {

    fun imagesToPdf(
        resolver: ContentResolver,
        imageUris: List<Uri>,
        outputUri: Uri,
    ): Long {
        require(imageUris.isNotEmpty()) { "Selecione pelo menos uma imagem." }

        val pdf = PdfDocument()
        try {
            imageUris.forEach { uri ->
                val bitmap = resolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Não foi possível abrir a imagem." }
                    BitmapFactory.decodeStream(input)
                } ?: throw IOException("Imagem inválida.")

                try {
                    val pageWidth = 595
                    val pageHeight = 842
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdf.pages.size + 1).create()
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

                val policy = StandardProtectionPolicy(password, password, permissions)
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
                        stream.showText(signatureText.take(120))
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
