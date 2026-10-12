package com.nexauren.docpilot.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DocPilotLanguageTest {
    @Test
    fun providesFiveDistinctLanguageOptions() {
        assertEquals(5, AppLanguage.entries.size)
        assertEquals(
            listOf("pt", "en", "es", "fr", "de"),
            AppLanguage.entries.map { it.localeCode },
        )
    }

    @Test
    fun translatesNavigationLabelsIntoEverySupportedLanguage() {
        assertEquals("Início", DocPilotLanguageManager.translate("Início", AppLanguage.PORTUGUESE))
        assertEquals("Home", DocPilotLanguageManager.translate("Início", AppLanguage.ENGLISH))
        assertEquals("Inicio", DocPilotLanguageManager.translate("Início", AppLanguage.SPANISH))
        assertEquals("Accueil", DocPilotLanguageManager.translate("Início", AppLanguage.FRENCH))
        assertEquals("Startseite", DocPilotLanguageManager.translate("Início", AppLanguage.GERMAN))
    }

    @Test
    fun translatesDynamicReaderPageCounts() {
        assertEquals("Page 3 of 10", DocPilotLanguageManager.translate("Página 3 de 10", AppLanguage.ENGLISH))
        assertEquals("Página 3 de 10", DocPilotLanguageManager.translate("Página 3 de 10", AppLanguage.SPANISH))
        assertEquals("Page 3 sur 10", DocPilotLanguageManager.translate("Página 3 de 10", AppLanguage.FRENCH))
        assertEquals("Seite 3 von 10", DocPilotLanguageManager.translate("Página 3 de 10", AppLanguage.GERMAN))
    }

    @Test
    fun leavesUnknownCopyIntactRatherThanErasingIt() {
        val text = "A message that has not been translated yet"
        assertEquals(text, DocPilotLanguageManager.translate(text, AppLanguage.FRENCH))
    }

    @Test
    fun translatesPdfPageNumberingTool() {
        assertEquals("Number pages", DocPilotLanguageManager.translate("Numerar páginas", AppLanguage.ENGLISH))
        assertEquals("Numéroter les pages", DocPilotLanguageManager.translate("Numerar páginas", AppLanguage.FRENCH))
    }
}
