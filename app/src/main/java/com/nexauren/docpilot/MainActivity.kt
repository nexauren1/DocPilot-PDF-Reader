package com.nexauren.docpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.nexauren.docpilot.ui.DocPilotApp
import com.nexauren.docpilot.ui.DocPilotTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
    private val widgetDestination = mutableStateOf<String?>(null)
    private val widgetCategory = mutableStateOf<String?>(null)
    private val navigationRequestId = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        enableEdgeToEdge()
        readWidgetRoute(intent)

        setContent {
            DocPilotTheme {
                DocPilotApp(
                    initialDestination = widgetDestination.value,
                    initialToolCategory = widgetCategory.value,
                    navigationRequestId = navigationRequestId.value,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readWidgetRoute(intent)
    }

    private fun readWidgetRoute(intent: android.content.Intent?) {
        widgetDestination.value = intent?.getStringExtra(EXTRA_WIDGET_DESTINATION)
        widgetCategory.value = intent?.getStringExtra(EXTRA_WIDGET_CATEGORY)
        navigationRequestId.value += 1
    }

    companion object {
        const val EXTRA_WIDGET_DESTINATION = "com.nexauren.docpilot.extra.WIDGET_DESTINATION"
        const val EXTRA_WIDGET_CATEGORY = "com.nexauren.docpilot.extra.WIDGET_CATEGORY"
    }
}
