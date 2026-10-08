package com.nexauren.docpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexauren.docpilot.ui.DocPilotApp
import com.nexauren.docpilot.ui.DocPilotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DocPilotTheme {
                DocPilotApp()
            }
        }
    }
}
