package com.nexauren.docpilot

import com.nexauren.docpilot.model.DocumentItem
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentItemTest {
    @Test
    fun document_item_keeps_name_and_uri() {
        val item = DocumentItem("manual.pdf", "content://example/manual")
        assertEquals("manual.pdf", item.name)
        assertEquals("content://example/manual", item.uri)
    }
}
