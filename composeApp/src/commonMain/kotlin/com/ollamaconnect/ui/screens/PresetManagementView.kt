package com.ollamaconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollamaconnect.models.ModelPreset
import com.ollamaconnect.store.ModelPresetStore
import com.ollamaconnect.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetManagementView(
    store: ModelPresetStore,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingPreset by remember { mutableStateOf<ModelPreset?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presets verwalten", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text("Fertig", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add Preset Button
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.clickable { creatingNew = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Aus aktuellen Werten neues Preset erstellen", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Custom Presets
                item {
                    Text("EIGENE PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (store.customPresets.isEmpty()) {
                    item {
                        Text(
                            "Noch keine eigenen Presets. Tippe oben auf den Button, um die aktuell eingestellten Werte als Preset zu speichern.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                } else {
                    items(store.customPresets, key = { it.id }) { preset ->
                        PresetRow(
                            preset = preset,
                            onClick = { editingPreset = preset },
                            onDelete = {
                                store.deleteCustomPreset(preset.id)
                                if (viewModel.activePresetID == preset.id) {
                                    viewModel.clearActivePreset()
                                }
                            }
                        )
                    }
                }

                // Built-in Presets
                item {
                    Text("MITGELIEFERTE PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(ModelPresetStore.builtInPresets, key = { it.id }) { preset ->
                    PresetRow(
                        preset = preset,
                        onClick = {}, // Read-only
                        onDelete = null
                    )
                }

                item {
                    Text(
                        "Werte aus den offiziellen Unsloth-Empfehlungen für Gemma 4 und Qwen3.6. Built-in-Presets können nicht gelöscht werden — du kannst sie aber als Vorlage für eigene Presets nutzen.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Create New Dialog
            if (creatingNew) {
                PresetEditorDialog(
                    initial = ModelPreset(
                        name = "Mein Preset",
                        summary = "",
                        temperature = viewModel.temperature,
                        topK = viewModel.topK.toInt(),
                        topP = viewModel.topP,
                        minP = viewModel.minP,
                        presencePenalty = viewModel.presencePenalty,
                        numCtx = if (viewModel.contextTokenLimit > 0) viewModel.contextTokenLimit else null
                    ),
                    title = "Neues Preset",
                    onDismiss = { creatingNew = false },
                    onConfirm = { preset ->
                        store.addCustomPreset(preset)
                        creatingNew = false
                    }
                )
            }

            // Edit Existing Dialog
            editingPreset?.let { preset ->
                PresetEditorDialog(
                    initial = preset,
                    title = "Preset bearbeiten",
                    onDismiss = { editingPreset = null },
                    onConfirm = { updated ->
                        store.updateCustomPreset(updated)
                        editingPreset = null
                    }
                )
            }
        }
    }
}

@Composable
fun PresetRow(
    preset: ModelPreset,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(preset.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (preset.isBuiltIn) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Built-in", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (preset.summary.isNotEmpty()) {
                    Text(preset.summary, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(preset.shortLabel, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun PresetEditorDialog(
    initial: ModelPreset,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (ModelPreset) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var summary by remember { mutableStateOf(initial.summary) }
    var temperature by remember { mutableStateOf(initial.temperature) }
    var topK by remember { mutableStateOf(initial.topK.toDouble()) }
    var topP by remember { mutableStateOf(initial.topP) }
    var minP by remember { mutableStateOf(initial.minP) }
    var presencePenalty by remember { mutableStateOf(initial.presencePenalty) }
    
    var hasCustomLimit by remember { mutableStateOf(initial.numCtx != null) }
    var numCtx by remember { mutableStateOf(initial.numCtx ?: 8192) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name & Beschreibung
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("z.B. „Llama 3 – Coding“") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Beschreibung (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Sliders
                ParameterSlider(
                    label = "Temperature",
                    value = temperature,
                    range = 0f..2f,
                    steps = 40,
                    format = "%.2f",
                    onValueChange = { temperature = it.toDouble() }
                )

                ParameterSlider(
                    label = "Top-K",
                    value = topK,
                    range = 1f..200f,
                    steps = 199,
                    format = "%.0f",
                    onValueChange = { topK = it.toDouble() }
                )

                ParameterSlider(
                    label = "Top-P",
                    value = topP,
                    range = 0f..1f,
                    steps = 100,
                    format = "%.2f",
                    onValueChange = { topP = it.toDouble() }
                )

                ParameterSlider(
                    label = "Min-P",
                    value = minP,
                    range = 0f..1f,
                    steps = 100,
                    format = "%.2f",
                    onValueChange = { minP = it.toDouble() }
                )

                ParameterSlider(
                    label = "Presence Penalty",
                    value = presencePenalty,
                    range = 0f..2f,
                    steps = 40,
                    format = "%.2f",
                    onValueChange = { presencePenalty = it.toDouble() }
                )

                // Token Limit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Eigenes Token-Limit setzen", fontSize = 14.sp)
                    Switch(checked = hasCustomLimit, onCheckedChange = { hasCustomLimit = it })
                }

                if (hasCustomLimit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("num_ctx", fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { if (numCtx > 1024) numCtx -= 1024 }) {
                                Icon(Icons.Default.Remove, contentDescription = "Dekrement")
                            }
                            Text("$numCtx Tokens", fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                            IconButton(onClick = { if (numCtx < 262144) numCtx += 1024 }) {
                                Icon(Icons.Default.Add, contentDescription = "Inkrement")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        val preset = initial.copy(
                            name = name.trim(),
                            summary = summary.trim(),
                            temperature = temperature,
                            topK = topK.toInt(),
                            topP = topP,
                            minP = minP,
                            presencePenalty = presencePenalty,
                            numCtx = if (hasCustomLimit) numCtx else null
                        )
                        onConfirm(preset)
                    }
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Sichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
