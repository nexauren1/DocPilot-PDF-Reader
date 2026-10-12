package com.nexauren.docpilot.ui.screens

import com.nexauren.docpilot.model.DocumentItem
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryScreenTest {
    private val documents = listOf(
        DocumentItem(name = "Zulu.pdf", uri = "content://docs/z"),
        DocumentItem(name = "Alpha.pdf", uri = "content://docs/a"),
        DocumentItem(name = "Manual.pdf", uri = "content://docs/m"),
    )

    @Test
    fun filters_names_without_case_sensitivity() {
        val result = filterLibraryDocuments(
            documents = documents,
            query = "ALPHA",
            sortByName = false,
            favoriteUris = emptySet(),
            favoritesOnly = false,
        )
        assertEquals(listOf("Alpha.pdf"), result.map { it.name })
    }

    @Test
    fun favorites_filter_only_returns_marked_documents() {
        val result = filterLibraryDocuments(
            documents = documents,
            query = "",
            sortByName = false,
            favoriteUris = setOf("content://docs/m", "content://docs/a"),
            favoritesOnly = true,
        )
        assertEquals(listOf("Alpha.pdf", "Manual.pdf"), result.map { it.name })
    }

    @Test
    fun alphabetical_sort_is_applied_after_filtering() {
        val result = filterLibraryDocuments(
            documents = documents,
            query = "",
            sortByName = true,
            favoriteUris = emptySet(),
            favoritesOnly = false,
        )
        assertEquals(listOf("Alpha.pdf", "Manual.pdf", "Zulu.pdf"), result.map { it.name })
    }

    @Test
    fun empty_search_returns_all_documents_in_recent_order() {
        val result = filterLibraryDocuments(
            documents = documents,
            query = "",
            sortByName = false,
            favoriteUris = emptySet(),
            favoritesOnly = false,
        )
        assertEquals(documents, result)
    }
}
