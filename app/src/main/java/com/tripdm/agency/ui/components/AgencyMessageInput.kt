package com.tripdm.agency.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.data.model.ChatMessage

val AGENCY_QUICK_REPLIES = listOf(
    "Yes, it's available. When are you planning to travel?",
    "Would you like me to send the complete itinerary?",
    "How many people are travelling?",
    "We have a special offer going on, would you like to hear about it?"
)

@Composable
fun AgencyMessageInput(
    onSendMessage: (String) -> Unit,
    isSending: Boolean = false,
    quickReplies: List<String> = AGENCY_QUICK_REPLIES,
    modifier: Modifier = Modifier,
    editingMessage: ChatMessage? = null,
    onEditMessage: (String) -> Unit = {},
    onCancelEdit: () -> Unit = {},
    replyingMessage: ChatMessage? = null,
    replyingMessageSenderName: String? = null,
    onCancelReply: () -> Unit = {},
    onTyping: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    val hapticFeedback = LocalHapticFeedback.current

    // Sync input text field with editingMessage
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            messageText = editingMessage.content
        } else {
            messageText = ""
        }
    }

    val handleSendOrEdit = {
        if (messageText.isNotBlank() && !isSending) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val trimmedText = messageText.trim()
            if (editingMessage != null) {
                onEditMessage(trimmedText)
            } else {
                onSendMessage(trimmedText)
            }
            messageText = ""
        }
    }

    Surface(
        color = Color(0xFFFAF6F0),
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding()
        ) {
            HorizontalDivider(color = Color(0x1F000000), thickness = 0.5.dp)

            // Quick Replies row - shown when text field is empty (standard chat UX)
            if (quickReplies.isNotEmpty() && messageText.isBlank() && editingMessage == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickReplies.forEach { reply ->
                        Surface(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSendMessage(reply)
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFFFFFFF),
                            contentColor = Color(0xFF374151),
                            border = BorderStroke(0.5.dp, Color(0x20000000)),
                            shadowElevation = 0.5.dp
                        ) {
                            Text(
                                text = reply,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Reply preview bar
            if (replyingMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2F7D8))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Replying to",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reply to ${replyingMessageSenderName ?: "message"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = replyingMessage.content,
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Reply",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4B5563)
                        )
                    }
                }
            }

            // Editing Message preview bar
            if (editingMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editing Message",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Editing message",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = editingMessage.content,
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Edit",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4B5563)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* emoji picker */ },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                        contentDescription = "Emoji",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Clean input container pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp)
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color(0x28000000),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "Type a message...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = messageText,
                            onValueChange = {
                                messageText = it
                                if (it.isNotBlank()) onTyping()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSending,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF111827),
                                fontSize = 14.sp
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    handleSendOrEdit()
                                }
                            ),
                            singleLine = false,
                            maxLines = 5,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Morphing send button
                AnimatedContent(
                    targetState = (messageText.isNotBlank() || editingMessage != null),
                    transitionSpec = {
                        (scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn()) togetherWith
                        (scaleOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut())
                    },
                    label = "sendMic"
                ) { hasTextOrEditing ->
                    IconButton(
                        onClick = {
                            if (hasTextOrEditing) {
                                handleSendOrEdit()
                            } else {
                                /* Voice input action */
                            }
                        },
                        enabled = !isSending,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (hasTextOrEditing) MaterialTheme.colorScheme.primary
                                else Color(0xFF475569),
                                CircleShape
                            )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = if (editingMessage != null) Icons.Default.Check
                                               else if (hasTextOrEditing) Icons.Default.Send
                                               else Icons.Default.Mic,
                                contentDescription = if (editingMessage != null) "Save changes"
                                                     else if (hasTextOrEditing) "Send"
                                                     else "Record voice message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
