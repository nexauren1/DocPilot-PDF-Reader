package com.nexauren.docpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(hasDocumentAccess: Boolean, onRequestDocumentAccess: () -> Unit) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "O DocPilot, à tua maneira.",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Preferências da aplicação e informação útil.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(bottom = 14.dp)) {
                    ListItem(
                        headlineContent = { Text("Biblioteca do dispositivo") },
                        supportingContent = {
                            Text(
                                if (hasDocumentAccess) "Acesso ativo. O DocPilot pode localizar PDFs no armazenamento partilhado."
                                else "Ativa o acesso para encontrar PDFs automaticamente no dispositivo."
                            )
                        },
                        leadingContent = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                    )
                    Button(
                        onClick = onRequestDocumentAccess,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    ) {
                        Text(if (hasDocumentAccess) "Atualizar biblioteca" else "Configurar acesso a documentos")
                    }
                    ListItem(
                        headlineContent = { Text("Aparência") },
                        supportingContent = { Text("O tema acompanha as definições do sistema.") },
                        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                    )
                    ListItem(
                        headlineContent = { Text("Privacidade") },
                        supportingContent = { Text("Os documentos selecionados são processados no dispositivo.") },
                        leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                ListItem(
                    headlineContent = { Text("Sobre o DocPilot") },
                    supportingContent = { Text("DocPilot • versão ${BuildConfig.VERSION_NAME}") },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                )
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                ListItem(
                    headlineContent = { Text("Funcionalidades") },
                    supportingContent = { Text("Leitor de PDF, biblioteca de documentos, scanner, OCR e ferramentas de organização.") },
                    leadingContent = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                )
            }
        }
    }
}
