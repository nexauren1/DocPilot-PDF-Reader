package com.nexauren.docpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Definições") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(),
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Aparência") },
                        supportingContent = { Text("Segue o tema do sistema nesta primeira versão") },
                        leadingContent = {
                            Icon(Icons.Outlined.DarkMode, contentDescription = null)
                        },
                    )
                    ListItem(
                        headlineContent = { Text("Privacidade") },
                        supportingContent = { Text("Processamento local será a prioridade do DocPilot") },
                        leadingContent = {
                            Icon(Icons.Outlined.Security, contentDescription = null)
                        },
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(),
            ) {
                ListItem(
                    headlineContent = { Text("Sobre o DocPilot") },
                    supportingContent = { Text("PDF Reader & Document Tools • versão 0.1.0") },
                    leadingContent = {
                        Icon(Icons.Outlined.Info, contentDescription = null)
                    },
                )
            }

            Card(
                colors = CardDefaults.cardColors(),
            ) {
                ListItem(
                    headlineContent = { Text("Estado do projeto") },
                    supportingContent = { Text("Base inicial preparada para leitura, biblioteca e ferramentas") },
                    leadingContent = {
                        Icon(Icons.Outlined.Settings, contentDescription = null)
                    },
                )
            }
        }
    }
}
