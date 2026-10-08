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
    fun formats_bytes_for_ui() {
        assertEquals("1.0 KB", formatBytes(1024))
    }
}
