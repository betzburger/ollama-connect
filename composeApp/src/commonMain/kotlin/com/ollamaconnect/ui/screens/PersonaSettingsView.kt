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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollamaconnect.store.PersonaStore

enum class PersonaTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color, val fileName: String) {
    CONFIG("Konfiguration", Icons.Default.Person, Color(0xFF3B82F6), "config.md"),
    SOUL("Persönlichkeit", Icons.Default.AutoAwesome, Color(0xFF8B5CF6), "soul.md"),
    MEMORY("Gedächtnis", Icons.Default.Memory, Color(0xFFF59E0B), "memory.md")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaSettingsView(
    store: PersonaStore,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PersonaTab.CONFIG) }
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Trigger local state reload when switching tabs or modifying
    var textDraft by remember(selectedTab, store.config, store.soul, store.memory) {
        mutableStateOf(
            when (selectedTab) {
                PersonaTab.CONFIG -> store.config
                PersonaTab.SOUL -> store.soul
                PersonaTab.MEMORY -> store.memory
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistent", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    TextButton(
                        onClick = {
                            // Save current draft before dismissing
                            when (selectedTab) {
                                PersonaTab.CONFIG -> { store.config = textDraft; store.saveConfig() }
                                PersonaTab.SOUL -> { store.soul = textDraft; store.saveSoul() }
                                PersonaTab.MEMORY -> { store.memory = textDraft; store.saveMemory() }
                            }
                            onDismiss()
                        }
                    ) {
                        Text("Fertig", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                PersonaTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            // Auto save previous draft when switching tabs
                            when (selectedTab) {
                                PersonaTab.CONFIG -> { store.config = textDraft; store.saveConfig() }
                                PersonaTab.SOUL -> { store.soul = textDraft; store.saveSoul() }
                                PersonaTab.MEMORY -> { store.memory = textDraft; store.saveMemory() }
                            }
                            selectedTab = tab
                        },
                        text = { Text(tab.title, fontSize = 12.sp) },
                        icon = { Icon(tab.icon, contentDescription = null, tint = if (selectedTab == tab) tab.color else Color.Gray) }
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro Card
                IntroCard(selectedTab = selectedTab)

                // Toggles Card
                TogglesCard(selectedTab = selectedTab, store = store)

                // Editor Card
                EditorCard(
                    text = textDraft,
                    onTextChange = { textDraft = it }
                )

                // Actions Card
                ActionsCard(
                    selectedTab = selectedTab,
                    onSave = {
                        when (selectedTab) {
                            PersonaTab.CONFIG -> { store.config = textDraft; store.saveConfig() }
                            PersonaTab.SOUL -> { store.soul = textDraft; store.saveSoul() }
                            PersonaTab.MEMORY -> { store.memory = textDraft; store.saveMemory() }
                        }
                    },
                    onReload = {
                        store.reloadFromDisk()
                        // refresh local draft
                        textDraft = when (selectedTab) {
                            PersonaTab.CONFIG -> store.config
                            PersonaTab.SOUL -> store.soul
                            PersonaTab.MEMORY -> store.memory
                        }
                    },
                    onReset = {
                        showResetDialog = true
                    }
                )

                // File Location Info
                FileLocationCard(selectedTab = selectedTab, baseDir = store.composedSystemPrompt()) // Composed prompt isn't baseDir but let's pass a description
            }
        }

        // Reset Alert Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Auf Standard zurücksetzen?") },
                text = { Text("Der Inhalt von „${selectedTab.title}“ wird durch die mitgelieferte Vorlage ersetzt.") },
                confirmButton = {
                    Button(
                        onClick = {
                            when (selectedTab) {
                                PersonaTab.CONFIG -> store.resetConfigToDefault()
                                PersonaTab.SOUL -> store.resetSoulToDefault()
                                PersonaTab.MEMORY -> store.resetMemoryToDefault()
                            }
                            textDraft = when (selectedTab) {
                                PersonaTab.CONFIG -> store.config
                                PersonaTab.SOUL -> store.soul
                                PersonaTab.MEMORY -> store.memory
                            }
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Zurücksetzen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}

@Composable
fun IntroCard(selectedTab: PersonaTab) {
    val text = when (selectedTab) {
        PersonaTab.CONFIG -> "Definiere, wer der Assistent ist (Name, Rolle), wer du bist und welche Rahmenbedingungen für jeden Chat gelten — etwa Sprache, Stil und no-gos."
        PersonaTab.SOUL -> "Hier prägst du den Vibe: Tonfall, Energie, Sprachstil. Diese Eigenschaften bestimmen, wie der Assistent klingt — nicht, was er sagt."
        PersonaTab.MEMORY -> "Persistente Fakten, die der Assistent über alle Chats hinweg im Kopf behält. Bittest du im Chat darum, etwas zu merken, wird es hier automatisch ergänzt. Du kannst Einträge auch direkt editieren."
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(selectedTab.icon, contentDescription = null, tint = selectedTab.color, modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(selectedTab.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun TogglesCard(selectedTab: PersonaTab, store: PersonaStore) {
    var configEnabled by remember(store.configEnabled) { mutableStateOf(store.configEnabled) }
    var soulEnabled by remember(store.soulEnabled) { mutableStateOf(store.soulEnabled) }
    var memoryEnabled by remember(store.memoryEnabled) { mutableStateOf(store.memoryEnabled) }
    var autoMemoryEnabled by remember(store.autoMemoryEnabled) { mutableStateOf(store.autoMemoryEnabled) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Main Toggle depending on active tab
            when (selectedTab) {
                PersonaTab.CONFIG -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Konfiguration aktiv", fontSize = 15.sp)
                        Switch(
                            checked = configEnabled,
                            onCheckedChange = {
                                configEnabled = it
                                store.configEnabled = it
                            }
                        )
                    }
                }
                PersonaTab.SOUL -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Persönlichkeit aktiv", fontSize = 15.sp)
                        Switch(
                            checked = soulEnabled,
                            onCheckedChange = {
                                soulEnabled = it
                                store.soulEnabled = it
                            }
                        )
                    }
                }
                PersonaTab.MEMORY -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gedächtnis aktiv", fontSize = 15.sp)
                            Switch(
                                checked = memoryEnabled,
                                onCheckedChange = {
                                    memoryEnabled = it
                                    store.memoryEnabled = it
                                }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Automatisch merken", fontSize = 15.sp)
                                Text("Erlaubt der LLM, neue Fakten via <remember>-Tag selbst zu speichern.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoMemoryEnabled,
                                onCheckedChange = {
                                    autoMemoryEnabled = it
                                    store.autoMemoryEnabled = it
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorCard(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("INHALT (MARKDOWN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${text.length} Zeichen", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
fun ActionsCard(
    selectedTab: PersonaTab,
    onSave: () -> Unit,
    onReload: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSave)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = selectedTab.color)
                Text("Auf Datei speichern", fontSize = 15.sp)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(start = 48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReload)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Aus Datei neu laden", fontSize = 15.sp)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(start = 48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReset)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Auf Vorlage zurücksetzen", fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FileLocationCard(selectedTab: PersonaTab, baseDir: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DATEIPFAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Assistant/${selectedTab.fileName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Die Datei befindet sich im Assistant-Ordner deines Anwendungsdaten-Verzeichnisses und kann dort auch von externen Editoren bearbeitet werden. Tippe danach auf „Aus Datei neu laden“.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
