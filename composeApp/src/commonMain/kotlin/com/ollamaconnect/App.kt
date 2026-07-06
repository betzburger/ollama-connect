package com.ollamaconnect

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ollamaconnect.models.ServerKind
import com.ollamaconnect.store.AppSettings
import com.ollamaconnect.store.ModelPresetStore
import com.ollamaconnect.store.PersonaStore
import com.ollamaconnect.ui.screens.*
import com.ollamaconnect.ui.theme.*
import com.ollamaconnect.viewmodel.ChatViewModel

enum class MobileScreen {
    LIST, DETAIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(storage: LocalStorage, context: Any? = null) {
    val settings = remember { AppSettings(storage) }
    val personaStore = remember { PersonaStore(storage, settings) }
    val presetStore = remember { ModelPresetStore(storage) }
    val viewModel = remember { ChatViewModel(storage, settings, personaStore, presetStore) }

    // Sheets / Dialogs Visibility
    var showSettings by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showPersona by remember { mutableStateOf(false) }
    var showPresets by remember { mutableStateOf(false) }

    // Mobile specific navigation state
    var mobileScreen by remember { mutableStateOf(MobileScreen.LIST) }

    OllamaConnectTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val isWide = maxWidth > 680.dp

            if (isWide) {
                // Wide Screen (Tablet / Desktop): Two Column split view
                Row(modifier = Modifier.fillMaxSize()) {
                    ConversationListView(
                        viewModel = viewModel,
                        onShowSettings = { showSettings = true },
                        onNavigateToDetail = { },
                        context = context,
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                    )
                    
                    VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Detail Area
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (viewModel.isConnected) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Top detail toolbar
                                DetailTopBar(
                                    viewModel = viewModel,
                                    isMobile = false,
                                    onBack = {},
                                    onShowSettings = { showSettings = true },
                                    onShowPersona = { showPersona = true },
                                    onShowHelp = { showHelp = true }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ChatView(viewModel = viewModel, modifier = Modifier.weight(1f))
                            }
                        } else {
                            WelcomeView(onShowSettings = { showSettings = true })
                        }
                    }
                }
            } else {
                // Mobile Screen: Single column nav stack
                Box(modifier = Modifier.fillMaxSize()) {
                    if (mobileScreen == MobileScreen.LIST) {
                        ConversationListView(
                            viewModel = viewModel,
                            onShowSettings = { showSettings = true },
                            onNavigateToDetail = {
                                mobileScreen = MobileScreen.DETAIL
                            },
                            context = context,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            DetailTopBar(
                                viewModel = viewModel,
                                isMobile = true,
                                onBack = { mobileScreen = MobileScreen.LIST },
                                onShowSettings = { showSettings = true },
                                onShowPersona = { showPersona = true },
                                onShowHelp = { showHelp = true }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            if (viewModel.isConnected) {
                                ChatView(viewModel = viewModel, modifier = Modifier.weight(1f))
                            } else {
                                WelcomeView(onShowSettings = { showSettings = true }, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Sheets/Dialogs as Overlay Dialogs
            if (showSettings) {
                FullscreenSheetDialog(onDismiss = { showSettings = false }) {
                    ConnectionSettingsView(
                        viewModel = viewModel,
                        onManagePresets = { showPresets = true },
                        onDismiss = { showSettings = false }
                    )
                }
            }

            if (showPresets) {
                FullscreenSheetDialog(onDismiss = { showPresets = false }) {
                    PresetManagementView(
                        store = presetStore,
                        viewModel = viewModel,
                        onDismiss = { showPresets = false }
                    )
                }
            }

            if (showPersona) {
                FullscreenSheetDialog(onDismiss = { showPersona = false }) {
                    PersonaSettingsView(
                        store = personaStore,
                        onDismiss = { showPersona = false }
                    )
                }
            }

            if (showHelp) {
                FullscreenSheetDialog(onDismiss = { showHelp = false }) {
                    HelpView(
                        onDismiss = { showHelp = false }
                    )
                }
            }
        }
    }
}

@Composable
fun FullscreenSheetDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Allow full width
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    viewModel: ChatViewModel,
    isMobile: Boolean,
    onBack: () -> Unit,
    onShowSettings: () -> Unit,
    onShowPersona: () -> Unit,
    onShowHelp: () -> Unit
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (viewModel.isConnected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { modelMenuExpanded = true }
                ) {
                    Text(
                        text = viewModel.selectedModel.ifEmpty { "Wähle Modell" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)

                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false }
                    ) {
                        viewModel.availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model.name) },
                                onClick = {
                                    viewModel.selectedModel = model.name
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text("Ollama Connect", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        navigationIcon = {
            if (isMobile) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            } else {
                // Connection pill button (macOS detail topBar style)
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onShowSettings)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (viewModel.isConnected) Color(0xFF10B981) else Color.Red,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (viewModel.isConnected) viewModel.host else "Verbinden",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Persona Button
            IconButton(onClick = onShowPersona) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Mira Persona",
                    tint = VioletColor
                )
            }

            // Help Button
            IconButton(onClick = onShowHelp) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Hilfe",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (viewModel.isConnected) {
                // New Chat Button
                IconButton(onClick = { viewModel.newChat() }) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "Neuer Chat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
