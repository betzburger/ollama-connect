package com.ollamaconnect.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ollama_connect.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpView(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.help_title), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // Header Card
            HelpHeaderCard()

            // Verbindung
            HelpSection(
                icon = Icons.Default.Wifi,
                iconColor = Color(0xFF3B82F6),
                title = stringResource(Res.string.help_section_connection_title),
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Dns,
                        Res.string.help_connection_server_type_title,
                        Res.string.help_connection_server_type_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.SettingsEthernet,
                        Res.string.help_connection_ip_title,
                        Res.string.help_connection_ip_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Refresh,
                        Res.string.help_connection_reconnect_title,
                        Res.string.help_connection_reconnect_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.History,
                        Res.string.help_connection_saved_title,
                        Res.string.help_connection_saved_desc
                    )
                )
            )

            // Chatten
            HelpSection(
                icon = Icons.Default.Chat,
                iconColor = Color(0xFF10B981),
                title = stringResource(Res.string.help_section_chat_title),
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Send,
                        Res.string.help_chat_send_title,
                        Res.string.help_chat_send_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Stop,
                        Res.string.help_chat_stop_title,
                        Res.string.help_chat_stop_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.ContentCopy,
                        Res.string.help_chat_copy_title,
                        Res.string.help_chat_copy_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Computer,
                        Res.string.help_chat_model_title,
                        Res.string.help_chat_model_desc
                    )
                )
            )

            // Chat-Verlauf
            HelpSection(
                icon = Icons.Default.Menu,
                iconColor = Color(0xFF8B5CF6),
                title = stringResource(Res.string.help_section_history_title),
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Edit,
                        Res.string.help_history_new_title,
                        Res.string.help_history_new_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Search,
                        Res.string.help_history_search_title,
                        Res.string.help_history_search_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Label,
                        Res.string.help_history_rename_title,
                        Res.string.help_history_rename_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Share,
                        Res.string.help_history_export_title,
                        Res.string.help_history_export_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Delete,
                        Res.string.help_history_delete_title,
                        Res.string.help_history_delete_desc
                    )
                )
            )

            // Assistent
            HelpSection(
                icon = Icons.Default.Face,
                iconColor = Color(0xFFEC4899),
                title = stringResource(Res.string.help_section_assistant_title),
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.Settings,
                        Res.string.help_assistant_config_title,
                        Res.string.help_assistant_config_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.AutoAwesome,
                        Res.string.help_assistant_soul_title,
                        Res.string.help_assistant_soul_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Memory,
                        Res.string.help_assistant_memory_title,
                        Res.string.help_assistant_memory_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.FolderOpen,
                        Res.string.help_assistant_files_title,
                        Res.string.help_assistant_files_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.ToggleOn,
                        Res.string.help_assistant_toggle_title,
                        Res.string.help_assistant_toggle_desc
                    )
                )
            )

            // Parameter
            HelpSection(
                icon = Icons.Default.Tune,
                iconColor = Color(0xFFEF4444),
                title = stringResource(Res.string.help_section_params_title),
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.DeviceThermostat,
                        Res.string.help_param_temperature_title,
                        Res.string.help_param_temperature_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.FormatListNumbered,
                        Res.string.help_param_topk_title,
                        Res.string.help_param_topk_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Percent,
                        Res.string.help_param_topp_title,
                        Res.string.help_param_topp_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.FilterList,
                        Res.string.help_param_minp_title,
                        Res.string.help_param_minp_desc
                    ),
                    HelpItemInfo(
                        Icons.Default.Repeat,
                        Res.string.help_param_presence_title,
                        Res.string.help_param_presence_desc
                    )
                )
            )

            // Tastaturkürzel
            HelpSection(
                icon = Icons.Default.Keyboard,
                iconColor = Color(0xFF64748B),
                title = stringResource(Res.string.help_section_shortcuts_title),
                items = listOf(
                    HelpItemInfo(
                        Icons.Default.KeyboardTab,
                        Res.string.help_shortcut_send_title,
                        Res.string.help_shortcut_send_desc
                    )
                )
            )

            // Tip Card
            TipCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpHeaderCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🦙", fontSize = 56.sp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Ollama Connect", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(Res.string.help_header_tagline),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

data class HelpItemInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: StringResource,
    val description: StringResource
)

@Composable
fun HelpSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    items: List<HelpItemInfo>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.forEachIndexed { index, item ->
                    HelpRowView(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(start = 36.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HelpRowView(item: HelpItemInfo) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                Text(stringResource(item.title), fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isExpanded) 90f else 0f)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = stringResource(item.description),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(start = 36.dp, top = 8.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun TipCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.help_tip_title), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(Res.string.help_tip_desc),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
