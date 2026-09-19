package com.tripdm.agency.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tripdm.agency.R
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.data.model.ChatMessage
import com.tripdm.agency.data.model.ChatMessageStatus
import com.tripdm.agency.ui.components.AgencyMessageBubble
import com.tripdm.agency.ui.components.AgencyMessageInput
import com.tripdm.agency.ui.components.DateSeparator
import com.tripdm.agency.ui.components.chatTravelBackground
import com.tripdm.agency.ui.components.formatDateSeparator
import kotlinx.coroutines.launch

private fun playChatSound(context: Context, isSent: Boolean) {
    try {
        val soundRes = if (isSent) R.raw.sendsound else R.raw.getsound
        val mediaPlayer = MediaPlayer.create(context.applicationContext, soundRes)
        mediaPlayer?.setOnCompletionListener { mp ->
            try {
                mp.release()
            } catch (t: Throwable) {
                android.util.Log.e("AgencyChatScreen", "Error releasing media player", t)
            }
        }
        mediaPlayer?.start()
    } catch (t: Throwable) {
        android.util.Log.e("AgencyChatScreen", "Error playing chat sound", t)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AgencyChatScreen(
    currentAgencyId: String,
    currentAgencyName: String = "",
    conversation: ChatConversation,
    messages: List<ChatMessage>,
    isLoadingHistory: Boolean = false,
    hasMoreHistory: Boolean = false,
    historyError: String? = null,
    onSendMessage: (content: String, replyToId: String?, replyToContent: String?, replyToSenderName: String?) -> Unit,
    onBack: () -> Unit,
    onLoadMoreHistory: (() -> Unit)? = null,
    onClearHistoryError: (() -> Unit)? = null,
    onDeleteMessage: ((String) -> Unit)? = null,
    onEditMessage: ((String, String) -> Unit)? = null,
    onReactToMessage: ((String, String) -> Unit)? = null,
    isPartnerTyping: Boolean = false,
    onTyping: () -> Unit = {},
    onEnterChat: () -> Unit = {},
    onLeaveChat: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    DisposableEffect(conversation.otherUserId) {
        onEnterChat()
        onDispose { onLeaveChat() }
    }

    // Play chat sounds on new messages
    var isHistoryLoaded by remember { mutableStateOf(false) }
    var lastMessageId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages, isLoadingHistory) {
        if (isLoadingHistory) return@LaunchedEffect

        val latest = messages.lastOrNull()
        if (latest != null) {
            if (isHistoryLoaded && latest.id != lastMessageId) {
                val isFromMe = latest.from == currentAgencyId
                playChatSound(context, isSent = isFromMe)
            }
            lastMessageId = latest.id
            isHistoryLoaded = true
        } else {
            isHistoryLoaded = true
        }
    }

    // Connection state monitoring
    var isOnline by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let { cm ->
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    isOnline = true
                }

                override fun onLost(network: android.net.Network) {
                    isOnline = false
                }
            })
        }
    }

    // Group messages by date for separators
    val messagesWithDates = remember(messages) {
        val result = mutableListOf<Pair<Any?, ChatMessage>>()
        var lastDateHeader: String? = null
        val uniqueMessages = messages.distinctBy { it.id }
        for (msg in uniqueMessages) {
            val dateHeader = formatDateSeparator(msg.timestamp)
            if (dateHeader != lastDateHeader) {
                result.add(Pair(null, msg))
                lastDateHeader = dateHeader
            }
            result.add(Pair(msg, msg))
        }
        result
    }

    // Scroll state management
    var hasScrolledToInitial by remember(conversation.otherUserId) { mutableStateOf(false) }
    var lastScrolledMessageId by remember(conversation.otherUserId) { mutableStateOf<String?>(null) }
    val messagesWithDatesReversed = remember(messagesWithDates) {
        messagesWithDates.reversed()
    }

    LaunchedEffect(messages) {
        if (messages.isEmpty() || hasScrolledToInitial) return@LaunchedEffect

        val firstUnreadIndex = messagesWithDates.indexOfFirst { (type, message) ->
            type != null && message.from != currentAgencyId && message.status != ChatMessageStatus.READ
        }
        if (firstUnreadIndex != -1) {
            val reversedIndex = messagesWithDates.size - 1 - firstUnreadIndex
            listState.scrollToItem(reversedIndex)
        } else {
            listState.scrollToItem(0)
        }
        hasScrolledToInitial = true
        lastScrolledMessageId = messages.lastOrNull()?.id
    }

    LaunchedEffect(messages) {
        if (messages.isEmpty()) return@LaunchedEffect
        val latestMessageId = messages.lastOrNull()?.id
        if (hasScrolledToInitial && latestMessageId != lastScrolledMessageId) {
            if (listState.firstVisibleItemIndex < 3) {
                listState.animateScrollToItem(0)
            }
            lastScrolledMessageId = latestMessageId
        }
    }

    val animatedMessageIds = remember { mutableStateListOf<String>() }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var replyingMessage by remember { mutableStateOf<ChatMessage?>(null) }

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    val displayedMessages = remember(messages, isSearchActive, searchQuery) {
        if (!isSearchActive || searchQuery.isBlank()) messages
        else messages.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(displayedMessages, isSearchActive) {
        if (isSearchActive && searchQuery.isNotBlank() && displayedMessages.isNotEmpty()) {
            listState.animateScrollToItem(displayedMessages.size - 1)
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            kotlinx.coroutines.delay(50)
            searchFocusRequester.requestFocus()
        }
    }

    val searchMessagesWithDates = remember(displayedMessages) {
        val result = mutableListOf<Pair<Any?, ChatMessage>>()
        var lastDateHeader: String? = null
        val uniqueMessages = displayedMessages.distinctBy { it.id }
        for (msg in uniqueMessages) {
            val dateHeader = formatDateSeparator(msg.timestamp)
            if (dateHeader != lastDateHeader) {
                result.add(Pair(null, msg))
                lastDateHeader = dateHeader
            }
            result.add(Pair(msg, msg))
        }
        result
    }
    val displayedWithDatesReversed = remember(searchMessagesWithDates) {
        searchMessagesWithDates.reversed()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = Color(0xFFFAF6F0),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                searchQuery = ""
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isSearchActive) "Close Search" else "Back",
                                tint = Color(0xFF111827)
                            )
                        }

                        AnimatedContent(
                            targetState = isSearchActive,
                            transitionSpec = {
                                fadeIn(tween(200)) + slideInHorizontally { it / 3 } togetherWith
                                fadeOut(tween(150)) + slideOutHorizontally { -it / 3 }
                            },
                            modifier = Modifier.weight(1f)
                        ) { searchMode ->
                            if (searchMode) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color(0xFF111827),
                                        fontSize = 15.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(searchFocusRequester),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color(0xFFFFFFFF),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    Color(0x20000000),
                                                    RoundedCornerShape(20.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    "Search messages…",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 15.sp
                                                )
                                            }
                                            inner()
                                        }
                                    }
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val avatarUrl = conversation.avatarUrl
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFEDD5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (avatarUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = "Profile",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                             val initialText = if (conversation.otherUserName.startsWith("Lead #")) {
                                                 "#" + conversation.otherUserName.removePrefix("Lead #")
                                             } else {
                                                 conversation.otherUserName.ifEmpty { "T" }.take(2).uppercase()
                                             }
                                             Text(
                                                 text = initialText,
                                                 color = Color(0xFF92400E),
                                                 fontSize = if (initialText.length > 2) 11.sp else 14.sp,
                                                 fontWeight = FontWeight.Bold
                                             )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = conversation.otherUserName,
                                            color = Color(0xFF111827),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val shortId = conversation.otherUserId.take(8)
                                            Text(
                                                text = "Customer ID: $shortId",
                                                color = Color(0xFF6B7280),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!conversation.relatedListingTitle.isNullOrBlank()) {
                                                Surface(
                                                    color = Color(0xFFFFF7ED),
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74))
                                                ) {
                                                    Text(
                                                        text = "⏱️ ${conversation.relatedListingTitle}",
                                                        color = Color(0xFFC2410C),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isSearchActive && searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (!isSearchActive) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = { /* menu */ }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (isSearchActive && searchQuery.isNotBlank()) {
                            Text(
                                text = "${displayedMessages.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .chatTravelBackground()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(
                        items = if (isSearchActive && searchQuery.isNotBlank()) displayedWithDatesReversed else messagesWithDatesReversed,
                        key = { (first, second) ->
                            if (first == null) "date_${second.id}" else second.id
                        }
                    ) { (type, message) ->
                        if (type == null) {
                            val dateText = formatDateSeparator(message.timestamp)
                            DateSeparator(dateText = dateText)
                        } else {
                            val isFromMe = message.from == currentAgencyId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            ) {
                                AgencyMessageBubble(
                                    message = message,
                                    isFromCurrentUser = isFromMe,
                                    showAvatar = !isFromMe,
                                    chatUserName = conversation.otherUserName,
                                    chatUserAvatarUrl = conversation.avatarUrl,
                                    modifier = Modifier.fillMaxWidth(),
                                    onDeleteMessage = { onDeleteMessage?.invoke(message.id) },
                                    onEditMessage = {
                                        replyingMessage = null
                                        editingMessage = message
                                    },
                                    onReactToMessage = { emoji -> onReactToMessage?.invoke(message.id, emoji) },
                                    onReplyToMessage = {
                                        editingMessage = null
                                        replyingMessage = message
                                    },
                                    onQuoteClick = {
                                        val idx = messages.reversed().indexOfFirst { it.id == message.replyToId }
                                        if (idx >= 0) {
                                            coroutineScope.launch { listState.animateScrollToItem(idx) }
                                        }
                                    },
                                    currentUserId = currentAgencyId,
                                    searchQuery = if (isSearchActive) searchQuery else ""
                                )
                            }
                        }
                    }

                    if (isLoadingHistory) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading history...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (hasMoreHistory && !isLoadingHistory) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { onLoadMoreHistory?.invoke() }
                                ) {
                                    Text(
                                        "Load Older Messages",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    if (messages.isEmpty() && !isLoadingHistory) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "🔒",
                                                fontSize = 28.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Messages are end-to-end encrypted.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                     Text(
                                         text = "No messages yet. Say hello to ${conversation.otherUserName}!",
                                         fontSize = 14.sp,
                                         color = MaterialTheme.colorScheme.onSurface,
                                         fontWeight = FontWeight.Medium
                                     )
                                }
                            }
                        }
                    }
                }
            }

            if (!isOnline) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 No internet connection",
                            color = MaterialTheme.colorScheme.onError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            historyError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ $error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { onClearHistoryError?.invoke() }
                        ) {
                            Text(
                                text = "DISMISS",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isPartnerTyping,
                enter = fadeIn(tween(200)) + expandVertically(),
                exit = fadeOut(tween(150)) + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dotAlphas = (0..2).map { i ->
                                val transition = rememberInfiniteTransition(label = "dot$i")
                                transition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = keyframes {
                                            durationMillis = 1200
                                            0.3f at 0
                                            1f at 300 + i * 150
                                            0.3f at 600 + i * 150
                                        },
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "dotAlpha$i"
                                ).value
                            }
                            dotAlphas.forEach { alpha ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                                        )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${conversation.otherUserName} is typing…",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            AgencyMessageInput(
                onSendMessage = { content ->
                    onSendMessage(
                        content,
                        replyingMessage?.id,
                        replyingMessage?.content,
                        if (replyingMessage?.from == currentAgencyId) currentAgencyName
                        else conversation.otherUserName
                    )
                    replyingMessage = null
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                },
                modifier = Modifier.fillMaxWidth(),
                editingMessage = editingMessage,
                onEditMessage = { newContent ->
                    editingMessage?.let { msg ->
                        onEditMessage?.invoke(msg.id, newContent)
                    }
                    editingMessage = null
                },
                onCancelEdit = { editingMessage = null },
                replyingMessage = replyingMessage,
                replyingMessageSenderName = if (replyingMessage?.from == currentAgencyId) currentAgencyName
                                            else conversation.otherUserName,
                onCancelReply = { replyingMessage = null },
                onTyping = onTyping
            )
        }
    }
}
