package com.tripdm.agency.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tripdm.agency.data.model.ChatMessage
import com.tripdm.agency.data.model.ChatMessageStatus
import java.text.SimpleDateFormat
import java.util.*

// Clean Material3 date separator pill
@Composable
fun DateSeparator(dateText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
            shadowElevation = 0.dp
        ) {
            Text(
                text = dateText,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AgencyMessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    showAvatar: Boolean = true,
    chatUserName: String = "",
    chatUserAvatarUrl: String = "",
    modifier: Modifier = Modifier,
    onDeleteMessage: () -> Unit = {},
    onEditMessage: () -> Unit = {},
    onReactToMessage: (String) -> Unit = {},
    onReplyToMessage: () -> Unit = {},
    onQuoteClick: () -> Unit = {},
    currentUserId: String = "",
    searchQuery: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val bubbleColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val textColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val timestampColor = if (isFromCurrentUser) {
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
    }

    // Custom corner shape for bubbles (sharp on tail side)
    val shape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 12.dp,
            bottomEnd = 2.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 2.dp,
            bottomEnd = 12.dp
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromCurrentUser) 64.dp else 4.dp,
                end = if (isFromCurrentUser) 4.dp else 64.dp,
                top = 3.dp,
                bottom = 3.dp
            ),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar for received messages
        if (!isFromCurrentUser && showAvatar) {
            if (!chatUserAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = chatUserAvatarUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initialText = if (chatUserName.startsWith("Lead #")) {
                        "#" + chatUserName.removePrefix("Lead #")
                    } else {
                        chatUserName.ifEmpty { "L" }.take(1).uppercase()
                    }
                    Text(
                        text = initialText,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = if (initialText.length > 2) 10.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
        } else if (!isFromCurrentUser && !showAvatar) {
            Spacer(modifier = Modifier.width(38.dp))
        }

        // The bubble container and reactions overlay Box
        Box(
            modifier = Modifier.padding(bottom = if (message.reactions.isNotEmpty()) 8.dp else 0.dp)
        ) {
            // The bubble surface
            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 0.dp,
                modifier = Modifier.pointerInput(message.id) {
                    detectTapGestures(
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    )
                }
            ) {
                Box {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Quote / Reply block (shown if message is a reply)
                        if (message.replyToId != null) {
                            Surface(
                                onClick = onQuoteClick,
                                color = bubbleColor.copy(alpha = 0.0f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Vertical accent line
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .fillMaxHeight()
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = message.replyToSenderName ?: "Unknown",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = message.replyToContent ?: "",
                                            fontSize = 11.sp,
                                            color = if (isFromCurrentUser)
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            else
                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Build highlighted annotated string for search matches
                        val annotatedContent = remember(message.content, searchQuery) {
                            if (searchQuery.isBlank()) {
                                AnnotatedString(message.content)
                            } else {
                                buildAnnotatedString {
                                    val lower = message.content.lowercase()
                                    val queryLower = searchQuery.lowercase()
                                    var cursor = 0
                                    while (cursor < message.content.length) {
                                        val idx = lower.indexOf(queryLower, cursor)
                                        if (idx == -1) {
                                            append(message.content.substring(cursor))
                                            break
                                        }
                                        append(message.content.substring(cursor, idx))
                                        withStyle(
                                            SpanStyle(
                                                background = Color(0xFFFFD740),
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append(message.content.substring(idx, idx + searchQuery.length))
                                        }
                                        cursor = idx + searchQuery.length
                                    }
                                }
                            }
                        }

                        Text(
                            text = annotatedContent,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Timestamp + status ticks row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = if (message.isEdited) "Edited • ${formatTimestamp(message.timestamp)}" else formatTimestamp(message.timestamp),
                                color = timestampColor,
                                fontSize = 10.sp
                            )

                            // Status ticks for sent messages with cross-fade transition
                            if (isFromCurrentUser) {
                                Spacer(modifier = Modifier.width(3.dp))
                                AnimatedContent(
                                    targetState = message.status,
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    },
                                    label = "ticks"
                                ) { status ->
                                    val icon = if (status == ChatMessageStatus.SENT) Icons.Default.Check
                                               else Icons.Default.DoneAll
                                    val tint = if (status == ChatMessageStatus.READ)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        timestampColor
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Emoji Reactions Row
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val emojis = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
                                    emojis.forEach { emoji ->
                                        IconButton(
                                            onClick = {
                                                showMenu = false
                                                onReactToMessage(emoji)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Text(text = emoji, fontSize = 20.sp)
                                        }
                                    }
                                }
                            },
                            onClick = {} // No-op for the row itself
                        )

                        DropdownMenuItem(
                            text = { Text("Copy Text") },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.content))
                                showMenu = false
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reply") },
                            onClick = {
                                showMenu = false
                                onReplyToMessage()
                            }
                        )
                        if (isFromCurrentUser) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEditMessage()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Floating reactions pill badge
            if (message.reactions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 1.dp,
                    onClick = {
                        // Tapping the reactions pill removes the current user's reaction if present
                        if (currentUserId.isNotEmpty()) {
                            val myReaction = message.reactions[currentUserId]
                            if (myReaction != null) {
                                onReactToMessage(myReaction)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(if (isFromCurrentUser) Alignment.BottomEnd else Alignment.BottomStart)
                        .offset(
                            x = if (isFromCurrentUser) (-12).dp else 12.dp,
                            y = 8.dp
                        )
                ) {
                    val uniqueEmojis = message.reactions.values.distinct().joinToString(" ")
                    Text(
                        text = uniqueEmojis,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to delete this message? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteMessage()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val messageTime = Date(timestamp)
    val today = Date(now)
    val yesterday = Date(now - 24 * 60 * 60 * 1000)

    val messageCalendar = Calendar.getInstance().apply { time = messageTime }
    val todayCalendar = Calendar.getInstance().apply { time = today }
    val yesterdayCalendar = Calendar.getInstance().apply { time = yesterday }

    return when {
        // Today - show time only
        messageCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime)
        }
        // Yesterday - show "Yesterday"
        messageCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR) -> {
            "Yesterday ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime)}"
        }
        // Within this week - show day name
        now - timestamp < 7 * 24 * 60 * 60 * 1000 -> {
            "${SimpleDateFormat("EEE", Locale.getDefault()).format(messageTime)} ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime)}"
        }
        // Older - show date and time
        else -> {
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(messageTime)
        }
    }
}

fun formatDateSeparator(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val messageTime = Date(timestamp)
    val today = Date(now)
    val yesterday = Date(now - 24 * 60 * 60 * 1000)

    val messageCalendar = Calendar.getInstance().apply { time = messageTime }
    val todayCalendar = Calendar.getInstance().apply { time = today }
    val yesterdayCalendar = Calendar.getInstance().apply { time = yesterday }

    return when {
        messageCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> "Today"
        messageCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) &&
        messageCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(messageTime)
    }
}
