package com.ollamaconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollamaconnect.models.ServerKind
import com.ollamaconnect.store.ModelPresetStore
import com.ollamaconnect.store.localizedPresetName
import com.ollamaconnect.viewmodel.ChatViewModel
import ollama_connect.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsView(
    viewModel: ChatViewModel,
    onManagePresets: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.connection_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.common_done), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Card
            ServerCard(viewModel = viewModel)

            // Connect Card
            ConnectCard(viewModel = viewModel, onDismiss = onDismiss)

            // System Prompt Card
            SystemPromptCard(viewModel = viewModel)

            // Presets Card
            PresetsCard(viewModel = viewModel, onManagePresets = onManagePresets)

            // Parameters Card
            ModelParametersCard(viewModel = viewModel)

            // Context Card
            ContextCard(viewModel = viewModel)

            // Saved connections
            if (viewModel.savedHosts.isNotEmpty()) {
                SavedHostsCard(viewModel = viewModel)
            }

            // Available Models
            if (viewModel.isConnected && viewModel.availableModels.isNotEmpty()) {
                ModelsCard(viewModel = viewModel)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ServerCard(viewModel: ChatViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(Res.string.server_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Typ Segmented Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.server_type_label), fontSize = 15.sp, modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .width(220.dp)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ServerKind.entries.forEach { kind ->
                        val selected = viewModel.serverKind == kind
                        val containerColor = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
                        val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(containerColor, RoundedCornerShape(6.dp))
                                .clickable { viewModel.setServerKindValue(kind) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(kind.displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // IP Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NetworkWifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                OutlinedTextField(
                    value = viewModel.host,
                    onValueChange = { viewModel.host = it },
                    placeholder = { Text(stringResource(Res.string.server_ip_placeholder)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Port
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Adjust, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                OutlinedTextField(
                    value = viewModel.port,
                    onValueChange = {
                        viewModel.port = it
                        viewModel.savePortForCurrentKind()
                    },
                    placeholder = { Text(stringResource(Res.string.server_port_placeholder)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ConnectCard(viewModel: ChatViewModel, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.connection_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (viewModel.isConnected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFF10B981), RoundedCornerShape(4.dp)))
                        Text(stringResource(Res.string.connection_connected_badge), fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }
            }

            if (viewModel.isLoadingModels) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(Res.string.connection_connecting, viewModel.host), fontSize = 14.sp)
                }
            } else {
                Button(
                    onClick = { viewModel.connect() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (viewModel.isConnected) stringResource(Res.string.connection_reconnect_button) else stringResource(Res.string.connection_connect_button))
                }

                if (viewModel.isConnected) {
                    Button(
                        onClick = {
                            viewModel.disconnect()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(Res.string.connection_disconnect_button), color = MaterialTheme.colorScheme.onError)
                    }
                }
            }

            viewModel.errorMessage?.let { error ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Text(error, fontSize = 12.sp, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun SystemPromptCard(viewModel: ChatViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(Res.string.system_prompt_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = viewModel.systemPrompt,
                onValueChange = {
                    viewModel.systemPrompt = it
                    viewModel.saveSystemPromptDefault()
                },
                placeholder = { Text(stringResource(Res.string.system_prompt_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 140.dp),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
fun PresetsCard(viewModel: ChatViewModel, onManagePresets: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val store = viewModel.presetStore
    val activePreset = store.preset(viewModel.activePresetID)
    val activeName = if (activePreset != null) localizedPresetName(activePreset) else stringResource(Res.string.presets_custom_values)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(Res.string.presets_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(activeName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(stringResource(Res.string.presets_tap_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val custom = store.customPresets
                    if (custom.isNotEmpty()) {
                        Text(stringResource(Res.string.presets_group_custom), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        custom.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(localizedPresetName(preset)) },
                                onClick = {
                                    viewModel.applyPreset(preset)
                                    expanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                    }

                    // Built-ins
                    Text(stringResource(Res.string.presets_group_gemma4), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    ModelPresetStore.builtInPresets.filter { it.id.startsWith("builtin.gemma4") }.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(localizedPresetName(preset)) },
                            onClick = {
                                viewModel.applyPreset(preset)
                                expanded = false
                            }
                        )
                    }
                    HorizontalDivider()

                    Text(stringResource(Res.string.presets_group_qwen36), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    ModelPresetStore.builtInPresets.filter { it.id.startsWith("builtin.qwen36") }.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(localizedPresetName(preset)) },
                            onClick = {
                                viewModel.applyPreset(preset)
                                expanded = false
                            }
                        )
                    }
                    HorizontalDivider()

                    Text(stringResource(Res.string.presets_group_universal), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    ModelPresetStore.builtInPresets.filter { it.id.startsWith("builtin.universal") }.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(localizedPresetName(preset)) },
                            onClick = {
                                viewModel.applyPreset(preset)
                                expanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onManagePresets)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.presets_manage_link), fontSize = 15.sp)
                Icon(Icons.Default.ArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
fun ModelParametersCard(viewModel: ChatViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(Res.string.params_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            ParameterSlider(
                label = "Temperature",
                value = viewModel.temperature,
                range = 0f..2f,
                steps = 20,
                format = "%.2f",
                onValueChange = {
                    viewModel.temperature = it.toDouble()
                    viewModel.saveModelParameterDefaults()
                    viewModel.clearActivePreset()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            ParameterSlider(
                label = "Top-K",
                value = viewModel.topK,
                range = 1f..200f,
                steps = 199,
                format = "%.0f",
                onValueChange = {
                    viewModel.topK = it.toDouble()
                    viewModel.saveModelParameterDefaults()
                    viewModel.clearActivePreset()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            ParameterSlider(
                label = "Top-P",
                value = viewModel.topP,
                range = 0f..1f,
                steps = 20,
                format = "%.2f",
                onValueChange = {
                    viewModel.topP = it.toDouble()
                    viewModel.saveModelParameterDefaults()
                    viewModel.clearActivePreset()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            ParameterSlider(
                label = "Min-P",
                value = viewModel.minP,
                range = 0f..1f,
                steps = 100,
                format = "%.2f",
                onValueChange = {
                    viewModel.minP = it.toDouble()
                    viewModel.saveModelParameterDefaults()
                    viewModel.clearActivePreset()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            ParameterSlider(
                label = "Presence Penalty",
                value = viewModel.presencePenalty,
                range = 0f..2f,
                steps = 40,
                format = "%.2f",
                onValueChange = {
                    viewModel.presencePenalty = it.toDouble()
                    viewModel.saveModelParameterDefaults()
                    viewModel.clearActivePreset()
                }
            )
        }
    }
}

@Composable
fun ParameterSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp)
            val digits = if (format == "%.0f") 0 else 2
            Text(formatDecimal(value, digits), fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatDecimal(v: Double, digits: Int): String {
    val factor = if (digits == 0) 1.0 else 100.0
    val rounded = kotlin.math.round(v * factor) / factor
    val str = rounded.toString()
    if (digits == 0) {
        val dot = str.indexOf('.')
        return if (dot != -1) str.substring(0, dot) else str
    }
    val dotIndex = str.indexOf('.')
    if (dotIndex == -1) {
        return str + "." + "0".repeat(digits)
    }
    val dec = str.substring(dotIndex + 1)
    if (dec.length < digits) {
        return str + "0".repeat(digits - dec.length)
    }
    if (dec.length > digits) {
        return str.substring(0, dotIndex + 1 + digits)
    }
    return str
}

@Composable
fun ContextCard(viewModel: ChatViewModel) {
    val messageLimitOptions = listOf(0, 10, 20, 50, 100)
    val tokenOptions = listOf(0, 2048, 4096, 8192, 16384, 32768, 65536, 131072)

    var msgMenuExpanded by remember { mutableStateOf(false) }
    var tokenMenuExpanded by remember { mutableStateOf(false) }

    val allLabel = stringResource(Res.string.context_all)
    val defaultLabel = stringResource(Res.string.context_token_default)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(Res.string.context_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Message Limit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { msgMenuExpanded = true }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.context_message_limit_label), fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val activeLimit = viewModel.contextMessageLimit
                    Text(if (activeLimit == 0) allLabel else stringResource(Res.string.context_last_n, activeLimit), fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(expanded = msgMenuExpanded, onDismissRequest = { msgMenuExpanded = false }) {
                    messageLimitOptions.forEach { limit ->
                        DropdownMenuItem(
                            text = { Text(if (limit == 0) allLabel else stringResource(Res.string.context_last_n, limit)) },
                            onClick = {
                                viewModel.contextMessageLimit = limit
                                viewModel.saveModelParameterDefaults()
                                msgMenuExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Token Limit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { tokenMenuExpanded = true }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.context_token_limit_label), fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val activeTokens = viewModel.contextTokenLimit
                    val label = if (activeTokens == 0) defaultLabel else if (activeTokens >= 1024) "${activeTokens / 1024}K" else "$activeTokens"
                    Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(expanded = tokenMenuExpanded, onDismissRequest = { tokenMenuExpanded = false }) {
                    tokenOptions.forEach { tokens ->
                        val label = if (tokens == 0) defaultLabel else if (tokens >= 1024) "${tokens / 1024}K" else "$tokens"
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.contextTokenLimit = tokens
                                viewModel.saveModelParameterDefaults()
                                tokenMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedHostsCard(viewModel: ChatViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(Res.string.saved_connections_header), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            viewModel.savedHosts.forEachIndexed { index, host ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectHost(host) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("${host.address}:${host.port}", fontSize = 15.sp)
                            Text(host.kind.displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.host == host.address && viewModel.serverKind == host.kind) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.content_desc_active), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.removeHost(host) }) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = stringResource(Res.string.common_delete), tint = Color.Red)
                        }
                    }
                }

                if (index < viewModel.savedHosts.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun ModelsCard(viewModel: ChatViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(Res.string.models_header, viewModel.availableModels.size), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            viewModel.availableModels.forEachIndexed { index, model ->
                val selected = viewModel.selectedModel == model.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectedModel = model.name }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(model.name, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        if (model.displaySize.isNotEmpty()) {
                            Text(model.displaySize, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (selected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = stringResource(Res.string.content_desc_selected), tint = MaterialTheme.colorScheme.primary)
                    }
                }

                if (index < viewModel.availableModels.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
