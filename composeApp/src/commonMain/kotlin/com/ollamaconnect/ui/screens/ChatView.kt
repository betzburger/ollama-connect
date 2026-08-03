package com.ollamaconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollamaconnect.formatTokenCount
import com.ollamaconnect.ui.components.ChatBubbleView
import com.ollamaconnect.viewmodel.ChatViewModel
import ollama_connect.composeapp.generated.resources.Res
import ollama_connect.composeapp.generated.resources.chat_input_placeholder
import ollama_connect.composeapp.generated.resources.content_desc_send
import ollama_connect.composeapp.generated.resources.content_desc_stop_generation
import ollama_connect.composeapp.generated.resources.context_info_limited
import ollama_connect.composeapp.generated.resources.context_token_default
import ollama_connect.composeapp.generated.resources.token_info_breakdown
import ollama_connect.composeapp.generated.resources.token_info_limit_only
import ollama_connect.composeapp.generated.resources.token_info_total
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    // Scroll to bottom when streaming content changes
    val lastMessageContent = viewModel.messages.lastOrNull()?.content
    LaunchedEffect(lastMessageContent) {
        if (viewModel.messages.isNotEmpty()) {
            listState.scrollToItem(viewModel.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Message Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(viewModel.messages, key = { it.id }) { message ->
                    val isLast = viewModel.messages.lastOrNull()?.id == message.id
                    ChatBubbleView(
                        message = message,
                        isStreaming = viewModel.isGenerating && isLast
                    )
                }
            }
        }

        // Banners
        AnimatedVisibility(
            visible = viewModel.errorMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val error = viewModel.errorMessage
            if (error != null) {
                ErrorBanner(
                    message = error,
                    onDismiss = { viewModel.errorMessage = null }
                )
            }
        }

        val contextInfo = if (viewModel.contextMessageLimit > 0 && viewModel.messages.size > viewModel.contextMessageLimit) {
            stringResource(Res.string.context_info_limited, viewModel.contextMessageLimit, viewModel.messages.size)
        } else {
            null
        }

        val tokenInfo = viewModel.tokenInfo?.let { info ->
            val limitLabel = if (info.limit > 0) {
                formatTokenCount(info.limit)
            } else {
                stringResource(Res.string.context_token_default)
            }
            if (info.total == null) {
                stringResource(Res.string.token_info_limit_only, limitLabel)
            } else {
                val parts = mutableListOf(
                    stringResource(Res.string.token_info_total, formatTokenCount(info.total), limitLabel)
                )
                if (info.promptTokens != null && info.completionTokens != null) {
                    parts.add(
                        stringResource(
                            Res.string.token_info_breakdown,
                            formatTokenCount(info.promptTokens),
                            formatTokenCount(info.completionTokens)
                        )
                    )
                }
                parts.joinToString(" · ")
            }
        }

        val statusInfo = listOfNotNull(contextInfo, tokenInfo)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" · ")

        AnimatedVisibility(
            visible = statusInfo != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            val info = statusInfo
            if (info != null) {
                ContextInfoBanner(message = info)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Message Input Bar
        MessageInputBar(viewModel = viewModel)
    }
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ContextInfoBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun MessageInputBar(
    viewModel: ChatViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // TextField
        OutlinedTextField(
            value = viewModel.inputText,
            onValueChange = { viewModel.inputText = it },
            placeholder = { Text(stringResource(Res.string.chat_input_placeholder), fontSize = 14.sp) },
            modifier = Modifier
                .weight(1f)
                .onKeyEvent { keyEvent ->
                    // Capture ⌘+Enter to submit message
                    if (keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key == Key.Enter &&
                        keyEvent.isMetaPressed
                    ) {
                        if (viewModel.canSend) {
                            viewModel.sendMessage()
                            true
                        } else false
                    } else false
                },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            maxLines = 8,
            enabled = viewModel.isConnected && !viewModel.isGenerating
        )

        // Action Button
        AnimatedContent(
            targetState = viewModel.isGenerating,
            transitionSpec = {
                scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
            }
        ) { generating ->
            if (generating) {
                IconButton(
                    onClick = { viewModel.stopGeneration() },
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red, RoundedCornerShape(17.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = stringResource(Res.string.content_desc_stop_generation),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = viewModel.canSend,
                    modifier = Modifier.size(34.dp)
                ) {
                    val color = if (viewModel.canSend) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, RoundedCornerShape(17.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = stringResource(Res.string.content_desc_send),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp): Modifier = this.width(size).height(size)
