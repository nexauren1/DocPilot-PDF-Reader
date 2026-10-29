package com.nexauren.docpilot.ui.screens

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import com.nexauren.docpilot.ui.localization.LText
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nexauren.docpilot.model.DocumentItem
import com.nexauren.docpilot.ui.components.DocumentCard

private const val LIBRARY_PREFERENCES = "docpilot_library"
private const val KEY_FAVORITE_URIS = "favorite_uris"

internal fun filterLibraryDocuments(
    documents: List<DocumentItem>,
    query: String,
    sortByName: Boolean,
    favoriteUris: Set<String>,
    favoritesOnly: Boolean,
): List<DocumentItem> {
    val matching = documents.filter { document ->
        document.name.contains(query.trim(), ignoreCase = true) &&
            (!favoritesOnly || document.uri in favoriteUris)
    }
    return if (sortByName) matching.sortedBy { it.name.lowercase() } else matching
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    documents: List<DocumentItem>,
    onOpenDocument: (DocumentItem) -> Unit,
    onOpenMenu: () -> Unit,
    onImportPdf: () -> Unit = {},
) {
    val context = LocalContext.current
    val libraryPreferences = remember(context) {
        context.getSharedPreferences(LIBRARY_PREFERENCES, Context.MODE_PRIVATE)
    }
    var query by remember { mutableStateOf("") }
    var sortByName by remember { mutableStateOf(false) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var favoriteUris by remember {
        mutableStateOf(libraryPreferences.getStringSet(KEY_FAVORITE_URIS, emptySet()).orEmpty().toSet())
    }
    val filtered = remember(documents, query, sortByName, favoriteUris, favoritesOnly) {
        filterLibraryDocuments(documents, query, sortByName, favoriteUris, favoritesOnly)
    }

    fun toggleFavorite(document: DocumentItem) {
        val updated = favoriteUris.toMutableSet().apply {
            if (document.uri in this) remove(document.uri) else add(document.uri)
        }
        favoriteUris = updated
        libraryPreferences.edit().putStringSet(KEY_FAVORITE_URIS, updated).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LText("Biblioteca") },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Abrir menu")
                    }
                },
                actions = {
                    IconButton(onClick = onImportPdf) {
                        Icon(Icons.Outlined.Add, contentDescription = "Abrir PDF")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            LText(
                "Os teus ficheiros",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(4.dp))
            LText(
                "Pesquisa, organiza e marca os PDFs importantes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(
                        modifier = Modifier.padding(start = 10.dp).weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        LText(
                            "${documents.size} documentos",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        LText(
                            "${favoriteUris.count { uri -> documents.any { it.uri == uri } }} favoritos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LText(
                        "Neste dispositivo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Pesquisar") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Limpar pesquisa")
                        }
                    }
                },
                placeholder = { LText("Pesquisar documentos") },
            )

            LText(
                "${filtered.size} resultados",
                modifier = Modifier.padding(bottom = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = !sortByName,
                    onClick = { sortByName = false },
                    label = { LText("Recentes") },
                )
                FilterChip(
                    selected = sortByName,
                    onClick = { sortByName = true },
                    label = { LText("Nome A–Z") },
                )
                FilterChip(
                    selected = favoritesOnly,
                    onClick = { favoritesOnly = !favoritesOnly },
                    leadingIcon = {
                        Icon(
                            if (favoritesOnly) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                        )
                    },
                    label = { LText("Favoritos") },
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    if (favoritesOnly) Icons.Outlined.StarBorder else Icons.Outlined.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                LText(
                                    when {
                                        documents.isEmpty() -> "A biblioteca está pronta para os teus PDFs"
                                        favoritesOnly -> "Ainda não tens favoritos"
                                        else -> "Não encontrámos esse documento"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                LText(
                                    when {
                                        documents.isEmpty() -> "Quando abrires um PDF, ele ficará listado aqui."
                                        favoritesOnly -> "Toca na estrela de um documento para o guardar nesta lista."
                                        else -> "Experimenta pesquisar com outro nome."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.uri }) { document ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.weight(1f),
                            ) {
                                DocumentCard(document) { onOpenDocument(document) }
                            }
                            IconButton(onClick = { toggleFavorite(document) }) {
                                Icon(
                                    if (document.uri in favoriteUris) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (document.uri in favoriteUris) {
                                        "Remover dos favoritos"
                                    } else {
                                        "Adicionar aos favoritos"
                                    },
                                    tint = if (document.uri in favoriteUris) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
