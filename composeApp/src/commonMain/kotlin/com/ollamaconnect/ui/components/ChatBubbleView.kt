package com.ollamaconnect.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ollamaconnect.models.ChatMessage
import com.ollamaconnect.models.MessageRole
import com.ollamaconnect.ui.theme.*

@Composable
fun ChatBubbleView(
    message: ChatMessage,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val isUser = message.roleEnum == MessageRole.USER

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            AssistantAvatar(modifier = Modifier.padding(end = 8.dp))
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            BubbleContent(
                message = message,
                isStreaming = isStreaming,
                isUser = isUser
            )
            
            val timeString = remember(message.timestamp) {
                com.ollamaconnect.formatTime(message.timestamp)
            }
            Text(
                text = timeString,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(
                    start = if (isUser) 0.dp else 4.dp,
                    end = if (isUser) 4.dp else 0.dp,
                    top = 2.dp
                )
            )
        }

        if (isUser) {
            UserAvatar(modifier = Modifier.padding(start = 8.dp))
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}

@Composable
fun BubbleContent(
    message: ChatMessage,
    isStreaming: Boolean,
    isUser: Boolean
) {
    val shape = if (isUser) {
        RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp)
    }

    val bubbleBackground = if (isUser) {
        Brush.linearGradient(
            colors = listOf(BlueColor, CyanColor)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
    }

    Box(
        modifier = Modifier
            .background(bubbleBackground, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (!isUser && message.content.isEmpty() && isStreaming) {
            TypingDotsView()
        } else {
            MarkdownBlockView(
                content = message.content,
                isUserBubble = isUser
            )
        }
    }
}

@Composable
fun AssistantAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                Brush.linearGradient(colors = listOf(VioletColor, IndigoColor)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🦙",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UserAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                Brush.linearGradient(colors = listOf(BlueColor, CyanColor)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Avatar",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun TypingDotsView() {
    val infiniteTransition = rememberInfiniteTransition()
    
    @Composable
    fun animateDot(delayMillis: Int): Float {
        return infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(delayMillis)
            )
        ).value
    }

    val dot1 = animateDot(0)
    val dot2 = animateDot(150)
    val dot3 = animateDot(300)

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        listOf(dot1, dot2, dot3).forEach { opacity ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(if (opacity > 0.85f) 1.2f else 0.85f)
                    .alpha(opacity)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), CircleShape)
            )
        }
    }
}
