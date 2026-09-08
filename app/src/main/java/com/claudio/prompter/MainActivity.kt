package com.claudio.prompter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PrompterApp() }
    }
}

private val Background = Color(0xFFFAF9FE)
private val SurfaceColor = Color(0xFFFFFFFF)
private val Accent = Color(0xFF4E6590)
private val TextPrimary = Color(0xFF1B1B20)
private val TextSecondary = Color(0xFF60616A)

private data class PromptPreview(
    val title: String,
    val category: String,
    val favorite: Boolean,
    val text: String
)

@Composable
private fun PrompterApp() {
    var search by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tutti") }
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // Dati demo/test temporanei: non rappresentano contenuto prodotto definitivo.
    val prompts = listOf(
        PromptPreview("Yu-Gi-Oh – crea carta", "Immagini", true,
            "Crea una carta originale con nome, attributo ed effetto bilanciato."),
        PromptPreview("Flow – preserva soggetto", "Video", true,
            "Anima il soggetto mantenendone identità e proporzioni, con movimenti fluidi."),
        PromptPreview("Ricerca approfondita", "Ricerca", true,
            "Analizza l'argomento, confronta fonti affidabili e riporta conclusioni con riferimenti verificabili."),
        PromptPreview("Riscrivi testo in italiano", "Testo", false,
            "Riformula il contenuto con grammatica corretta e frasi chiare, preservando il significato originale."),
        PromptPreview("Prompt immagine prodotto", "Immagini", false,
            "Crea una fotografia del prodotto con illuminazione da studio e sfondo neutro.")
    )
    val filters = listOf("Tutti", "Immagini", "Video", "Ricerca", "Testo")
    val query = search.trim()
    val matchingPrompts = prompts.filter { prompt ->
        (selectedFilter == "Tutti" || prompt.category == selectedFilter) &&
            (query.isEmpty() ||
                prompt.title.contains(query, ignoreCase = true) ||
                prompt.category.contains(query, ignoreCase = true) ||
                prompt.text.contains(query, ignoreCase = true))
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Prompter", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    item {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            placeholder = { Text("Cerca prompt") },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor,
                                focusedBorderColor = Accent,
                                unfocusedBorderColor = Color(0xFFE0E0E6)
                            )
                        )
                    }
                    item { SectionTitle("Preferiti") }
                    items(matchingPrompts.filter { it.favorite }) { prompt ->
                        PromptCard(prompt) {
                            clipboardManager.setText(AnnotatedString(prompt.text))
                            coroutineScope.launch { snackbarHostState.showSnackbar("Prompt copiato") }
                        }
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filters) { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter) }
                                )
                            }
                        }
                    }
                    item { SectionTitle("Tutti i prompt") }
                    if (matchingPrompts.isEmpty()) {
                        item { Text("Nessun prompt trovato", color = TextSecondary) }
                    }
                    items(matchingPrompts) { prompt ->
                        PromptCard(prompt) {
                            clipboardManager.setText(AnnotatedString(prompt.text))
                            coroutineScope.launch { snackbarHostState.showSnackbar("Prompt copiato") }
                        }
                    }
                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                )
                FloatingActionButton(
                    onClick = {},
                    modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                    containerColor = Accent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) { Icon(Icons.Outlined.Add, contentDescription = "Nuovo prompt") }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, modifier = Modifier.padding(top = 8.dp), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
}

@Composable
private fun PromptCard(prompt: PromptPreview, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(prompt.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    if (prompt.favorite) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Outlined.Star, contentDescription = "Preferito", modifier = Modifier.size(17.dp), tint = Accent)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(prompt.category, fontSize = 14.sp, color = TextSecondary)
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copia prompt", tint = Accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}
