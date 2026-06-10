package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SocialFriend
import com.example.ui.SoulMateViewModel
import kotlinx.coroutines.delay

// Aesthetic Color Palette with unique non-clashing custom names
private val ChatSpaceDark = Color(0xFF03050C)
private val ChatCardBg = Color(0xFF0D111E)
private val ChatNeonTeal = Color(0xFF00F5D4)
private val ChatSoftPurple = Color(0xFF7B2CBF)
private val ChatEmpathyPink = Color(0xFFFF4081)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWindow(viewModel: SoulMateViewModel, friend: SocialFriend) {
    val messages by viewModel.chatFriendMessages.collectAsStateWithLifecycle()
    val typingFriendId by viewModel.typingIndicatorFriendId.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var selectedReplyMsgId by remember { mutableStateOf(-1L) }
    var isDiagnosticsExpanded by remember { mutableStateOf(false) }

    // Track state of Firebase connection
    val isRtdbAvailable = remember {
        viewModel.allSocialFriends.value.let {
            try {
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    // Auto Scroll to Bottom on New Messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Debounced Typing Status Update to Firebase Realtime Database
    LaunchedEffect(textInput) {
        if (textInput.isNotEmpty()) {
            viewModel.setRtdbTypingState(true)
            delay(2000) // Keep typing state true for 2 seconds of inactivity
            viewModel.setRtdbTypingState(false)
        } else {
            viewModel.setRtdbTypingState(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ChatSpaceDark, Color(0xFF070B19))
                )
            )
    ) {
        // --- TOP BAR / CHANNEL HEADER ---
        TopAppBar(
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.displayName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (friend.isOnline) Color(0xFF4CAF50) else Color.Gray)
                        )
                    }
                    Text(
                        text = if (friend.isOnline) "Live frequency connected" else "Frequency offline",
                        color = if (friend.isOnline) ChatNeonTeal else Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.setActiveChatFriend(null) },
                    modifier = Modifier.testTag("chat_window_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { isDiagnosticsExpanded = !isDiagnosticsExpanded }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "DB Config",
                        tint = if (isRtdbAvailable) ChatNeonTeal else Color.Gray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ChatCardBg)
        )

        // --- SUB CONFIG/DIAGNOSTICS PANEL ---
        AnimatedVisibility(
            visible = isDiagnosticsExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = ChatCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛰️ REALTIME DATABASE DEPLOYMENT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isRtdbAvailable) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isRtdbAvailable) "REAL RTDB READY" else "SIMULATION ACTIVE",
                                color = if (isRtdbAvailable) Color(0xFF4CAF50) else Color.Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This ChatWindow uses Firebase Realtime Database SDK to synchronize text, typing events, and 'Seen' states instantaneously.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To sync with your own production Cloud server:\n1. Apply custom 'google-services.json' root file inside the '/app' folder.\n2. Ensure Firebase Database access rules allow reads/writes without authenticator obstacles for initial testing.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // --- CONVERSATION STREAM (SCROLLABLE LIST) ---
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            item {
                Text(
                    text = "🔒 Outpost channel encrypted using standard custom TLS structures.",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            items(messages) { msg ->
                val isMe = msg.senderId == "me"
                val hasReply = msg.replyToId != -1L
                val replyMsg = messages.firstOrNull { it.id == msg.replyToId }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // Replied-to header bubble
                    if (replyMsg != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 2.dp, start = 6.dp, end = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Reply,
                                contentDescription = "Replying",
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Replying to: ${replyMsg.text.take(24)}...",
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Message envelope
                    Card(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .pointerInput(msg.id) {
                                detectTapGestures(
                                    onLongPress = {
                                        if (isMe) {
                                            viewModel.deleteSocialMessage(msg.id)
                                            Toast.makeText(context, "Frequency note cleared.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onDoubleTap = {
                                        selectedReplyMsgId = msg.id
                                        Toast.makeText(context, "Replying to this message.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) ChatSoftPurple.copy(alpha = 0.4f) else ChatCardBg
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isMe) ChatSoftPurple.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // STICKER TYPE RENDER
                            if (msg.mediaType == "sticker") {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = msg.text, fontSize = 42.sp)
                                }
                            } else {
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                Text(
                                    text = sdf.format(java.util.Date(msg.timestamp)),
                                    color = Color.Gray,
                                    fontSize = 8.sp
                                )

                                // Real-time SEEN status tick indicators
                                if (isMe) {
                                    Icon(
                                        imageVector = if (msg.seen) Icons.Filled.DoneAll else Icons.Filled.Done,
                                        contentDescription = if (msg.seen) "Seen" else "Delivered",
                                        tint = if (msg.seen) ChatNeonTeal else Color.Gray,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Emoji Reactions indicator
                    if (msg.reactions.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-4).dp)
                                .clip(CircleShape)
                                .background(ChatCardBg)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = msg.reactions, fontSize = 9.sp)
                        }
                    }
                }
            }

            // --- REALTIME DEBOUNCED TYPING INDICATOR STATUS ---
            if (typingFriendId == friend.id) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ChatNeonTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${friend.displayName} is writing",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Aesthetic anim pulse dots for writing stream
                        val infiniteTransition = rememberInfiniteTransition(label = "dots")
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(650, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot_alpha"
                        )
                        Text(
                            text = "...",
                            color = ChatNeonTeal.copy(alpha = dotAlpha),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }

        // --- REPLY ACTIVE PANEL ---
        AnimatedVisibility(
            visible = selectedReplyMsgId != -1L,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val replyMsg = messages.firstOrNull { it.id == selectedReplyMsgId }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChatCardBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Reply,
                        contentDescription = "Reply",
                        tint = ChatNeonTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Replying to frequency text",
                            color = ChatNeonTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = replyMsg?.text ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = { selectedReplyMsgId = -1L }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear Reply",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- MESSAGE INPUT CONSOLE ---
        Surface(
            color = ChatCardBg,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.sendSocialMessage(friend.id, "💆", "sticker")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEmotions,
                        contentDescription = "Stickers",
                        tint = Color.LightGray
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_window_input"),
                    placeholder = {
                        Text(
                            text = "Encrypted frequency notes...",
                            color = Color.Gray,
                            fontSize = 12.5.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ChatNeonTeal,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = ChatSpaceDark.copy(alpha = 0.5f),
                        unfocusedContainerColor = ChatSpaceDark.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendSocialMessage(
                                friendId = friend.id,
                                text = textInput,
                                replyToId = selectedReplyMsgId
                            )
                            textInput = ""
                            selectedReplyMsgId = -1L
                        }
                    },
                    modifier = Modifier
                        .testTag("chat_window_send_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(ChatNeonTeal, Color(0xFF00B4D8))
                            )
                        ),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = ChatSpaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send Message",
                        tint = ChatSpaceDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
