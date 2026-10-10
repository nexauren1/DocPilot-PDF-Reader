package com.nexauren.docpilot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    folderConnected: Boolean,
    folderName: String?,
    scanning: Boolean,
    onChooseFolder: () -> Unit,
    onRefreshFolder: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Definições") }) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(2.dp))
            Text("O DocPilot, à tua maneira.", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Controla a tua biblioteca e vê como os teus documentos são tratados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        Icon(Icons.Outlined.FolderOpen, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Acesso à biblioteca", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (folderConnected) folderName ?: "Pasta selecionada" else "Nenhuma pasta ligada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(onClick = onRefreshFolder, enabled = folderConnected && !scanning) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Atualizar pasta")
                        }
                    }
                    if (scanning) LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(
                        "O acesso fica limitado à pasta e às subpastas que selecionares no seletor do Android. Podes alterar a pasta quando quiseres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = onChooseFolder, enabled = !scanning, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Text(if (folderConnected) "Alterar pasta autorizada" else "Escolher pasta")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Aparência") },
                        supportingContent = { Text("O tema claro ou escuro acompanha as definições do sistema.") },
                        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                    )
                    ListItem(
                        headlineContent = { Text("Privacidade local") },
                        supportingContent = { Text("O processamento de PDFs decorre no dispositivo. O DocPilot não precisa de uma conta.") },
                        leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null) },
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Sobre o DocPilot") },
                        supportingContent = { Text("Versão ${BuildConfig.VERSION_NAME} • Leitor e ferramentas PDF") },
                        leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    )
                    ListItem(
                        headlineContent = { Text("Ferramentas") },
                        supportingContent = { Text("Pesquisa no PDF, marcadores, scanner, OCR, rotação, marcas de água, paginação, juntar, dividir e comprimir.") },
                        leadingContent = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
