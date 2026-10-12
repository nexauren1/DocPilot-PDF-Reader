package com.nexauren.docpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexauren.docpilot.ui.DocPilotApp
import com.nexauren.docpilot.ui.DocPilotLanguageManager
import com.nexauren.docpilot.ui.DocPilotTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DocPilotLanguageManager.load(this)
        PDFBoxResourceLoader.init(applicationContext)
        enableEdgeToEdge()

        setContent {
            DocPilotTheme {
                DocPilotApp()
            }
        }
    }
}
