package com.nexauren.docpilot.pdf

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfProcessorTest {

    @Test
    fun parses_comma_and_space_separated_page_order() {
        assertEquals(listOf(3, 1, 2, 4), parsePageOrder("3, 1 2;4"))
    }

    @Test
    fun rejects_non_numeric_page_order() {
        runCatching { parsePageOrder("1,a,3") }
            .onSuccess { error("Expected invalid page order to throw") }
    }

    @Test
    fun accepts_complete_page_permutation() {
        validatePageOrder(listOf(3, 1, 2, 4), pageCount = 4)
    }

    @Test
    fun rejects_page_order_that_omits_pages() {
        runCatching { validatePageOrder(listOf(3, 1, 2), pageCount = 4) }
            .onSuccess { error("Expected incomplete page order to throw") }
    }

    @Test
    fun rejects_duplicate_pages_in_reorder_operation() {
        runCatching { validatePageOrder(listOf(1, 2, 2), pageCount = 3) }
            .onSuccess { error("Expected duplicated pages to throw") }
    }

    @Test
    fun rejects_page_numbers_outside_the_document() {
        runCatching { validatePageOrder(listOf(1, 2, 4), pageCount = 3) }
            .onSuccess { error("Expected page outside document bounds to throw") }
    }

    @Test
    fun accepts_valid_single_page_numbers() {
        requireValidPageNumber(1, pageCount = 1)
        requireValidPageNumber(4, pageCount = 4)
    }

    @Test
    fun rejects_page_number_outside_document() {
        runCatching { requireValidPageNumber(0, pageCount = 4) }
            .onSuccess { error("Expected page zero to be rejected") }
        runCatching { requireValidPageNumber(5, pageCount = 4) }
            .onSuccess { error("Expected page beyond document to be rejected") }
    }

    @Test
    fun accepts_supported_page_rotation_angles() {
        requireValidRotationDegrees(90)
        requireValidRotationDegrees(180)
        requireValidRotationDegrees(270)
    }

    @Test
    fun rejects_unsupported_page_rotation_angles() {
        runCatching { requireValidRotationDegrees(0) }
            .onSuccess { error("Expected zero-degree rotation to be rejected") }
        runCatching { requireValidRotationDegrees(45) }
            .onSuccess { error("Expected unsupported rotation to be rejected") }
    }

    @Test
    fun normalizes_watermark_text() {
        assertEquals("DRAFT COPY", normalizeWatermarkText("  DRAFT   COPY  "))
    }

    @Test
    fun rejects_empty_watermark_text() {
        runCatching { normalizeWatermarkText("   ") }
            .onSuccess { error("Expected empty watermark text to be rejected") }
    }

    @Test
    fun rejects_watermark_text_that_is_too_long() {
        runCatching { normalizeWatermarkText("A".repeat(81)) }
            .onSuccess { error("Expected watermark text over 80 characters to be rejected") }
    }

    @Test
    fun formats_bytes_for_ui() {
        assertEquals("1.0 KB", formatBytes(1024))
    }
}
