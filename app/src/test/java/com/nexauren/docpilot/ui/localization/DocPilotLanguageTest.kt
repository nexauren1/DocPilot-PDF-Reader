package com.nexauren.docpilot.ui.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocPilotLanguageTest {
    @Test
    fun exposesFiveSupportedLanguages() {
        assertEquals(5, DocPilotLanguage.supported.size)
        assertEquals(
            setOf("pt", "en", "fr", "es", "de"),
            DocPilotLanguage.supported.map { it.first }.toSet(),
        )
    }

    @Test
    fun everyLanguageHasADisplayName() {
        assertTrue(DocPilotLanguage.supported.all { (code, name) ->
            code.isNotBlank() && name.isNotBlank()
        })
    }
}
