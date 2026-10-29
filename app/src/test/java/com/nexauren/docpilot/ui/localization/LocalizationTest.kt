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
    fun localizes_page_numbering_tool() {
        assertEquals("Numerar páginas", translateForLanguage("pt", "Numerar páginas"))
        assertEquals("Number pages", translateForLanguage("en", "Numerar páginas"))
        assertEquals("Numéroter les pages", translateForLanguage("fr", "Numerar páginas"))
        assertEquals("Numerar páginas", translateForLanguage("es", "Numerar páginas"))
        assertEquals("Seiten nummerieren", translateForLanguage("de", "Numerar páginas"))
    }

    @Test
    fun translates_watermark_tool_into_all_five_languages() {
        assertEquals("Marca de água", translateForLanguage("pt", "Marca de água"))
        assertEquals("Watermark", translateForLanguage("en", "Marca de água"))
        assertEquals("Filigrane", translateForLanguage("fr", "Marca de água"))
        assertEquals("Marca de agua", translateForLanguage("es", "Marca de água"))
        assertEquals("Wasserzeichen", translateForLanguage("de", "Marca de água"))
    }

    @Test
    fun localizes_watermark_action_and_validation_text() {
        assertEquals("Apply watermark", translateForLanguage("en", "Aplicar marca de água"))
        assertEquals("Gib den Wasserzeichentext ein.", translateForLanguage("de", "Escreve o texto da marca de água."))
    }

    @Test
    fun localizes_reader_page_counter() {
        assertEquals("Page 2 of 9", translateForLanguage("en", "Página 2 de 9"))
        assertEquals("Seite 2 von 9", translateForLanguage("de", "Página 2 de 9"))
    }
    @Test
    fun localizes_tool_categories_and_processing_counters() {
        assertEquals("All", translateForLanguage("en", "Todas"))
        assertEquals("Organiser", translateForLanguage("fr", "Organizar"))
        assertEquals("Convert", translateForLanguage("en", "Converter"))
        assertEquals("Sicherheit", translateForLanguage("de", "Segurança"))
        assertEquals("Text & analysis", translateForLanguage("en", "Texto e análise"))
        assertEquals("Files", translateForLanguage("en", "Ficheiros"))
        assertEquals("Images", translateForLanguage("en", "Imagens"))
        assertEquals("Seiten", translateForLanguage("de", "páginas"))
    }

    @Test
    fun localizes_native_pdf_picker_and_new_home_actions() {
        assertEquals("Open PDF", translateForLanguage("en", "Abrir PDF"))
        assertEquals("Ouvrir un PDF", translateForLanguage("fr", "Abrir PDF"))
        assertEquals("PDF öffnen", translateForLanguage("de", "Abrir PDF"))
        assertEquals("Select one or more files", translateForLanguage("en", "Seleciona um ou mais ficheiros"))
        assertEquals("Rechercher des fichiers", translateForLanguage("fr", "Pesquisar ficheiros"))
        assertEquals("Keine Dateien gefunden", translateForLanguage("de", "Nenhum ficheiro encontrado"))
        assertEquals("Use selection", translateForLanguage("en", "Usar seleção"))
        assertEquals("Activar acceso al dispositivo", translateForLanguage("es", "Ativar acesso ao dispositivo"))
        assertEquals("Your documents. Your control.", translateForLanguage("en", "Os teus documentos. O teu controlo."))
    }

    @Test
    fun localizes_in_app_folder_navigation() {
        assertEquals("Internal storage", translateForLanguage("en", "Armazenamento interno"))
        assertEquals("Fichiers récents", translateForLanguage("fr", "Ficheiros recentes"))
        assertEquals("Browse folders without leaving DocPilot", translateForLanguage("en", "Navega pelas pastas sem sair do DocPilot"))
        assertEquals("Übergeordneter Ordner", translateForLanguage("de", "Pasta anterior"))
        assertEquals("Carpeta", translateForLanguage("es", "Pasta"))
        assertEquals("This folder has no files of this type", translateForLanguage("en", "Esta pasta não contém ficheiros deste tipo"))
    }

    @Test
    fun localizes_tools_empty_state_and_filter_reset() {
        assertEquals("No tools found", translateForLanguage("en", "Não encontrámos ferramentas"))
        assertEquals("Aucun outil trouvé", translateForLanguage("fr", "Não encontrámos ferramentas"))
        assertEquals("Prueba otra búsqueda o borra los filtros activos.", translateForLanguage("es", "Experimenta outra pesquisa ou remove os filtros ativos."))
        assertEquals("Filter löschen", translateForLanguage("de", "Limpar filtros"))
    }

    @Test
    fun localizes_batch_ocr_tool() {
        assertEquals(
            "Extract text from multiple images",
            translateForLanguage("en", "Extrair texto de várias imagens"),
        )
        assertEquals(
            "Recognizes text from one or multiple images on-device.",
            translateForLanguage("en", "Reconhece texto de uma ou várias imagens localmente."),
        )
    }

}
