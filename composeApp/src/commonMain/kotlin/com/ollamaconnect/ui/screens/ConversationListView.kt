package com.ollamaconnect.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollamaconnect.models.Conversation
import com.ollamaconnect.shareText
import com.ollamaconnect.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationListView(
    viewModel: ChatViewModel,
    onShowSettings: () -> Unit,
    onNavigateToDetail: () -> Unit = {},
    context: Any? = null,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var renamingConversation by remember { mutableStateOf<Conversation?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredConversations = remember(searchText, viewModel.conversations.size, viewModel.conversations.map { it.updatedAt }) {
        val query = searchText.lowercase().trim()
        if (query.isEmpty()) {
            viewModel.conversations.toList()
        } else {
            viewModel.conversations.filter { conv ->
                conv.title.lowercase().contains(query) ||
                        conv.modelName.lowercase().contains(query) ||
                        conv.persistedMessages.any { it.content.lowercase().contains(query) }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onShowSettings) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Server Verbindung",
                            tint = if (viewModel.isConnected) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.newChat()
                        onNavigateToDetail()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Neuer Chat",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Chats durchsuchen", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen", modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // List or Empty States
            if (viewModel.conversations.isEmpty()) {
                EmptyStateView(
                    isConnected = viewModel.isConnected,
                    onShowSettings = onShowSettings
                )
            } else if (searchText.isNotEmpty() && filteredConversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Keine Ergebnisse für „$searchText“",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conversation ->
                        var isMenuExpanded by remember { mutableStateOf(false) }
                        val isSelected = viewModel.currentConversation?.id == conversation.id

                        val itemBg = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        viewModel.loadConversation(conversation)
                                        onNavigateToDetail()
                                    },
                                    onLongClick = {
                                        isMenuExpanded = true
                                    }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = itemBg)
                        ) {
                            Box {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = conversation.title,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val timeStr = remember(conversation.updatedAt) {
                                            com.ollamaconnect.formatShortDate(conversation.updatedAt)
                                        }
                                        Text(
                                            text = timeStr,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (conversation.modelName.isNotEmpty()) {
                                            Text(
                                                text = "·",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = conversation.modelName,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Context Menu
                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Umbenennen") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                        onClick = {
                                            isMenuExpanded = false
                                            renameText = conversation.title
                                            renamingConversation = conversation
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Teilen") },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                        onClick = {
                                            isMenuExpanded = false
                                            shareText(viewModel.exportConversation(conversation), context)
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Löschen", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            isMenuExpanded = false
                                            viewModel.deleteConversation(conversation)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rename Dialog
        if (renamingConversation != null) {
            AlertDialog(
                onDismissRequest = { renamingConversation = null },
                title = { Text("Chat umbenennen") },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        placeholder = { Text("Neuer Titel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val conv = renamingConversation
                            if (conv != null && renameText.trim().isNotEmpty()) {
                                viewModel.renameConversation(conv, renameText)
                            }
                            renamingConversation = null
                        }
                    ) {
                        Text("Sichern")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renamingConversation = null }) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyStateView(
    isConnected: Boolean,
    onShowSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isConnected) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Keine Chats",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Starte ein neues Gespräch mit dem Stift-Symbol oben rechts.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nicht verbunden",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verbinde dich mit deinem Ollama- oder llama-server, um zu starten.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onShowSettings) {
                Icon(Icons.Default.Wifi, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Server verbinden")
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
