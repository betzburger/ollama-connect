package com.ollamaconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ollama_connect.composeapp.generated.resources.Res
import ollama_connect.composeapp.generated.resources.content_desc_copy_code
import org.jetbrains.compose.resources.stringResource

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class NumberedItem(val number: Int, val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String?, val code: String) : MarkdownBlock()
    object DividerBlock : MarkdownBlock()
}

@Composable
fun MarkdownBlockView(
    content: String,
    isUserBubble: Boolean,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { parseMarkdown(content) }

    SelectionContainer {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text),
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
                is MarkdownBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        3 -> 16.sp
                        else -> 15.sp
                    }
                    val fontWeight = FontWeight.Bold
                    val topPadding = when (block.level) {
                        1 -> 8.dp
                        2 -> 6.dp
                        else -> 4.dp
                    }
                    Text(
                        text = parseInlineMarkdown(block.text),
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = topPadding)
                    )
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${block.number}.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.width(22.dp),
                            style = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            fontSize = 15.sp,
                            color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = parseInlineMarkdown(block.text),
                            fontSize = 15.sp,
                            color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code,
                        isUserBubble = isUserBubble
                    )
                }
                is MarkdownBlock.DividerBlock -> {
                    HorizontalDivider(
                        color = (if (isUserBubble) Color.White else MaterialTheme.colorScheme.outline).copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
    }
}

@Composable
fun CodeBlockView(
    language: String?,
    code: String,
    isUserBubble: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    val backgroundColor = if (isUserBubble) {
        Color.Black.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language?.trim() ?: "code",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = (if (isUserBubble) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.6f)
                )

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        copied = true
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = stringResource(Res.string.content_desc_copy_code),
                        tint = if (copied) Color.Green else (if (isUserBubble) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Code Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = if (isUserBubble) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// MARK: - Inline Markdown Parser

fun parseInlineMarkdown(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var i = 0
    while (i < text.length) {
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(text.substring(i + 2, end))
                builder.pop()
                i = end + 2
                continue
            }
        }
        if (text.startsWith("*", i) && !text.startsWith("**", i)) {
            val end = text.indexOf("*", i + 1)
            if (end != -1) {
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                builder.append(text.substring(i + 1, end))
                builder.pop()
                i = end + 1
                continue
            }
        }
        if (text.startsWith("`", i)) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                builder.pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        background = Color.Black.copy(alpha = 0.08f)
                    )
                )
                builder.append(text.substring(i + 1, end))
                builder.pop()
                i = end + 1
                continue
            }
        }
        builder.append(text[i])
        i++
    }
    return builder.toAnnotatedString()
}

// MARK: - Block Markdown Parser

fun parseMarkdown(raw: String): List<MarkdownBlock> {
    val result = mutableListOf<MarkdownBlock>()
    val paraLines = mutableListOf<String>()
    val codeLines = mutableListOf<String>()
    var inCode = false
    var codeLang: String? = null

    fun flushParagraph() {
        val joined = paraLines.joinToString("\n").trim()
        if (joined.isNotEmpty()) {
            result.add(MarkdownBlock.Paragraph(joined))
        }
        paraLines.clear()
    }

    val lines = raw.split("\n")
    for (line in lines) {
        if (line.startsWith("```")) {
            if (inCode) {
                result.add(MarkdownBlock.CodeBlock(codeLang, codeLines.joinToString("\n")))
                codeLines.clear()
                codeLang = null
                inCode = false
            } else {
                flushParagraph()
                val lang = line.substring(3).trim()
                codeLang = if (lang.isEmpty()) null else lang
                inCode = true
            }
            continue
        }

        if (inCode) {
            codeLines.add(line)
            continue
        }

        val t = line.trim()
        if (t.isEmpty()) {
            flushParagraph()
            continue
        }

        // Headings
        val hashCount = t.takeWhile { it == '#' }.length
        if (hashCount in 1..6 && t.length > hashCount && t[hashCount] == ' ') {
            val headText = t.substring(hashCount + 1).trim()
            if (headText.isNotEmpty()) {
                flushParagraph()
                result.add(MarkdownBlock.Heading(hashCount, headText))
                continue
            }
        }

        // Numbered list
        val dotIdx = t.indexOf('.')
        if (dotIdx > 0 && t.substring(0, dotIdx).all { it.isDigit() }) {
            val n = t.substring(0, dotIdx).toIntOrNull()
            if (n != null && dotIdx + 1 < t.length && t[dotIdx + 1] == ' ') {
                val itemText = t.substring(dotIdx + 2).trim()
                flushParagraph()
                result.add(MarkdownBlock.NumberedItem(n, itemText))
                continue
            }
        }

        // Bullet list
        if (t.length >= 2 && (t[0] == '-' || t[0] == '*' || t[0] == '+') && t[1] == ' ') {
            val itemText = t.substring(2).trim()
            flushParagraph()
            result.add(MarkdownBlock.BulletItem(itemText))
            continue
        }

        // Divider
        if (t == "---" || t == "***" || t == "___") {
            flushParagraph()
            result.add(MarkdownBlock.DividerBlock)
            continue
        }

        // Regular paragraph line
        paraLines.add(line)
    }

    flushParagraph()

    // Flush a dangling code block
    if (inCode && codeLines.isNotEmpty()) {
        result.add(MarkdownBlock.CodeBlock(codeLang, codeLines.joinToString("\n")))
    }

    return result
}
