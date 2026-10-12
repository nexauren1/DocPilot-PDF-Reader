package com.nexauren.docpilot.ui.localization

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationTest {
    @Test
    fun translates_navigation_label_into_all_five_languages() {
        assertEquals("Início", translateForLanguage("pt", "Início"))
        assertEquals("Home", translateForLanguage("en", "Início"))
        assertEquals("Accueil", translateForLanguage("fr", "Início"))
        assertEquals("Inicio", translateForLanguage("es", "Início"))
        assertEquals("Start", translateForLanguage("de", "Início"))
    }

    @Test
    fun preserves_document_names_and_unknown_content() {
        assertEquals("Planning_2026.pdf", translateForLanguage("fr", "Planning_2026.pdf"))
        assertEquals("Texto personalizado", translateForLanguage("de", "Texto personalizado"))
    }

    @Test
    fun localizes_count_templates() {
        assertEquals("4 documents", translateForLanguage("en", "4 documentos"))
        assertEquals("2 favoris", translateForLanguage("fr", "2 favoritos"))
        assertEquals("3 Ergebnisse", translateForLanguage("de", "3 resultados"))
    }

    @Test
    fun localizes_reader_page_counter() {
        assertEquals("Page 2 of 9", translateForLanguage("en", "Página 2 de 9"))
        assertEquals("Seite 2 von 9", translateForLanguage("de", "Página 2 de 9"))
    }
}
